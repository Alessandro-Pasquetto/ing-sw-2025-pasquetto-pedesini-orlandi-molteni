package org.progetto.server.model;

import java.util.*;
import org.progetto.server.model.components.*;
import org.progetto.server.model.loadClasses.MaskMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class BuildingBoard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Spaceship spaceship;
    private Component[][] spaceshipMatrix;  //composition of components
    private int[][] boardMask;              //mask layer for building clearance (0 = buildable, -1 = built, 1 = notBuildable)
    private final Component[] booked;    //list for booked components storage
    private Component handComponent;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(int levelShip, int color, Spaceship spaceship) {
        this.spaceship = spaceship;
        this.boardMask = loadBoardMask(levelShip);
        this.spaceshipMatrix = createSpaceshipMatrix();
        this.booked = new Component[2];
        this.handComponent = null;
        this.imgSrc = loadImgShip(levelShip);

        placeCentralUnit(levelShip, getImgSrcCentralUnitFromColor(color));
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

    public Component[] getBooked() {
        return booked;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // =======================
    // SETTERS
    // =======================

    public void setHandComponent(Component component) {
        handComponent = component;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Passing the player's color number it returns the imgSrc of the centralUnitComponent
     *
     * @author Alessandro
     * @param color the player's color
     * @return imgPathCentralUnit the imgPath of the central unit component with the player's color
     */
    public String getImgSrcCentralUnitFromColor(int color) {
        return switch (color) {
            case 0 -> "base-unit-blue.jpg";
            case 1 -> "base-unit-green.jpg";
            case 2 -> "base-unit-red.jpg";
            case 3 -> "base-unit-yellow.jpg";
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

    /**
     * Adds the centralUnit to the spaceship
     *
     * @author Alessandro
     * @param levelShip the ship's level
     * @param imgPathCentralUnit the imgPath of the central unit component with the player's color
     */
    private void placeCentralUnit(int levelShip, String imgPathCentralUnit) {
        switch (levelShip) {
            case 1:
                spaceshipMatrix[2][2] = new HousingUnit(ComponentType.CENTRAL_UNIT, new int[]{3,3,3,3}, imgPathCentralUnit, 2);
                break;
            case 2:
                spaceshipMatrix[2][3] = new HousingUnit(ComponentType.CENTRAL_UNIT, new int[]{3,3,3,3}, imgPathCentralUnit, 2);
                break;
        }
        // There is no need to increment the componentCount in the spaceship because it is set by default to 1
    }

    /**
     * Places the handComponent in the specified coordinates, with specified rotation
     *
     * @author Lorenzo
     * @param y coordinate for placing component
     * @param x coordinate for placing component
     * @param r rotation value of placing component
     * @return true if component has been placed correctly, otherwise false
     */
    public boolean placeComponent(int y, int x, int r) {
        if(boardMask[y][x] != 1)
            return false;

        // If it's not connected to at least one component returns false
        if((y == 0 || boardMask[y - 1][x] != -1) && (x == spaceshipMatrix[0].length - 1 || boardMask[y][x + 1] != -1) && (y == spaceshipMatrix.length - 1 || boardMask[y + 1][x] != -1) && (x == 0 || boardMask[y][x - 1] != -1)) {
            return false;
        }

        spaceship.addComponentsShipCount(1);

        handComponent.setY_coordinate(y);
        handComponent.setX_coordinate(x);
        handComponent.setRotation(r);

        spaceshipMatrix[y][x] = handComponent;

        boardMask[y][x] = -1;   //signal the presence of a component
        handComponent = null;
        return true;
    }

    /**
     * Move the handComponent to position idx in the booked array
     *
     * @author Alessandro
     * @param idx the index of the booking cell where I want to place the handComponent
     */
    public void setAsBooked(int idx) throws IllegalStateException {
        if(handComponent == null)
            throw new IllegalStateException("EmptyHandComponent");
        if(idx < 0 || idx > 1)
            throw new IllegalStateException("IllegalIndex");
        if(booked[idx] != null)
            throw new IllegalStateException("BookedCellOccupied");

        booked[idx] = handComponent;
        handComponent = null;
    }

    /**
     * Takes the component from the booked ones
     *
     * @author Alessandro
     * @param idx the index of the booked cell from which I want to take the component
     */
    public void pickBookedComponent(int idx) throws IllegalStateException{
        if(handComponent != null)
            throw new IllegalStateException("FullHandComponent");
        if(idx < 0 || idx > 1)
            throw new IllegalStateException("IllegalIndex");
        if(booked[idx] == null)
            throw new IllegalStateException("EmptyBookedCell");

        handComponent = booked[idx];
        booked[idx] = null;
    }

    /**
     * Check if two components are connected
     *
     * @author Lorenzo
     * @param c1 is the first component
     * @param c2 is the second component
     * @return true if c1 and c2 are connected
     */
    public boolean areConnected(Component c1, Component c2) {

        int[] c1_connections = c1.getConnections();
        int[] c2_connections = c2.getConnections();

        if (c1.getX_coordinate() == c2.getX_coordinate() + 1 && c1.getY_coordinate() == c2.getY_coordinate()) {  //c1 on the right
            if (c1_connections[3] != 0 && c2_connections[1] != 0) {
                if (c1_connections[3] == c2_connections[1] || c1_connections[3] == 3 || c2_connections[1] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX_coordinate() == c2.getX_coordinate() - 1 && c1.getY_coordinate() == c2.getY_coordinate()) {  //c1 on the left
            if (c1_connections[1] != 0 && c2_connections[3] != 0) {
                if (c1_connections[1] == c2_connections[3] || c1_connections[1] == 3 || c2_connections[3] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX_coordinate() == c2.getX_coordinate() && c1.getY_coordinate() == c2.getY_coordinate() - 1) {  //c1 above c2
            if (c1_connections[2] != 0 && c2_connections[0] != 0) {
                if (c1_connections[2] == c2_connections[0] || c1_connections[2] == 3 || c2_connections[0] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX_coordinate() == c2.getX_coordinate() && c1.getY_coordinate() == c2.getY_coordinate() + 1) {  //c1 under c2
            if (c1_connections[0] != 0 && c2_connections[2] != 0) {
                if (c1_connections[0] == c2_connections[2] || c1_connections[0] == 3 || c2_connections[2] == 3) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Search for component of the same type
     *
     * @author Lorenzo
     * @param type is the component type to search in the matrix
     * @return list of components found
     */
    private List<Component> typeSearch(ComponentType type) {

        ArrayList<Component> found_list = new ArrayList<>();

        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for(int x = 0; x < spaceshipMatrix[y].length; x++) {

                if(spaceshipMatrix[y][x] != null) {
                    if (spaceshipMatrix[y][x].getType() == type)
                        found_list.add(spaceshipMatrix[y][x]);
                }
            }
        }
        return found_list;
    }

    /**
     * Check if a housing unit can contain an orange alien, if it can't the alien is removed
     *
     * @author Lorenzo
     */
    private void updateOrangeAlienAllow(){
        List<Component> housing_components = typeSearch(ComponentType.HOUSING_UNIT);
        List<Component> orange_components = typeSearch(ComponentType.ORANGE_HOUSING_UNIT);
        boolean allow = false;
        for(Component housing_component : housing_components) {
            HousingUnit housingUnit = (HousingUnit) housing_component;
            for(Component orange_component : orange_components) {
                HousingUnit orangeUnit = (HousingUnit) orange_component;

                if(areConnected(orangeUnit, housingUnit)) {
                    allow = true;
                    break;
                }
            }

            housingUnit.setAllowAlienOrange(allow);

            if(!housingUnit.getAllowAlienOrange())
                housingUnit.setAlienOrange(false);
        }
    }


    /**
     * Check if a housing unit can contain a purple alien
     *
     * @author Lorenzo
     */
    private void updatePurpleAlienAllow(){
        List<Component> housing_components = typeSearch(ComponentType.HOUSING_UNIT);
        List<Component> purple_components = typeSearch(ComponentType.PURPLE_HOUSING_UNIT);
        boolean allow = false;
        for(Component housing_component : housing_components) {
            HousingUnit housingUnit = (HousingUnit) housing_component;
            for(Component purple_component : purple_components) {
                HousingUnit purpleUnit = (HousingUnit) purple_component;

                if(areConnected(purpleUnit, housingUnit)) {
                    allow = true;

                    break;
                }
            }

            housingUnit.setAllowAlienPurple(allow);
            if(!housingUnit.getAllowAlienPurple())
                housingUnit.setAlienPurple(false);

        }

    }

    /**
     * Loading mask for the building board
     *
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

    private String loadImgShip(int levelShip){
        return "spaceship" + levelShip + ".jpg";
    }

    /**
     * Create the component matrix given the mask matrix dimensions
     *
     * @author Lorenzo
     * @return the component matrix for the spaceship initialized with null values
     */
    private Component[][] createSpaceshipMatrix()
    {
        return new Component[boardMask.length][boardMask[0].length];
    }

    //controller will call destroy component for each event were a component needs to be removed, the actions sequence is the following
    // 1. remove the component from the spaceship matrix, update mask matrix
    // 2. update spaceship attributes
    // 3. update housing-unit alien hosting if an alien-unit has been removed
    // 4. check ship validity and update exposed connectors, controller will call automatically this one

    /**
     * Remove a component from the spaceship, update values
     *
     * @author Lorenzo
     * @param y is the y coordinate of the component to remove
     * @param x is the x coordinate of the component to remove
     * @return true if the component can be removed
     */
    public boolean destroyComponent(int y, int x) {
        if(boardMask[y][x] != -1)
            return false;

        spaceship.addComponentsShipCount(-1);
        spaceship.addDestroyedCount(1);
        Component destroyedComponent = spaceshipMatrix[y][x];
        boardMask[y][x] = 1;
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
                HousingUnit hu = (HousingUnit) destroyedComponent;
                if(hu.hasOrangeAlien())
                    spaceship.setAlienOrange(false);

                if(hu.hasPurpleAlien())
                    spaceship.setAlienPurple(false);

                spaceship.addCrewCount(-hu.getCrewCount());
                break;

            case ORANGE_HOUSING_UNIT:
                List<Component> orange_units = typeSearch(ComponentType.ORANGE_HOUSING_UNIT);
                if(orange_units.size() == 1) {    //if only a module is present
                    spaceship.setAlienOrange(false);
                }

                updateOrangeAlienAllow();

                break;

            case PURPLE_HOUSING_UNIT:
                List<Component> purple_units = typeSearch(ComponentType.PURPLE_HOUSING_UNIT);
                if(purple_units.size() == 1) {    //if only a module is present
                    spaceship.setAlienPurple(false);
                }

                updatePurpleAlienAllow();

                break;

            case CENTRAL_UNIT:
                hu = (HousingUnit) destroyedComponent;
                spaceship.addCrewCount(-hu.getCrewCount());
                break;

            case STRUCTURAL_UNIT:
                break;

            case BATTERY_STORAGE:
                BatteryStorage bs = (BatteryStorage) destroyedComponent;
                spaceship.addBatteriesCount(-bs.getItemsCount());
                break;

            case RED_BOX_STORAGE:
                BoxStorage bsc = (BoxStorage) destroyedComponent;
                Box[] boxes = bsc.getBoxStorage();

                for (Box box : boxes) {
                    if(box != null)
                        spaceship.addBoxCount(-1, box);
                }
                break;

            case BOX_STORAGE:
                bsc = (BoxStorage) destroyedComponent;
                boxes = bsc.getBoxStorage();

                for (Box box : boxes) {
                    if(box != null)
                        spaceship.addBoxCount(-1, box);
                }
                break;

            default:
                break;
        }

        System.gc();   // call for garbage collector
        return true;
    }


    /**
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param cannon the cannon component to checked
     * @return true if the cannon is well positioned, false otherwise
     */
    private boolean checkCannonValidity(Component cannon, int x, int y) {

        switch (cannon.getRotation()){
            case 0: // up
                if(y > 0 && spaceshipMatrix[y - 1][x] != null)
                    return false;
                break;
            case 1: // right
                if(x + 1 < spaceshipMatrix[0].length && spaceshipMatrix[y][x + 1] != null)
                    return false;
                break;
            case 2: // bottom
                if(y + 1 < spaceshipMatrix.length && spaceshipMatrix[y + 1][x] != null)
                    return false;
                break;
            case 3: // left
                if(x > 0 && spaceshipMatrix[y][x - 1] != null)
                    return false;
                break;
        }

        return true;
    }

    /**
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param visited the already visited component list
     * @param numComponentsChecked the num of the components checked
     * @param exposedConnectorsCount the num of the exposed connectors in the spaceship
     * @return true if the component is validly connected, false otherwise
     */
    private boolean dfsValidity(int x, int y, boolean[][] visited, AtomicInteger numComponentsChecked, AtomicInteger exposedConnectorsCount){
        numComponentsChecked.getAndIncrement();
        visited[y][x] = true;

        Component currentComponent = spaceshipMatrix[y][x];
        int currentRotation = currentComponent.getRotation();
        ComponentType currentType = currentComponent.getType();

        if (currentType == ComponentType.CANNON || currentType == ComponentType.DOUBLE_CANNON) {
            if (!checkCannonValidity(currentComponent, x, y))
                return false;
        } else if (currentType == ComponentType.ENGINE){
            if (currentRotation != 0 || (y > 0 && spaceshipMatrix[y - 1][x] != null))
                return false;
        }

        boolean up = false, right = false, bottom = false, left = false;

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && !visited[y - 1][x]){
            Component upComponent = spaceshipMatrix[y - 1][x];
            int upConnection = currentComponent.getConnections()[0];
            int relativeConnection = upComponent.getConnections()[2];
            if ((upConnection == 1 && relativeConnection == 2) || (upConnection == 2 && relativeConnection == 1))
                return false;
            if (upConnection != 0 && relativeConnection != 0)
                up = true;

        } else if (y == 0 || boardMask[y - 1][x] != -1) {
            if(currentComponent.getConnections()[0] != 0)
                exposedConnectorsCount.getAndIncrement();
        }
        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && !visited[y][x + 1]){
            Component rightComponent = spaceshipMatrix[y][x + 1];
            int rightConnection = currentComponent.getConnections()[1];
            int relativeConnection = rightComponent.getConnections()[3];
            if ((rightConnection == 1 && relativeConnection == 2) || (rightConnection == 2 && relativeConnection == 1))
                return false;
            if (rightConnection != 0 && relativeConnection != 0)
                right = true;

        } else if (x + 1 == spaceshipMatrix[0].length || boardMask[y][x + 1] != -1) {
            if(currentComponent.getConnections()[1] != 0)
                exposedConnectorsCount.getAndIncrement();
        }
        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && !visited[y + 1][x]){
            Component bottomComponent = spaceshipMatrix[y + 1][x];
            int bottomConnection = currentComponent.getConnections()[2];
            int relativeConnection = bottomComponent.getConnections()[0];
            if ((bottomConnection == 1 && relativeConnection == 2) || (bottomConnection == 2 && relativeConnection == 1))
                return false;
            if (bottomConnection != 0 && relativeConnection != 0)
                bottom = true;

        } else if (y + 1 == spaceshipMatrix.length || boardMask[y + 1][x] != -1) {
            if(currentComponent.getConnections()[2] != 0)
                exposedConnectorsCount.getAndIncrement();
        }
        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && !visited[y][x - 1]){
            Component leftComponent = spaceshipMatrix[y][x - 1];
            int leftConnection = currentComponent.getConnections()[3];
            int relativeConnection = leftComponent.getConnections()[1];
            if ((leftConnection == 1 && relativeConnection == 2) || (leftConnection == 2 && relativeConnection == 1))
                return false;
            if (leftConnection != 0 && relativeConnection != 0)
                left = true;

        } else if (x == 0 || boardMask[y][x - 1] != -1) {
            if(currentComponent.getConnections()[3] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        boolean result = true;
        if (up)
            result = dfsValidity(x, y - 1, visited, numComponentsChecked, exposedConnectorsCount);
        if (result && right)
            result = dfsValidity(x + 1, y, visited, numComponentsChecked, exposedConnectorsCount);
        if (result && bottom)
            result = dfsValidity(x, y + 1, visited, numComponentsChecked, exposedConnectorsCount);
        if (result && left)
            result = dfsValidity(x - 1, y, visited, numComponentsChecked, exposedConnectorsCount);

        return result;
    }

    //controller will call checkShipValidity at the end of the building phase and after a component has been removed,
    //this method is also able to identify if a spaceship has been divided, in this case the controller let the player decide witch stump keep by clicking
    // on a component of that stump


    /**
     * Check and return if the spaceship is valid and count and update the exposedConnectorsCount value of the spaceship
     *
     * @return true if the spaceship is valid, false otherwise
     */
    public boolean checkShipValidity() throws IllegalStateException{

        boolean[][] visited = new boolean[boardMask.length][boardMask[0].length];

        int xComponent = -1, yComponent = -1;

        for(int i = 0; i < spaceshipMatrix.length && xComponent == -1; i++) {
            for(int j = 0; j < spaceshipMatrix[i].length; j++) {
                if(spaceshipMatrix[i][j] != null){
                    yComponent = i;
                    xComponent = j;
                    break;
                }
            }
        }

        if(xComponent == -1) throw new IllegalStateException("Empty spaceship");

        AtomicInteger numComponentsChecked = new AtomicInteger(0);
        AtomicInteger exposedConnectorsCount = new AtomicInteger(0);

        boolean result = dfsValidity(xComponent, yComponent, visited, numComponentsChecked, exposedConnectorsCount) && numComponentsChecked.get() == spaceship.getShipComponentsCount();

        spaceship.setExposedConnectorsCount(exposedConnectorsCount.intValue());

        return result;
    }

    public void printBoard(){

        System.out.println();

        for (int i = 0; i < boardMask.length; i++) {
            System.out.println();
            for (int j = 0; j < boardMask[i].length; j++) {
                System.out.print(boardMask[i][j] + " ");
            }
        }
    }

    /**
     * Initialize spaceship attributes after checking the ship validity
     */
    public void initSpaceshipParams()
    {
        for(int y = 0; y < spaceshipMatrix.length; y++){
            for(int x = 0; x < spaceshipMatrix[y].length; x++){
                if(spaceshipMatrix[y][x] != null){

                    switch(spaceshipMatrix[y][x].getType()){

                        case CANNON:
                            if (spaceshipMatrix[y][x].getRotation() == 0)
                                spaceship.addNormalShootingPower(+1);
                            else
                                spaceship.addNormalShootingPower((float) +0.5);
                            break;

                        case DOUBLE_CANNON:
                            spaceship.addDoubleCannonCount(+1);
                            break;

                        case ENGINE:
                            spaceship.addNormalEnginePower(+1);
                            break;

                        case DOUBLE_ENGINE:
                            spaceship.addDoubleEngineCount(+1);
                            break;

                        case SHIELD:
                            switch (spaceshipMatrix[y][x].getRotation()) {

                                case 0: // left-up
                                    spaceship.addLeftUpShieldCount(1);
                                    break;

                                case 1: // up-right
                                    spaceship.addUpRightShieldCount(1);
                                    break;

                                case 2: // right-down
                                    spaceship.addRightDownShieldCount(1);
                                    break;

                                case 3: // down-left
                                    spaceship.addDownLeftShieldCount(1);
                                    break;
                            }
                            break;

                        case HOUSING_UNIT:
                            HousingUnit hu = (HousingUnit) spaceshipMatrix[y][x];
                            if(!hu.getAllowAlienPurple() && !hu.getAllowAlienOrange()) {
                                hu.incrementCrewCount(spaceship,2);
                            }
                            break;

                        case ORANGE_HOUSING_UNIT:
                            HousingUnit orange_unit = (HousingUnit) spaceshipMatrix[y][x];
                            List<Component> housing_components = typeSearch(ComponentType.HOUSING_UNIT);
                            HousingUnit housingUnit;
                            for(Component housing_component :  housing_components){
                                if(areConnected(orange_unit, housing_component)){
                                    housingUnit = (HousingUnit) housing_component;
                                    housingUnit.setAllowAlienOrange(true);
                                }
                            }
                            break;

                        case PURPLE_HOUSING_UNIT:
                            HousingUnit purple_unit = (HousingUnit) spaceshipMatrix[y][x];
                            housing_components = typeSearch(ComponentType.HOUSING_UNIT);
                            for(Component housing_component :  housing_components) {
                                if (areConnected(purple_unit, housing_component)) {
                                    housingUnit = (HousingUnit) housing_component;
                                    housingUnit.setAllowAlienPurple(true);
                                }
                            }
                            break;

                        case CENTRAL_UNIT:
                            hu = (HousingUnit) spaceshipMatrix[y][x];
                            hu.incrementCrewCount(spaceship,2);
                            break;

                        case BATTERY_STORAGE:
                            BatteryStorage bs = (BatteryStorage) spaceshipMatrix[y][x];
                            bs.incrementItemsCount(spaceship,bs.getCapacity());
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
}