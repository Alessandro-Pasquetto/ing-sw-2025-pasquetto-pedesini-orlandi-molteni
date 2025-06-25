package org.progetto.server.model;

import java.io.*;

import javafx.util.Pair;
import org.progetto.server.MainServer;
import org.progetto.server.model.components.*;
import org.progetto.server.model.loading.MaskMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicInteger;

public class BuildingBoard implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Spaceship spaceship;
    private final Component[][] spaceshipMatrix;  // composition of components
    private int[][] boardMask;                    // mask layer for building clearance (0 = buildable, -1 = built, 1 = notBuildable)
    private final Component[] booked;             // list for booked components storage
    private Component handComponent;
    private final String imgSrc;

    // =======================
    // CONSTRUCTORS
    // =======================

    public BuildingBoard(Spaceship spaceship, int color) {
        this.spaceship = spaceship;
        this.boardMask = loadBoardMask();
        this.spaceshipMatrix = createSpaceshipMatrix();
        this.booked = new Component[2];
        this.handComponent = null;
        this.imgSrc = loadImgShip();

        placeCentralUnit(getImgSrcCentralUnitFromColor(color));
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

    public Component[][] getSpaceshipMatrixCopy() {
        Component[][] SpaceshipMatrix = new Component[spaceshipMatrix.length][];
        for (int i = 0; i < spaceshipMatrix.length; i++) {
            SpaceshipMatrix[i] = spaceshipMatrix[i].clone();
        }

        return SpaceshipMatrix;
    }

    public int[][] getBoardMask() {
        return boardMask;
    }

    public Component[] getBookedCopy() {
        synchronized (booked) {
            Component[] bookedComponents = new Component[booked.length];

            for (int i = 0; i < booked.length; i++) {
                bookedComponents[i] = booked[i];
            }

            return bookedComponents;
        }
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
     * Returns the central unit of the spaceship
     *
     * @author Alessandro
     * @return the centralUnit component of the spaceship
     */
    public Component getCentralUnit() {
        return switch (spaceship.getLevelShip()) {
            case 1 -> spaceshipMatrix[2][2];
            case 2 -> spaceshipMatrix[2][3];
            default -> null;
        };
    }

    /**
     * Adds the centralUnit to the spaceship
     *
     * @author Alessandro
     * @param imgPathCentralUnit the imgPath of the central unit component with the player's color
     */
    private void placeCentralUnit(String imgPathCentralUnit) {
        switch (spaceship.getLevelShip()) {
            case 1:
                spaceshipMatrix[2][2] = new HousingUnit(ComponentType.CENTRAL_UNIT, new int[]{3, 3, 3, 3}, imgPathCentralUnit, 2);
                spaceshipMatrix[2][2].setY(2);
                spaceshipMatrix[2][2].setX(2);
                break;
            case 2:
                spaceshipMatrix[2][3] = new HousingUnit(ComponentType.CENTRAL_UNIT, new int[]{3, 3, 3, 3}, imgPathCentralUnit, 2);
                spaceshipMatrix[2][3].setY(2);
                spaceshipMatrix[2][3].setX(3);
                break;
        }
    }

    /**
     * Places the handComponent in the specified coordinates, with specified rotation
     *
     * @author Lorenzo
     * @param x coordinate for placing component
     * @param y coordinate for placing component
     * @param r rotation value of placing component
     */
    public void placeComponent(int x, int y, int r) {

        if(handComponent == null)
            throw new IllegalStateException("EmptyHandComponent");

        if(x < 0 || y < 0 || x >= boardMask[0].length || y >= boardMask.length)
            throw new IllegalStateException("NotValidCoordinates");

        if(boardMask[y][x] != 1)
            throw new IllegalStateException("NotAllowedToPlaceComponent");

        // If it's not connected to at least one component returns false
        if((y == 0 || boardMask[y - 1][x] != -1) && (x == spaceshipMatrix[0].length - 1 || boardMask[y][x + 1] != -1) && (y == spaceshipMatrix.length - 1 || boardMask[y + 1][x] != -1) && (x == 0 || boardMask[y][x - 1] != -1))
            throw new IllegalStateException("NotAllowedToPlaceComponent");


        spaceship.addComponentsShipCount(1);

        handComponent.setY(y);
        handComponent.setX(x);
        handComponent.setRotation(r);

        spaceshipMatrix[y][x] = handComponent;

        boardMask[y][x] = -1;   // signals the presence of a component
        handComponent = null;
    }

    /**
     * Try to place the hand component if possible
     *
     * @author Alessandro
     * @param x coordinate for placing component
     * @param y coordinate for placing component
     */
    public void tryToPlaceComponent(int x, int y) {

        if(handComponent == null)
            throw new IllegalStateException("EmptyHandComponent");

        if(x < 0 || y < 0 || x >= boardMask[0].length || y >= boardMask.length)
            throw new IllegalStateException("NotValidCoordinates");

        if(boardMask[y][x] != 1)
            throw new IllegalStateException("NotAllowedToPlaceComponent");

        // If it's not connected to at least one component returns false
        if((y == 0 || boardMask[y - 1][x] != -1) && (x == spaceshipMatrix[0].length - 1 || boardMask[y][x + 1] != -1) && (y == spaceshipMatrix.length - 1 || boardMask[y + 1][x] != -1) && (x == 0 || boardMask[y][x - 1] != -1))
            throw new IllegalStateException("NotAllowedToPlaceComponent");
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
            throw new IllegalStateException("IllegalBookIndex");
        if(booked[idx] != null)
            throw new IllegalStateException("BookedCellOccupied");

        booked[idx] = handComponent;
        booked[idx].setHasBeenBooked(true);
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

        int[] c1Connections = c1.getConnections();
        int[] c2Connections = c2.getConnections();

        if (c1.getX() == c2.getX() + 1 && c1.getY() == c2.getY()) {  //c1 on the right
            if (c1Connections[3] != 0 && c2Connections[1] != 0) {
                if (c1Connections[3] == c2Connections[1] || c1Connections[3] == 3 || c2Connections[1] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX() == c2.getX() - 1 && c1.getY() == c2.getY()) {  //c1 on the left
            if (c1Connections[1] != 0 && c2Connections[3] != 0) {
                if (c1Connections[1] == c2Connections[3] || c1Connections[1] == 3 || c2Connections[3] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX() == c2.getX() && c1.getY() == c2.getY() - 1) {  //c1 above c2
            if (c1Connections[2] != 0 && c2Connections[0] != 0) {
                if (c1Connections[2] == c2Connections[0] || c1Connections[2] == 3 || c2Connections[0] == 3) {
                    return true;
                }
            }
        }

        if (c1.getX() == c2.getX() && c1.getY() == c2.getY() + 1) {  //c1 under c2
            if (c1Connections[0] != 0 && c2Connections[2] != 0) {
                if (c1Connections[0] == c2Connections[2] || c1Connections[0] == 3 || c2Connections[2] == 3) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Loading mask for the building board
     *
     * @author Lorenzo
     * @return the loaded matrix configuration for the board
     */
    private int[][] loadBoardMask() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            try (InputStream inputStream = MainServer.class.getResourceAsStream("Masks.json")) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Masks.json not found in resources");
                }

                MaskMatrix data = objectMapper.readValue(inputStream, MaskMatrix.class);

                switch (spaceship.getLevelShip()) {
                    case 1 -> boardMask = data.getBaseMatrix();
                    case 2 -> boardMask = data.getAdvancedMatrix();
                }

                return boardMask;
            }

        } catch (IOException e) {
            throw new RuntimeException("Error loading board mask: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the spaceship's image
     *
     * @author Alessandro
     */
    private String loadImgShip(){
        return "spaceship" + spaceship.getLevelShip() + ".jpg";
    }

    /**
     * Create the component matrix given the mask matrix dimensions
     *
     * @author Lorenzo
     * @return the component matrix for the spaceship initialized with null values
     */
    private Component[][] createSpaceshipMatrix() {
        return new Component[boardMask.length][boardMask[0].length];
    }

    /**
     * Remove a component from the spaceship
     *
     * @author Alessandro
     * @param y is the y coordinate of the component to remove
     * @param x is the x coordinate of the component to remove
     */
    public void startDestroyComponent(int x, int y) throws IllegalStateException {
        if(boardMask[y][x] != -1)
            throw new IllegalStateException("EmptyComponentCell");

        spaceship.addComponentsShipCount(-1);

        spaceship.addDestroyedCount(1);
        boardMask[y][x] = 1;
        spaceshipMatrix[y][x] = null;
    }

    /**
     * Remove a component from the spaceship, update values
     *
     * @author Lorenzo
     * @param y is the y coordinate of the component to remove
     * @param x is the x coordinate of the component to remove
     */
    public void destroyComponent(int x, int y) throws IllegalStateException {
        if (boardMask[y][x] != -1)
           throw new IllegalStateException("EmptyComponentCell");

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
                if(destroyedComponent.getRotation() == 0)
                    spaceship.addFullDoubleCannonCount(-1);
                else
                    spaceship.addHalfDoubleCannonCount(-1);

                break;

            case ENGINE:
                spaceship.addNormalEnginePower(-1);
                break;

            case DOUBLE_ENGINE:
                spaceship.addDoubleEngineCount(-1);
                break;

            case SHIELD:
                switch (destroyedComponent.getRotation()) {

                    case 0: // up-right
                        spaceship.addUpRightShieldCount(-1);
                        break;

                    case 1: // right-down
                        spaceship.addRightDownShieldCount(-1);
                        break;

                    case 2: // down-left
                        spaceship.addDownLeftShieldCount(-1);
                        break;

                    case 3: // left-up
                        spaceship.addLeftUpShieldCount(-1);
                        break;
                }
                break;

            case HOUSING_UNIT:
                HousingUnit hu = (HousingUnit) destroyedComponent;

                if (hu.getHasOrangeAlien()) {
                    spaceship.addNormalEnginePower(-2);
                    spaceship.setAlienOrange(false);
                }

                if (hu.getHasPurpleAlien()) {
                    spaceship.addNormalShootingPower(-2);
                    spaceship.setAlienPurple(false);
                }

                spaceship.addCrewCount(-hu.getCrewCount());
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
                Box[] boxes = bsc.getBoxes();

                for (Box box : boxes) {
                    if (box != null)
                        spaceship.addBoxCount(-1, box);
                }
                break;

            case BOX_STORAGE:
                bsc = (BoxStorage) destroyedComponent;
                boxes = bsc.getBoxes();

                for (Box box : boxes) {
                    if (box != null)
                        spaceship.addBoxCount(-1, box);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Checks if a cannon is valid
     *
     * @author Alessandro
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param cannon the cannon component to checked
     * @return true if the cannon is well positioned, false otherwise
     */
    private boolean checkCannonValidity(Component cannon, int x, int y) {

        switch (cannon.getRotation()){
            case 0: // up
                if(y > 0 && spaceshipMatrix[y - 1][x] != null){
                    spaceshipMatrix[y - 1][x].setIncorrectlyPlaced(true);
                    return false;
                }
                break;

            case 1: // right
                if(x + 1 < spaceshipMatrix[0].length && spaceshipMatrix[y][x + 1] != null){
                    spaceshipMatrix[y][x + 1].setIncorrectlyPlaced(true);
                    return false;
                }
                break;

            case 2: // bottom
                if(y + 1 < spaceshipMatrix.length && spaceshipMatrix[y + 1][x] != null){
                    spaceshipMatrix[y + 1][x].setIncorrectlyPlaced(true);
                    return false;
                }
                break;

            case 3: // left
                if(x > 0 && spaceshipMatrix[y][x - 1] != null){
                    spaceshipMatrix[y][x - 1].setIncorrectlyPlaced(true);
                    return false;
                }
                break;
        }

        return true;
    }

    /**
     * DFS that checks the validity of the ship, cannot contain not adjacent components
     *
     * @author Alessandro
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param visited the already visited component list
     * @param numComponentsChecked the num of the components checked
     * @param exposedConnectorsCount the num of the exposed connectors in the spaceship
     * @return true if the component is validly connected, false otherwise
     */
    private boolean dfsStartValidity(int x, int y, boolean[][] visited, AtomicInteger numComponentsChecked, AtomicInteger exposedConnectorsCount){
         if (visited[y][x])
            return true;

        boolean correctlyPlaced = true;

        numComponentsChecked.getAndIncrement();
        visited[y][x] = true;

        Component currentComponent = spaceshipMatrix[y][x];
        int currentRotation = currentComponent.getRotation();
        ComponentType currentType = currentComponent.getType();

        int upConnection = currentComponent.getConnections()[0];
        int rightConnection = currentComponent.getConnections()[1];
        int bottomConnection = currentComponent.getConnections()[2];
        int leftConnection = currentComponent.getConnections()[3];

        if (currentType == ComponentType.CANNON || currentType == ComponentType.DOUBLE_CANNON) {
            if (!checkCannonValidity(currentComponent, x, y))
                correctlyPlaced = false;

        } else if (currentType == ComponentType.ENGINE || currentType == ComponentType.DOUBLE_ENGINE) {
            if (currentRotation != 0)
                correctlyPlaced = false;

            else if(y + 1 < spaceshipMatrix.length && spaceshipMatrix[y + 1][x] != null){
                spaceshipMatrix[y + 1][x].setIncorrectlyPlaced(true);
                correctlyPlaced = false;
            }
        }

        boolean up = false, right = false, bottom = false, left = false;

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && !visited[y - 1][x]){
            Component upComponent = spaceshipMatrix[y - 1][x];
            int relativeConnection = upComponent.getConnections()[2];

            if ((upConnection == 1 && relativeConnection == 2) || (upConnection == 2 && relativeConnection == 1) || (upConnection == 0 && relativeConnection != 0) || (upConnection != 0 && relativeConnection == 0)){
                upComponent.setIncorrectlyPlaced(true);
                correctlyPlaced = false;
            }
            else if (upConnection != 0)
                up = true;

        } else if (y == 0 || boardMask[y - 1][x] != -1) {
            if(currentComponent.getConnections()[0] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && !visited[y][x + 1]){
            Component rightComponent = spaceshipMatrix[y][x + 1];
            int relativeConnection = rightComponent.getConnections()[3];

            if ((rightConnection == 1 && relativeConnection == 2) || (rightConnection == 2 && relativeConnection == 1) || (rightConnection == 0 && relativeConnection != 0) || (rightConnection != 0 && relativeConnection == 0)){
                rightComponent.setIncorrectlyPlaced(true);
                correctlyPlaced = false;
            }
            else if (rightConnection != 0)
                right = true;

        } else if (x + 1 == spaceshipMatrix[0].length || boardMask[y][x + 1] != -1) {
            if(currentComponent.getConnections()[1] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && !visited[y + 1][x]){
            Component bottomComponent = spaceshipMatrix[y + 1][x];
            int relativeConnection = bottomComponent.getConnections()[0];

            if ((bottomConnection == 1 && relativeConnection == 2) || (bottomConnection == 2 && relativeConnection == 1) || (bottomConnection == 0 && relativeConnection != 0) || (bottomConnection != 0 && relativeConnection == 0)){
                bottomComponent.setIncorrectlyPlaced(true);
                correctlyPlaced = false;
            }
            else if (bottomConnection != 0)
                bottom = true;

        } else if (y + 1 == spaceshipMatrix.length || boardMask[y + 1][x] != -1) {
            if(currentComponent.getConnections()[2] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && !visited[y][x - 1]){
            Component leftComponent = spaceshipMatrix[y][x - 1];
            int relativeConnection = leftComponent.getConnections()[1];

            if ((leftConnection == 1 && relativeConnection == 2) || (leftConnection == 2 && relativeConnection == 1) || (leftConnection == 0 && relativeConnection != 0) || (leftConnection != 0 && relativeConnection == 0)){
                leftComponent.setIncorrectlyPlaced(true);
                correctlyPlaced = false;
            }
            else if (leftConnection != 0)
                left = true;

        } else if (x == 0 || boardMask[y][x - 1] != -1) {
            if(currentComponent.getConnections()[3] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        if(!correctlyPlaced)
            currentComponent.setIncorrectlyPlaced(true);

        boolean resultUp = true;
        boolean resultRight = true;
        boolean resultDown = true;
        boolean resultLeft = true;

        if (up)
            resultUp = dfsStartValidity(x, y - 1, visited, numComponentsChecked, exposedConnectorsCount);
        if (right)
            resultRight = dfsStartValidity(x + 1, y, visited, numComponentsChecked, exposedConnectorsCount);
        if (bottom)
            resultDown = dfsStartValidity(x, y + 1, visited, numComponentsChecked, exposedConnectorsCount);
        if (left)
            resultLeft = dfsStartValidity(x - 1, y, visited, numComponentsChecked, exposedConnectorsCount);

        return resultUp && resultRight && resultDown && resultLeft && correctlyPlaced;
    }

    /**
     * Checks if the spaceship is valid, also counting and updating the exposedConnectorsCount value of the spaceship, visits the entire spaceship and set badlyPlaced if the component is placed incorrectly
     *
     * @author Alessandro
     * @return a pair of booleans: the first indicates if the spaceship is valid, the second indicates if there were any disconnected components
     */
    public Pair<Boolean, Boolean> checkStartShipValidity(){
        boolean disconnectedComponents = false;

        // Check if all components are connected
        boolean[][] visited = new boolean[boardMask.length][boardMask[0].length];
        AtomicInteger numComponentsChecked = new AtomicInteger(0);
        AtomicInteger exposedConnectorsCount = new AtomicInteger(0);

        dfsValidity(getCentralUnit().getX(), getCentralUnit().getY(), visited, numComponentsChecked, exposedConnectorsCount);

        if(numComponentsChecked.get() != spaceship.getShipComponentsCount()){
            startDeleteDisconnectedComponents(visited);
            disconnectedComponents = true;
        }

        // Check components validity
        visited = new boolean[boardMask.length][boardMask[0].length];
        numComponentsChecked = new AtomicInteger(0);
        exposedConnectorsCount = new AtomicInteger(0);

        resetIncorrectlyPlacedComponents();
        boolean result = dfsStartValidity(getCentralUnit().getX(), getCentralUnit().getY(), visited, numComponentsChecked, exposedConnectorsCount) && numComponentsChecked.get() == spaceship.getShipComponentsCount();

        spaceship.setExposedConnectorsCount(exposedConnectorsCount.get());

        return new Pair<>(result, disconnectedComponents);
    }

    /**
     * DFS that checks for disconnected components
     *
     * @author Alessandro
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param visited the already visited component list
     * @param numComponentsChecked the num of the components checked
     * @param exposedConnectorsCount the num of the exposed connectors in the spaceship
     */
    private void dfsValidity(int x, int y, boolean[][] visited, AtomicInteger numComponentsChecked, AtomicInteger exposedConnectorsCount){

        if(visited[y][x])
            return;

        numComponentsChecked.getAndIncrement();
        visited[y][x] = true;

        Component currentComponent = spaceshipMatrix[y][x];

        int upConnection = currentComponent.getConnections()[0];
        int rightConnection = currentComponent.getConnections()[1];
        int bottomConnection = currentComponent.getConnections()[2];
        int leftConnection = currentComponent.getConnections()[3];

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && !visited[y - 1][x]){
            Component upComponent = spaceshipMatrix[y - 1][x];
            int relativeConnection = upComponent.getConnections()[2];

            if ((upConnection != 0 && relativeConnection != 0) && (upConnection == 3 || relativeConnection == 3 || upConnection == relativeConnection))
                dfsValidity(x, y - 1, visited, numComponentsChecked, exposedConnectorsCount);

        } else if (y == 0 || boardMask[y - 1][x] != -1) {
            if(upConnection != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && !visited[y][x + 1]){
            Component rightComponent = spaceshipMatrix[y][x + 1];
            int relativeConnection = rightComponent.getConnections()[3];

            if ((rightConnection != 0 && relativeConnection != 0) && (rightConnection == 3 || relativeConnection == 3 || rightConnection == relativeConnection))
                dfsValidity(x + 1, y, visited, numComponentsChecked, exposedConnectorsCount);

        } else if (x + 1 == spaceshipMatrix[0].length || boardMask[y][x + 1] != -1) {
            if(rightConnection != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && !visited[y + 1][x]){
            Component bottomComponent = spaceshipMatrix[y + 1][x];
            int relativeConnection = bottomComponent.getConnections()[0];

            if ((bottomConnection != 0 && relativeConnection != 0) && (bottomConnection == 3 || relativeConnection == 3 || bottomConnection == relativeConnection))
                dfsValidity(x, y + 1, visited, numComponentsChecked, exposedConnectorsCount);

        } else if (y + 1 == spaceshipMatrix.length || boardMask[y + 1][x] != -1) {
            if(bottomConnection != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && !visited[y][x - 1]){
            Component leftComponent = spaceshipMatrix[y][x - 1];
            int relativeConnection = leftComponent.getConnections()[1];

            if ((leftConnection != 0 && relativeConnection != 0) && (leftConnection == 3 || relativeConnection == 3 || leftConnection == relativeConnection))
                dfsValidity(x - 1, y, visited, numComponentsChecked, exposedConnectorsCount);

        } else if (x == 0 || boardMask[y][x - 1] != -1) {
            if(leftConnection != 0)
                exposedConnectorsCount.getAndIncrement();
        }
    }

    /**
     * Delete disconnected components and updating the spaceship's counters
     *
     * @author Alessandro
     * @param visited the already visited component list
     */
    private void deleteDisconnectedComponents(boolean[][] visited){
        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for(int x = 0; x < spaceshipMatrix[y].length; x++) {

                if (spaceshipMatrix[y][x] == null)
                    continue;

                if(!visited[y][x]) {
                    destroyComponent(x, y);
                }
            }
        }
    }

    /**
     * Delete disconnected components
     *
     * @author Alessandro
     * @param visited the already visited component list
     */
    private void startDeleteDisconnectedComponents(boolean[][] visited){
        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for(int x = 0; x < spaceshipMatrix[y].length; x++) {

                if (spaceshipMatrix[y][x] == null)
                    continue;

                if(!visited[y][x]) {
                    startDestroyComponent(x, y);
                }
            }
        }
    }

    /**
     * Check if all components are connected.
     * If there are disconnected components, remove them automatically if possible, otherwise return false.
     * In any case, fix the alien presence.
     *
     * @author Alessandro
     * @return doesNotRequirePlayerAction: false if the spaceship needs playerAction, true otherwise
     */
    public boolean checkShipValidityAndFixAliens() {

        boolean[][] visited = new boolean[boardMask.length][boardMask[0].length];

        AtomicInteger numComponentsChecked = new AtomicInteger(0);
        AtomicInteger exposedConnectorsCount = new AtomicInteger(0);

        int xComponent = -1, yComponent = -1;
        Component centralUnit = getCentralUnit();

        if (centralUnit != null) {
            xComponent = centralUnit.getX();
            yComponent = centralUnit.getY();

        } else {

            for(int y = 0; y < spaceshipMatrix.length && xComponent == -1; y++) {
                for(int x = 0; x < spaceshipMatrix[y].length; x++) {
                    if(spaceshipMatrix[y][x] != null){
                        xComponent = x;
                        yComponent = y;
                        break;
                    }
                }
            }

            if (xComponent == -1) return true; // Empty spaceship
        }

        dfsValidity(xComponent, yComponent, visited, numComponentsChecked, exposedConnectorsCount);

        if (numComponentsChecked.get() != spaceship.getShipComponentsCount())
            return false;

        spaceship.setExposedConnectorsCount(exposedConnectorsCount.get());
        fixAlienPresence();

        return true;
    }

    /**
     * Fixes the alien presence in the housing unit if it cannot contain them
     *
     * @author Alessandro
     */
    private void fixAlienPresence(){
        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {

                Component component = spaceshipMatrix[y][x];

                if(component == null)
                    continue;

                if(component.getType() == ComponentType.HOUSING_UNIT){

                    HousingUnit hu = (HousingUnit) component;

                    if(!checkAndSetAllowPurpleAlien(hu)){
                        if(hu.getHasPurpleAlien()){
                            hu.setAlienPurple(false);
                            hu.incrementCrewCount(spaceship, -1);
                            spaceship.setAlienPurple(false);
                            spaceship.addNormalShootingPower(-2);
                        }
                    }

                    if(!checkAndSetAllowOrangeAlien(hu)){
                        if(hu.getHasOrangeAlien()){
                            hu.setAlienOrange(false);
                            hu.incrementCrewCount(spaceship, -1);
                            spaceship.setAlienOrange(false);
                            spaceship.addNormalEnginePower(-2);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if in the housingUnit is allowed to put an purpleAlien
     *
     * @author Alessandro
     * @param hu the housingUnit under examination
     * @return true if it is allowed
     */
    private boolean checkAndSetAllowPurpleAlien(HousingUnit hu){

        int x = hu.getX();
        int y = hu.getY();

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && spaceshipMatrix[y - 1][x].getType() == ComponentType.PURPLE_HOUSING_UNIT){

            int upConnection = hu.getConnections()[0];
            int relativeConnection = spaceshipMatrix[y - 1][x].getConnections()[2];

            if ((upConnection != 0 && relativeConnection != 0) && (upConnection == 3 || relativeConnection == 3 || upConnection == relativeConnection)){
                hu.setAllowPurpleAlien(true);
                return true;
            }
        }

        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && spaceshipMatrix[y][x + 1].getType() == ComponentType.PURPLE_HOUSING_UNIT){

            int rightConnection = hu.getConnections()[1];
            int relativeConnection = spaceshipMatrix[y][x + 1].getConnections()[3];

            if((rightConnection != 0 && relativeConnection != 0) && (rightConnection == 3 || relativeConnection == 3 || rightConnection == relativeConnection)){
                hu.setAllowPurpleAlien(true);
                return true;
            }
        }

        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && spaceshipMatrix[y + 1][x].getType() == ComponentType.PURPLE_HOUSING_UNIT){

            int bottomConnection = hu.getConnections()[2];
            int relativeConnection = spaceshipMatrix[y + 1][x].getConnections()[0];

            if((bottomConnection != 0 && relativeConnection != 0) && (bottomConnection == 3 || relativeConnection == 3 || bottomConnection == relativeConnection)){
                hu.setAllowPurpleAlien(true);
                return true;
            }
        }

        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && spaceshipMatrix[y][x - 1].getType() == ComponentType.PURPLE_HOUSING_UNIT){

            int leftConnection = hu.getConnections()[3];
            int relativeConnection = spaceshipMatrix[y][x - 1].getConnections()[1];

            if((leftConnection != 0 && relativeConnection != 0) && (leftConnection == 3 || relativeConnection == 3 || leftConnection == relativeConnection)){
                hu.setAllowPurpleAlien(true);
                return true;
            }
        }

        hu.setAllowPurpleAlien(false);
        return false;
    }

    /**
     * Checks if in the housingUnit is allowed to put an orangeAlien
     *
     * @author Alessandro
     * @param hu the housingUnit under examination
     * @return true if it is allowed
     */
    private boolean checkAndSetAllowOrangeAlien(HousingUnit hu){

        int x = hu.getX();
        int y = hu.getY();

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && spaceshipMatrix[y - 1][x].getType() == ComponentType.ORANGE_HOUSING_UNIT){

            int upConnection = hu.getConnections()[0];
            int relativeConnection = spaceshipMatrix[y - 1][x].getConnections()[2];

            if ((upConnection != 0 && relativeConnection != 0) && (upConnection == 3 || relativeConnection == 3 || upConnection == relativeConnection)){
                hu.setAllowOrangeAlien(true);
                return true;
            }
        }

        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && spaceshipMatrix[y][x + 1].getType() == ComponentType.ORANGE_HOUSING_UNIT){

            int rightConnection = hu.getConnections()[1];
            int relativeConnection = spaceshipMatrix[y][x + 1].getConnections()[3];

            if((rightConnection != 0 && relativeConnection != 0) && (rightConnection == 3 || relativeConnection == 3 || rightConnection == relativeConnection)){
                hu.setAllowOrangeAlien(true);
                return true;
            }
        }

        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && spaceshipMatrix[y + 1][x].getType() == ComponentType.ORANGE_HOUSING_UNIT){

            int bottomConnection = hu.getConnections()[2];
            int relativeConnection = spaceshipMatrix[y + 1][x].getConnections()[0];

            if((bottomConnection != 0 && relativeConnection != 0) && (bottomConnection == 3 || relativeConnection == 3 || bottomConnection == relativeConnection)){
                hu.setAllowOrangeAlien(true);
                return true;
            }
        }

        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && spaceshipMatrix[y][x - 1].getType() == ComponentType.ORANGE_HOUSING_UNIT){

            int leftConnection = hu.getConnections()[3];
            int relativeConnection = spaceshipMatrix[y][x - 1].getConnections()[1];

            if((leftConnection != 0 && relativeConnection != 0) && (leftConnection == 3 || relativeConnection == 3 || leftConnection == relativeConnection)){
                hu.setAllowOrangeAlien(true);
                return true;
            }
        }

        hu.setAllowOrangeAlien(false);
        return false;
    }

    /**
     * Initializes spaceship attributes after checking the ship validity
     *
     * @author Alessandro, Lorenzo
     * @return true if player action isn't needed; false otherwise
     */
    public boolean initSpaceshipParams() {

        boolean isAutoPopulated = true;

        for(int y = 0; y < spaceshipMatrix.length; y++){
            for(int x = 0; x < spaceshipMatrix[y].length; x++){

                Component component = spaceshipMatrix[y][x];

                if(component == null)
                    continue;

                switch(component.getType()){

                    case CANNON:
                        if (component.getRotation() == 0)
                            spaceship.addNormalShootingPower(+1);
                        else
                            spaceship.addNormalShootingPower((float) +0.5);
                        break;

                    case DOUBLE_CANNON:
                        if(component.getRotation() == 0)
                            spaceship.addFullDoubleCannonCount(+1);
                        else
                            spaceship.addHalfDoubleCannonCount(+1);
                        break;

                    case ENGINE:
                        spaceship.addNormalEnginePower(+1);
                        break;

                    case DOUBLE_ENGINE:
                        spaceship.addDoubleEngineCount(+1);
                        break;

                    case SHIELD:
                        switch (component.getRotation()) {

                            case 0: // up-right
                                spaceship.addUpRightShieldCount(1);
                                break;

                            case 1: // right-down
                                spaceship.addRightDownShieldCount(1);
                                break;

                            case 2: // down-left
                                spaceship.addDownLeftShieldCount(1);
                                break;

                            case 3: // left-up
                                spaceship.addLeftUpShieldCount(1);
                                break;
                        }
                        break;

                    case HOUSING_UNIT:
                        HousingUnit hu = (HousingUnit) component;

                        boolean allowPurpleAlien = checkAndSetAllowPurpleAlien(hu);
                        boolean allowOrangeAlien = checkAndSetAllowOrangeAlien(hu);

                        if(spaceship.getLevelShip() != 1 && (allowPurpleAlien || allowOrangeAlien))
                            isAutoPopulated = false;
                        else
                            hu.incrementCrewCount(spaceship, 2);

                        break;

                    case CENTRAL_UNIT:
                        hu = (HousingUnit) component;
                        hu.incrementCrewCount(spaceship, 2);
                        break;

                    case BATTERY_STORAGE:
                        BatteryStorage bs = (BatteryStorage) component;
                        bs.incrementItemsCount(spaceship, bs.getCapacity());
                        break;

                    default:
                        break;
                }
            }
        }

        return isAutoPopulated;
    }

    /**
     * Removes booked components
     *
     * @author Alessandro
     */
    public void removeBookedComponents() {

        for (int i = 0; i < 2; i++) {
            if(booked[i] == null)
                continue;

            booked[i] = null;
            spaceship.addDestroyedCount(1);
        }
    }

    /**
     * DFS that explores spaceship part from a xy coordinates of a component
     *
     * @author Alessandro
     * @param x the x coordinate of the component to checked
     * @param y the y coordinate of the component to checked
     * @param visited the already visited component list
     * @param exposedConnectorsCount the num of the exposed connectors in the spaceship
     */
    private void dfsKeepSpaceshipPart(int x, int y, boolean[][] visited, AtomicInteger exposedConnectorsCount){

        if(visited[y][x])
            return;

        visited[y][x] = true;

        Component currentComponent = spaceshipMatrix[y][x];

        // up
        if (y > 0 && boardMask[y - 1][x] == -1 && !visited[y - 1][x]){
            Component upComponent = spaceshipMatrix[y - 1][x];
            int upConnection = currentComponent.getConnections()[0];
            int relativeConnection = upComponent.getConnections()[2];
            if ((upConnection != 0 && relativeConnection != 0) && (upConnection == 3 || relativeConnection == 3 || upConnection == relativeConnection))
                dfsKeepSpaceshipPart(x, y - 1, visited, exposedConnectorsCount);

        } else if (y == 0 || boardMask[y - 1][x] != -1) {
            if(currentComponent.getConnections()[0] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // right
        if (x + 1 < spaceshipMatrix[0].length && boardMask[y][x + 1] == -1 && !visited[y][x + 1]){
            Component rightComponent = spaceshipMatrix[y][x + 1];
            int rightConnection = currentComponent.getConnections()[1];
            int relativeConnection = rightComponent.getConnections()[3];
            if ((rightConnection != 0 && relativeConnection != 0) && (rightConnection == 3 || relativeConnection == 3 || rightConnection == relativeConnection))
                dfsKeepSpaceshipPart(x + 1, y, visited, exposedConnectorsCount);

        } else if (x + 1 == spaceshipMatrix[0].length || boardMask[y][x + 1] != -1) {
            if(currentComponent.getConnections()[1] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // bottom
        if (y + 1 < spaceshipMatrix.length && boardMask[y + 1][x] == -1 && !visited[y + 1][x]){
            Component bottomComponent = spaceshipMatrix[y + 1][x];
            int bottomConnection = currentComponent.getConnections()[2];
            int relativeConnection = bottomComponent.getConnections()[0];
            if ((bottomConnection != 0 && relativeConnection != 0) && (bottomConnection == 3 || relativeConnection == 3 || bottomConnection == relativeConnection))
                dfsKeepSpaceshipPart(x, y + 1, visited, exposedConnectorsCount);

        } else if (y + 1 == spaceshipMatrix.length || boardMask[y + 1][x] != -1) {
            if(currentComponent.getConnections()[2] != 0)
                exposedConnectorsCount.getAndIncrement();
        }

        // left
        if (x > 0 && boardMask[y][x - 1] == -1 && !visited[y][x - 1]){
            Component leftComponent = spaceshipMatrix[y][x - 1];
            int leftConnection = currentComponent.getConnections()[3];
            int relativeConnection = leftComponent.getConnections()[1];
            if ((leftConnection != 0 && relativeConnection != 0) && (leftConnection == 3 || relativeConnection == 3 || leftConnection == relativeConnection))
                dfsKeepSpaceshipPart(x - 1, y, visited, exposedConnectorsCount);

        } else if (x == 0 || boardMask[y][x - 1] != -1) {
            if(currentComponent.getConnections()[3] != 0)
                exposedConnectorsCount.getAndIncrement();
        }
    }

    /**
     * Keep a spaceship part and update exposed components
     *
     * @author Alessandro
     * @param xComponent coordinate
     * @param yComponent coordinate
     */
    public void keepSpaceshipPart(int xComponent, int yComponent) throws IllegalStateException{

        if(xComponent < 0 || xComponent >= boardMask[0].length || yComponent < 0 || yComponent >= boardMask.length)
            throw new IllegalStateException("NotValidCoordinates");

        boolean[][] visited = new boolean[boardMask.length][boardMask[0].length];

        AtomicInteger exposedConnectorsCount = new AtomicInteger(0);

        dfsKeepSpaceshipPart(xComponent, yComponent, visited, exposedConnectorsCount);

        spaceship.setExposedConnectorsCount(exposedConnectorsCount.intValue());
        deleteDisconnectedComponents(visited);
    }

    /**
     * Handles player decision on how to populate a specific housing unit
     *
     * @author Alessandro
     * @param alienColor
     * @param xComponent
     * @param yComponent
     * @throws IllegalStateException
     */
    public void placeAlienComponent(String alienColor, int xComponent, int yComponent) throws IllegalStateException{

        if(xComponent < 0 || yComponent < 0 || xComponent >= boardMask[0].length || yComponent >= boardMask.length)
            throw new IllegalStateException("InvalidCoordinates");

        Component component = spaceshipMatrix[yComponent][xComponent];

        if(component == null || component.getType() != ComponentType.HOUSING_UNIT)
            throw new IllegalStateException("InvalidComponent");

        HousingUnit hu = (HousingUnit) component;

        if(hu.getCrewCount() > 0 || hu.getHasPurpleAlien() || hu.getHasOrangeAlien())
            throw new IllegalStateException("ComponentAlreadyOccupied");

        switch (alienColor.toLowerCase()) {
            case "orange":
                if (hu.getAllowOrangeAlien()) {

                    if(getSpaceship().getAlienOrange())
                        throw new IllegalStateException("OrangeAlienAlreadyPlaced");

                    getSpaceship().setAlienOrange(true);

                    hu.setAlienOrange(true);
                    hu.setAllowPurpleAlien(false);
                    hu.incrementCrewCount(spaceship, 1);
                    spaceship.addNormalEnginePower(2);
                } else {
                    throw new IllegalStateException("CannotContainOrangeAlien");
                }
                break;

            case "purple":
                if (hu.getAllowPurpleAlien()) {

                    if(getSpaceship().getAlienPurple())
                        throw new IllegalStateException("PurpleAlienAlreadyPlaced");

                    getSpaceship().setAlienPurple(true);

                    hu.setAlienPurple(true);
                    hu.setAllowOrangeAlien(false);
                    hu.incrementCrewCount(spaceship, 1);
                    spaceship.addNormalShootingPower(2);
                } else {
                    throw new IllegalStateException("CannotContainPurpleAlien");
                }
                break;

            default:
                throw new IllegalStateException("UnknownCrewType");
        }
    }

    /**
     * Fills all housing units that doesn't need a choice with humans
     *
     * @author Alessandro
     */
    public void fillHumans(){
        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {

                Component component = spaceshipMatrix[y][x];

                if(component == null)
                    continue;

                if(component instanceof HousingUnit hu && hu.getCrewCount() == 0)
                    hu.incrementCrewCount(spaceship, 2);
            }
        }
    }


    /**
     * Reset a component that has not been correctly placed
     *
     * @author Alessandro
     */
    private void resetIncorrectlyPlacedComponents() {
        for(int y = 0; y < spaceshipMatrix.length; y++) {
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {
                Component component = spaceshipMatrix[y][x];

                if(component != null)
                    component.setIncorrectlyPlaced(false);
            }
        }
    }
}