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
    private int[][] boardMask;   //mask layer for building clearance
    private ArrayList<Component> booked;  //list for booked components storage
    private Component handComponent;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(int levelShip, Spaceship spaceship) {
        this.spaceship = spaceship;
        this.boardMask = loadBoardMask(levelShip);
        this.spaceshipMatrix = createSpaceshipMatrix(levelShip);
        this.imgSrc = loadImgSrc(levelShip);
        this.booked = new ArrayList<>(2);
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
     *
     * @param type is the component type to search in the matrix
     * @return list of components found
     */
    private  List<Component> typeSearch(ComponentType type) {

        List<Component> found_list = new ArrayList<>();

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
     * @param levelShip is the game level chosen
     * @return the component matrix for the spaceship initialized with null values
     */
    private Component[][] createSpaceshipMatrix(int levelShip)
    {
        return new Component[boardMask.length][boardMask.length];
    }


    /**
     * @author Lorenzo
     * @param y coordinate for placing component
     * @param x coordinate for placing component
     * @return true if component has been placed correctly else otherwise
     */
    public boolean placeComponent(int y, int x) {
        if(boardMask[y][x] == 0) {
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
        if(boardMask[y][x] == -1) {
            spaceshipMatrix[y][x].setPlaced(false);
            spaceship.setDestroyedCount(spaceship.getDestroyedCount() + 1);

            switch (spaceshipMatrix[y][x].getType()) {
                case CANNON:
                    if (spaceshipMatrix[y][x].getRotation() == 2)
                        spaceship.setNormalShootingPower(spaceship.getNormalShootingPower() - 1);
                    else
                        spaceship.setNormalShootingPower((float) (spaceship.getNormalShootingPower() - 0.5));
                    break;

                case DOUBLE_CANNON:
                    spaceship.setNormalShootingPower(spaceship.getDoubleCannonCount() - 1);
                    break;

                case ENGINE:
                    spaceship.setNormalEnginePower(spaceship.getNormalEnginePower() - 1);
                    break;

                case DOUBLE_ENGINE:
                    spaceship.setDoubleEngineCount(spaceship.getDoubleEngineCount() - 1);
                    break;

                case SHIELD:

                    int[] lst = spaceship.getShields();
                    switch (spaceshipMatrix[y][x].getRotation()) {

                        case 0:
                            lst[0] = lst[0] - 1;
                            lst[1] = lst[1] - 1;
                            spaceship.setShields(lst);
                            break;

                        case 1:
                            lst[1] = lst[1] - 1;
                            lst[2] = lst[2] - 1;
                            spaceship.setShields(lst);
                            break;

                        case 2:
                            lst[2] = lst[2] - 1;
                            lst[3] = lst[3] - 1;
                            spaceship.setShields(lst);
                            break;

                        case 3:
                            lst[3] = lst[3] - 1;
                            lst[0] = lst[0] - 1;
                            spaceship.setShields(lst);
                            break;
                    }

                    break;

                case HOUSING_UNIT:
                    StorageComponent sc = (StorageComponent) spaceshipMatrix[y][x];
                    if(sc.getOrangeAlien())
                        spaceship.setAlienOrange(false);

                    if(sc.getPurpleAlien())
                        spaceship.setAlienPurple(false);

                    spaceship.setCrew(spaceship.getCrewCount() - sc.getItemsCount());
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

                    sc = (StorageComponent) spaceshipMatrix[y][x];
                    if(sc.getOrangeAlien())
                        spaceship.setAlienOrange(false);

                    if(sc.getPurpleAlien())
                        spaceship.setAlienPurple(false);

                    spaceship.setCrew(spaceship.getCrewCount() - sc.getItemsCount());
                    break;

                case STRUCTURAL_UNIT:
                    break;

                case BATTERY_STORAGE:
                    sc = (StorageComponent) spaceshipMatrix[y][x];
                    spaceship.setBatteriesCount(spaceship.getBatteriesCount() - sc.getItemsCount());
                    break;

                case RED_BOX_STORAGE:
                    BoxStorageComponent bsc = (BoxStorageComponent) spaceshipMatrix[y][x];
                    Box[] boxes = bsc.getBoxStorage();

                    int tot_value = 0;
                    for (Box box : boxes) {
                        tot_value = tot_value + box.getValue();
                    }
                    spaceship.setBoxValue(spaceship.getBoxValue() - tot_value);
                    break;

                case BOX_STORAGE:
                    bsc = (BoxStorageComponent) spaceshipMatrix[y][x];
                    boxes = bsc.getBoxStorage();

                    tot_value = 0;
                    for (Box box : boxes) {
                        tot_value = tot_value + box.getValue();
                    }
                    spaceship.setBoxValue(spaceship.getBoxValue() - tot_value);
                    break;

                default:
                    break;

            }

            spaceshipMatrix[y][x] = null;
            System.gc();   //call for garbage collector
            return true;
        }

        else
            return false;

    }
}


