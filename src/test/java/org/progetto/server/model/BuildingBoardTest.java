package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import static org.junit.jupiter.api.Assertions.*;
class BuildingBoardTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSpaceship() {
        // Create necessary objects for testing
        Spaceship spaceship = new Spaceship(1, 0);
        BuildingBoard board = new BuildingBoard(1, 0, spaceship);

        // Test that getSpaceship returns the same spaceship instance
        assertSame(spaceship, board.getSpaceship());

        // Test with different spaceship
        Spaceship spaceship2 = new Spaceship(1, 1);
        BuildingBoard board2 = new BuildingBoard(1, 1, spaceship2);
        assertSame(spaceship2, board2.getSpaceship());
    }

    @Test
    void getHandComponent() {
        // Create board with initial null hand component
        BuildingBoard board = new BuildingBoard(1, 0, new Spaceship(1, 0));

        // Test initial state (should be null)
        assertNull(board.getHandComponent());

        // Create and set a component
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "imgSrc", 2);
        board.setHandComponent(component);

        // Test that getHandComponent returns component
        assertSame(component, board.getHandComponent());
    }

    @Test
    void getSpaceshipMatrix() {
        // Create board for level 1
        BuildingBoard board = new BuildingBoard(1, 0, new Spaceship(1, 0));

        Component[][] matrix = board.getSpaceshipMatrix();

        // Test matrix is not null
        assertNotNull(matrix);

        // Test matrix dimensions
        assertEquals(5, matrix.length);
        assertEquals(5, matrix[0].length);

        // Test that central unit is placed
        assertNotNull(matrix[2][2]);
        assertEquals(ComponentType.CENTRAL_UNIT, matrix[2][2].getType());
    }

    @Test
    void getBoardMask() {
        Spaceship spaceship = new Spaceship(1, 0);
        BuildingBoard buildingboard = spaceship.getBuildingBoard();

        int[][] mat = buildingboard.getBoardMask();

//        for (int i = 0; i < mat.length; i++) {
//            System.out.println();
//            for (int j = 0; j < mat[i].length; j++) {
//                System.out.print(mat[i][j] + " ");
//            }
//        }
    }

    @Test
    void getBooked() {
        BuildingBoard board = new BuildingBoard(1, 0, new Spaceship(1, 0));

        // Test initial state
        Component[] booked = board.getBooked();
        assertNotNull(booked);
        assertEquals(2, booked.length);

        // Test all slots are initially null
        assertNull(booked[0]);
        assertNull(booked[1]);
    }

    @Test
    void getImgSrc() {
        // Test for level 1
        BuildingBoard board1 = new BuildingBoard(1, 0, new Spaceship(1, 0));
        assertEquals("spaceship1.jpg", board1.getImgSrc());

        // Test for level 2
        BuildingBoard board2 = new BuildingBoard(2, 0, new Spaceship(2, 0));
        assertEquals("spaceship2.jpg", board2.getImgSrc());
    }

    @Test
    void setAsBooked() {
        BuildingBoard board = new BuildingBoard(1, 0, new Spaceship(1, 0));
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "imgSrc", 2);

        // Test without hand component
        assertThrows(IllegalStateException.class, () -> board.setAsBooked(0));

        // Set hand component
        board.setHandComponent(component);

        // Test illegal indices
        assertThrows(IllegalStateException.class, () -> board.setAsBooked(-1));
        assertThrows(IllegalStateException.class, () -> board.setAsBooked(2));  // Changed from 3 to 2

        // Now it should work (overwriting the pre-existing component)
        assertDoesNotThrow(() -> board.setAsBooked(0));

        // Verify component was moved correctly
        assertNull(board.getHandComponent());
        assertSame(component, board.getBooked()[0]);
    }

    @Test
    void setHandComponent() {
        BuildingBoard board = new BuildingBoard(1, 0, new Spaceship(1, 0));

        // Test setting null component
        board.setHandComponent(null);
        assertNull(board.getHandComponent());

        // Test setting a component
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "imgSrc", 2);
        board.setHandComponent(component);
        assertSame(component, board.getHandComponent());

        // Test overwriting existing hand component
        Component component2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "imgSrc", 2);
        board.setHandComponent(component2);
        assertSame(component2, board.getHandComponent());
    }

    @Test
    void placeComponent() {
        Spaceship spaceship = new Spaceship(1, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        Component c;

        boolean result; // function return value

        // Placed in a correct place
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(1, 2, 0);

        assertEquals(c, spaceshipMatrix[1][2]);
        assertTrue(result);

        // Placed outside the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(0, 0, 0);

        assertNull(spaceshipMatrix[0][0]);
        assertFalse(result);

        // Placed in the center cell of the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(2, 2, 0);

        assertNotEquals(c, spaceshipMatrix[2][2]);
        assertFalse(result);
    }

    @Test
    void destroyComponent() {

        Spaceship spaceship = new Spaceship(2, 0);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // place housing_unit //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",2));   //housing unit
        Component housing_unit = buildingBoard.getHandComponent();
        HousingUnit housingUnit = (HousingUnit) housing_unit;
        buildingBoard.placeComponent(2, 2, 0);

        buildingBoard.initSpaceshipParams();



        // test allow orange alien //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.initSpaceshipParams();
        assertTrue(housingUnit.getAllowAlienOrange());

        //  update orange alien hosting test   //
        buildingBoard.destroyComponent(2,1); // Removes the orange alien unit, so it can't host alien anymore
        assertFalse(housingUnit.getAllowAlienOrange());

        // update orange alien hosting test with another orange_unit //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(1, 2, 0);

        buildingBoard.initSpaceshipParams();
        buildingBoard.destroyComponent(1,2); // Removes the orange alien unit, another unit is present
        assertTrue(housingUnit.getAllowAlienOrange());

        // reset spaceship //
        buildingBoard.destroyComponent(2,1);



        // test allow purple alien //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.initSpaceshipParams();
        assertTrue(housingUnit.getAllowAlienPurple());


        //  update purple alien hosting test   //
        buildingBoard.destroyComponent(2,1); // Removes the purple alien unit, so it can't host alien anymore
        assertFalse(housingUnit.getAllowAlienPurple());

        // update purple alien hosting test with another orange_unit //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(1, 2, 0);

        buildingBoard.initSpaceshipParams();
        buildingBoard.destroyComponent(1,2); // Removes the purple alien unit, another unit is present
        assertTrue(housingUnit.getAllowAlienPurple());




        //  Connectors count update test    //
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",0));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.checkShipValidity();                                //update connectors count
        assertEquals(8,spaceship.getExposedConnectorsCount());

        buildingBoard.destroyComponent(2,1);                        //remove component and check new connectors count
        buildingBoard.checkShipValidity();
        assertEquals(6,spaceship.getExposedConnectorsCount());
    }

    @Test
    void areConnected() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // No Connectors - Triple Connector
        Component c1 = new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath");
        Component c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 3}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // Single Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 1, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Double Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 2, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // No Connectors - No Connectors
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 0}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // Double Connector - Double Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 2, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 2}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 3}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Double Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 2}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX_coordinate(0);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector, c1 on the right
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX_coordinate(2);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(0);

        assertTrue(buildingBoard.areConnected(c1, c2));


        // Triple Connector - Triple Connector, c1 on top
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX_coordinate(1);
        c1.setY_coordinate(0);
        c2.setX_coordinate(1);
        c2.setY_coordinate(1);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector, c1 under
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX_coordinate(1);
        c1.setY_coordinate(2);
        c2.setX_coordinate(1);
        c2.setY_coordinate(1);

        assertTrue(buildingBoard.areConnected(c1, c2));

    }

    @Test
    void checkShipValidity() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);

//        for (int i = 0; i < 4; i++)
//            System.out.print(buildingBoard.getHandComponent().getConnections()[i] + " ");

        buildingBoard.placeComponent(1, 3, 0);


        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(1, 2, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(1, 1, 1);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(1, 4, 1);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(1, 5, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 2);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        buildingBoard.placeComponent(3, 2, 0);


        int[][] mask = buildingBoard.getBoardMask();

//        for (int i = 0; i < mask.length; i++) {
//            System.out.println();
//            for (int j = 0; j < mask[i].length; j++) {
//                System.out.printf("%-5s", mask[i][j] + " ");
//            }
//        }
//        System.out.println();

//        for (int i = 0; i < spaceshipMatrix.length; i++) {
//            System.out.println();
//            for (int j = 0; j < spaceshipMatrix[0].length; j++) {
//                String value = (spaceshipMatrix[i][j] == null) ? "NULL" : spaceshipMatrix[i][j].getType().toString() + "-" + spaceshipMatrix[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//
//        System.out.println();
//        System.out.println();
//
//        System.out.println(buildingBoard.checkShipValidity());

        assertFalse(buildingBoard.checkShipValidity());
    }

    @Test
    void printBoard() {
    }

    @Test
    void initSpaceshipParams(){
        Spaceship spaceship = new Spaceship(2,1);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // Cannons (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 3, 0);
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 4, 1);

        // Engine
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 0);

        // DoubleEngine
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 4, 0);

        // Shields (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 5, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 2, 3);

        // BatteryStorage
        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(3, 2, 0);

        // DoubleCannon
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 1, 0);

        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-20s", 5 + i);
        }
        for (int i = 0; i < buildingBoard.getSpaceshipMatrix().length; i++) {
               System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < buildingBoard.getSpaceshipMatrix()[0].length; j++) {
                String value = (buildingBoard.getSpaceshipMatrix()[i][j] == null) ? "NULL" : buildingBoard.getSpaceshipMatrix()[i][j].getType().toString() + "-" + buildingBoard.getSpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();

        assertTrue(buildingBoard.checkShipValidity());

        buildingBoard.initSpaceshipParams();
        assertEquals(1.5, spaceship.getNormalShootingPower());
        assertEquals(1, spaceship.getDoubleCannonCount());
        assertEquals(1, spaceship.getNormalEnginePower());
        assertEquals(1, spaceship.getDoubleEngineCount());
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(3));
        assertEquals(2, spaceship.getBatteriesCount());
    }
}