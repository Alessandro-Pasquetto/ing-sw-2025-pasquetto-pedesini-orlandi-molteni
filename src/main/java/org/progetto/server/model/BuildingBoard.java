package org.progetto.server.model;
import java.util.*;

import org.progetto.server.model.components.*;
import org.progetto.server.model.loadClasses.MaskMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;


public class BuildingBoard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Spaceship spaceship;
    private Component[][] spaceshipMatrix;  //composition of components
    private int[][] boardMask;              //mask layer for building clearance (0 = buildable, -1 = built, 1 = notBuildable)
    private ArrayList<Component> booked;    //list for booked components storage
    private Component handComponent;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(int levelShip, int color, Spaceship spaceship) {
        this.spaceship = spaceship;
        this.boardMask = loadBoardMask(levelShip);
        this.spaceshipMatrix = createSpaceshipMatrix();
        this.booked = new ArrayList<>(2);
        this.handComponent = null;
        this.imgSrc = loadImgSrc(levelShip);

        placeCentralUnit(levelShip, color);
    }

    // =======================
    // GETTERS
    // =======================

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public Component getHandComponent() {
        return handComponent;
    }

    public Component[][] getSpaceshipMatrix() {
        return spaceshipMatrix;
    }

    public int[][] getBoardMask() {
        return boardMask;
    }

    public ArrayList<Component> getBooked() {
        return booked;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    public boolean setAsBooked(Component component) {
        if (booked.size() < 2) {
            booked.add(component);  // need to handle booked flag in component
            return true;
        } else {
            return false;
        }
    }

    public void setHandComponent(Component component) {
        handComponent = component;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * @param levelShip the ship's level
     * @param color the player's color
     */
    private void placeCentralUnit(int levelShip, int color) {
        switch (levelShip) {
            case 1:
                spaceshipMatrix[2][2] = new StorageComponent(ComponentType.CENTRAL_UNIT, new int[]{3,3,3,3}, "img" + color + "path", 2);
                break;
            case 2:
                spaceshipMatrix[2][3] = new StorageComponent(ComponentType.CENTRAL_UNIT, new int[]{3,3,3,3}, "img" + color + "path", 2);
                break;
        }
    }

    /**
     *
     * @param type is the component type to search in the matrix
     * @return list of components found
     */
    private List<Component> typeSearch(ComponentType type) {

        ArrayList<Component> found_list = new ArrayList<>();

        for(int i = 0; i < spaceshipMatrix.length; i++) {
            for(int j = 0; j < spaceshipMatrix[i].length; j++) {

                if(spaceshipMatrix[i][j].getType() == type)
                    found_list.add(spaceshipMatrix[i][j]);
            }
        }
        return found_list;
    }

    /**
     * @author Lorenzo
     * @param levelShip is the game level chosen
     * @return the loaded matrix configuration for the board
     */
    private int[][] loadBoardMask(int levelShip)
    {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            MaskMatrix data = objectMapper.readValue(new File("src/main/resources/org.progetto.server/Masks.json"), MaskMatrix.class);

            switch (levelShip)
            {
                case 1:
                    boardMask = data.getBaseMatrix();
                    break;

                case 2:
                    boardMask = data.getAdvancedMatrix();
                    break;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return boardMask;
    }

    private String loadImgSrc(int levelShip){
        return "imgPath";
    }

    /**
     * @author Lorenzo
     * @return the component matrix for the spaceship initialized with null values
     */
    private Component[][] createSpaceshipMatrix()
    {
        return new Component[boardMask.length][boardMask[0].length];
    }

    /**
     * @author Lorenzo
     * @param y coordinate for placing component
     * @param x coordinate for placing component
     * @return true if component has been placed correctly else otherwise
     */
    public boolean placeComponent(int y, int x) {
        if(boardMask[y][x] == 0) {
            spaceship.addComponentShipCount(1);

            spaceshipMatrix[y][x] = handComponent;
            spaceshipMatrix[y][x].setPlaced(true);
            spaceshipMatrix[y][x].setY_coordinate(y);
            spaceshipMatrix[y][x].setX_coordinate(x);

            boardMask[y][x] = -1;   //signal the presence of a component
            handComponent = null;
            return true;
        }

        return false;
    }

    /**
     * @author Lorenzo
     * @param y is the y coordinate of the component to remove
     * @param x is the x coordinate of the component to remove
     * @return true il the component can be removed
     */
    public boolean destroyComponent(int y, int x) {
        if(boardMask[y][x] != -1)
            return false;

        spaceship.addComponentShipCount(-1);
        spaceship.addDestroyedCount(1);
        Component destroyedComponent = spaceshipMatrix[y][x];
        spaceshipMatrix[y][x] = null;

        switch (destroyedComponent.getType()) {

            case CANNON:
                if (destroyedComponent.getRotation() == 0)
                    spaceship.addNormalShootingPower(-1);
                else
                    spaceship.addNormalShootingPower((float) -0.5);
                break;

            case DOUBLE_CANNON:
                spaceship.addDoubleCannonCount(-1);
                break;

            case ENGINE:
                spaceship.addNormalEnginePower(-1);
                break;

            case DOUBLE_ENGINE:
                spaceship.addDoubleEngineCount(-1);
                break;

            case SHIELD:
                switch (destroyedComponent.getRotation()) {

                    case 0: // left-up
                        spaceship.addLeftUpShieldCount(-1);
                        break;

                    case 1: // up-right
                        spaceship.addUpRightShieldCount(-1);
                        break;

                    case 2: // right-down
                        spaceship.addRightDownShieldCount(-1);
                        break;

                    case 3: // down-left
                        spaceship.addDownLeftShieldCount(-1);
                        break;
                }

                break;

            case HOUSING_UNIT:
                StorageComponent sc = (StorageComponent) destroyedComponent;
                if(sc.getOrangeAlien())
                    spaceship.setAlienOrange(false);

                if(sc.getPurpleAlien())
                    spaceship.setAlienPurple(false);

                spaceship.addCrewCount(-sc.getItemsCount());
                break;

            case ORANGE_HOUSING_UNIT:
                List<Component> orange_units = typeSearch(ComponentType.ORANGE_HOUSING_UNIT);
                if(orange_units.size() == 1) {    //if only a module is present
                    spaceship.setAlienOrange(false);
                }
                break;

            case PURPLE_HOUSING_UNIT:
                List<Component> purple_units = typeSearch(ComponentType.PURPLE_HOUSING_UNIT);
                if(purple_units.size() == 1) {    //if only a module is present
                    spaceship.setAlienPurple(false);
                }
                break;

            case CENTRAL_UNIT:
                sc = (StorageComponent) destroyedComponent;
                if(sc.getOrangeAlien())
                    spaceship.setAlienOrange(false);

                if(sc.getPurpleAlien())
                    spaceship.setAlienPurple(false);

                spaceship.addCrewCount(-sc.getItemsCount());
                break;

            case STRUCTURAL_UNIT:
                break;

            case BATTERY_STORAGE:
                sc = (StorageComponent) destroyedComponent;
                spaceship.addBatteriesCount(-sc.getItemsCount());
                break;

            case RED_BOX_STORAGE:
                BoxStorageComponent bsc = (BoxStorageComponent) destroyedComponent;
                Box[] boxes = bsc.getBoxStorage();

                for (Box box : boxes) {
                    if(box == null)
                        continue;

                    switch (box.getType()) {
                        case RED:
                            spaceship.addRedBoxCount(-1);
                            break;
                        case YELLOW:
                            spaceship.addYellowBoxCount(-1);
                            break;
                        case GREEN:
                            spaceship.addGreenBoxCount(-1);
                            break;
                        case BLUE:
                            spaceship.addBlueBoxCount(-1);
                            break;
                    }
                }
                break;

            case BOX_STORAGE:
                bsc = (BoxStorageComponent) destroyedComponent;
                boxes = bsc.getBoxStorage();

                for (Box box : boxes) {
                    if(box == null)
                        continue;

                    switch (box.getType()) {
                        case YELLOW:
                            spaceship.addYellowBoxCount(-1);
                            break;
                        case GREEN:
                            spaceship.addGreenBoxCount(-1);
                            break;
                        case BLUE:
                            spaceship.addBlueBoxCount(-1);
                            break;
                    }
                }
                break;

            default:
                break;
        }

        System.gc();   //call for garbage collector
        return true;
    }

    public boolean areConnected(Component c1, Component c2) {

        return false;
    }
}