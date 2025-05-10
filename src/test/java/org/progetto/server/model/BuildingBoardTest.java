package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.*;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;
class BuildingBoardTest {

    @BeforeEach
    void setUp() {
    }

    private void printBoard(BuildingBoard buildingBoard){
        System.out.println();
        System.out.printf("%-20s", "-");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%-20s", 5 + i);
        }
        for (int i = 0; i < buildingBoard.getCopySpaceshipMatrix().length; i++) {
            System.out.println();
            System.out.printf("%-20s", 5 + i);
            for (int j = 0; j < buildingBoard.getCopySpaceshipMatrix()[0].length; j++) {
                String value = (buildingBoard.getCopySpaceshipMatrix()[i][j] == null) ? "NULL" : buildingBoard.getCopySpaceshipMatrix()[i][j].getType().toString() + "-" + buildingBoard.getCopySpaceshipMatrix()[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }
        System.out.println();
    }

    @Test
    void getSpaceship() {
        // Create necessary objects for testing
        Spaceship spaceship = new Spaceship(1, 0);
        BuildingBoard board = new BuildingBoard(spaceship, 0);

        // Test that getSpaceship returns the same spaceship instance
        assertSame(spaceship, board.getSpaceship());

        // Test with different spaceship
        Spaceship spaceship2 = new Spaceship(1, 1);
        BuildingBoard board2 = new BuildingBoard(spaceship2, 1);
        assertSame(spaceship2, board2.getSpaceship());
    }

    @Test
    void getHandComponent() {
        // Create board with initial null hand component
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 1);

        // Test initial state (should be null)
        assertNull(board.getHandComponent());

        // Create and set a component
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);
        board.setHandComponent(component);

        // Test that getHandComponent returns component
        assertSame(component, board.getHandComponent());
    }

    @Test
    void getCopySpaceshipMatrix() {
        // Create board for level 1
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);

        Component[][] matrix = board.getCopySpaceshipMatrix();

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
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);

        // Test initial state
        Component[] booked = board.getBookedCopy();
        assertNotNull(booked);
        assertEquals(2, booked.length);

        // Test all slots are initially null
        assertNull(booked[0]);
        assertNull(booked[1]);
    }

    @Test
    void getBookedCopy() {
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);

        // Test initial state
        Component[] booked = board.getBookedCopy();
        assertNotNull(booked);
        assertEquals(2, booked.length);

        // Test all slots are initially null
        assertNull(booked[0]);
        assertNull(booked[1]);

        Component component1 = new Component(ComponentType.ENGINE, new int[]{1, 1, 1, 1}, "imgSrc");
        board.setHandComponent(component1);
        board.setAsBooked(0);
        Component component2 = new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgSrc");
        board.setHandComponent(component2);
        board.setAsBooked(1);
        Component[] expected = {component1, component2};
        assertArrayEquals(expected, board.getBookedCopy());
    }

    @Test
    void getImgSrc() {
        // Test for level 1
        BuildingBoard board1 = new BuildingBoard(new Spaceship(1, 0), 0);
        assertEquals("spaceship1.jpg", board1.getImgSrc());

        // Test for level 2
        BuildingBoard board2 = new BuildingBoard(new Spaceship(2, 0), 0);
        assertEquals("spaceship2.jpg", board2.getImgSrc());
    }

    @Test
    void setAsBooked() {
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);

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
        assertSame(component, board.getBookedCopy()[0]);

        //Test cell occupied
        board.setHandComponent(component);
        assertThrows(IllegalStateException.class, () -> board.setAsBooked(0));
    }

    @Test
    void pickBookedComponent(){
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);

        // Test empty booked list
        assertThrows(IllegalStateException.class, () -> board.pickBookedComponent(0));

        //set booked component
        board.setHandComponent(component);
        board.setAsBooked(0);

        // Test illegal indices
        assertThrows(IllegalStateException.class, () -> board.pickBookedComponent(-1));
        assertThrows(IllegalStateException.class, () -> board.pickBookedComponent(2));

        //Test not empty hand
        board.setHandComponent(component);
        assertThrows(IllegalStateException.class, () -> board.pickBookedComponent(0));


        // Verify component was moved correctly
        board.setHandComponent(null);
        board.pickBookedComponent(0);
        assertSame(component, board.getHandComponent());
        assertNull(board.getBookedCopy()[0]);
    }


    @Test
    void setHandComponent() {
        BuildingBoard board = new BuildingBoard(new Spaceship(1, 0), 0);

        // Test setting null component
        board.setHandComponent(null);
        assertNull(board.getHandComponent());

        // Test setting a component
        Component component = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);
        board.setHandComponent(component);
        assertSame(component, board.getHandComponent());

        // Test overwriting existing hand component
        Component component2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgSrc", 2);
        board.setHandComponent(component2);
        assertSame(component2, board.getHandComponent());
    }

    @Test
    void placeComponent(){
        Spaceship spaceship = new Spaceship(1, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getCopySpaceshipMatrix();

        Component c;

        // Placed in a correct place
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));

        buildingBoard.placeComponent(2, 1, 0);

        c = buildingBoard.getHandComponent();

        assertEquals(c, spaceshipMatrix[1][2]);

        // Placed outside the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeComponent(0, 0, 0);
        });
        assertEquals("NotAllowedToPlaceComponent", exception.getMessage());

        assertNull(spaceshipMatrix[0][0]);

        // Placed in the center cell of the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeComponent(2, 2, 0);
        });
        assertEquals("NotAllowedToPlaceComponent", exception.getMessage());

        assertNotEquals(c, spaceshipMatrix[2][2]);

        //Placed away from a placed component
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeComponent(3, 4, 0);
        });
        assertEquals("NotAllowedToPlaceComponent", exception.getMessage());
    }

    @Test
    void tryToPlaceComponent(){
        Spaceship spaceship = new Spaceship(2, 0);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // test EmptyHandComponent exception
        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.tryToPlaceComponent(0, 0);
        });
        assertEquals("EmptyHandComponent", exception1.getMessage());

        // test NotValidCoordinates exception
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard.setHandComponent(housingUnit);
        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.tryToPlaceComponent(-1, 2);
        });
        assertEquals("NotValidCoordinates", exception2.getMessage());
        IllegalStateException exception3 = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.tryToPlaceComponent(2, -1);
        });
        assertEquals("NotValidCoordinates", exception3.getMessage());

        // test NotAllowedToPlaceComponent exception
        HousingUnit housingUnit2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard.setHandComponent(housingUnit2);
        buildingBoard.placeComponent(2, 2, 0);
        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        IllegalStateException exception4 = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.tryToPlaceComponent(2, 2);
        });
        assertEquals("NotAllowedToPlaceComponent", exception4.getMessage());

        // test if it's not connected to at least one component
        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        IllegalStateException exception5 = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.tryToPlaceComponent(2, 0);
        });
        assertEquals("NotAllowedToPlaceComponent", exception5.getMessage());
    }

    @Test
    void destroyComponent() {
        Spaceship spaceship = new Spaceship(2, 0);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // place housing_unit //
        HousingUnit housingUnit = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard.setHandComponent(housingUnit);
        buildingBoard.placeComponent(2, 2, 0);

        // test allow orange alien //
        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 2, 0);

        buildingBoard.initSpaceshipParams();
        assertTrue(housingUnit.getAllowOrangeAlien());

        //  update orange alien hosting test   //
        buildingBoard.destroyComponent(1, 2); // Removes the orange alien unit, so it can't host alien anymore
        assertTrue(buildingBoard.checkShipValidityAndTryToFix());
        assertFalse(housingUnit.getAllowOrangeAlien());

        // update orange alien hosting test with another orange_unit //
        Spaceship spaceship1 = new Spaceship(2, 0);
        BuildingBoard buildingBoard1 = spaceship1.getBuildingBoard();

        buildingBoard1.setHandComponent(housingUnit);
        buildingBoard1.placeComponent(2, 2, 0);

        buildingBoard1.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard1.placeComponent(1, 2, 0);

        buildingBoard1.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard1.placeComponent(2, 1, 0);

        buildingBoard1.initSpaceshipParams();
        buildingBoard1.destroyComponent(2, 1);

        assertTrue(buildingBoard1.checkShipValidityAndTryToFix());// Removes the orange alien unit, another unit is present
        assertTrue(housingUnit.getAllowOrangeAlien());

        // reset spaceship
        buildingBoard1.destroyComponent(1, 2);
        assertTrue(buildingBoard1.checkShipValidityAndTryToFix());


        // test allow purple alien
        Spaceship spaceship2 = new Spaceship(2, 0);
        BuildingBoard buildingBoard2 = spaceship2.getBuildingBoard();

        buildingBoard2.setHandComponent(housingUnit);
        buildingBoard2.placeComponent(2, 2, 0);

        buildingBoard2.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard2.placeComponent(1, 2, 0);

        buildingBoard2.initSpaceshipParams();
        assertTrue(housingUnit.getAllowPurpleAlien());

        //  update purple alien hosting test   //
        buildingBoard2.destroyComponent(1, 2);
        assertTrue(buildingBoard2.checkShipValidityAndTryToFix());// Removes the purple alien unit, so it can't host alien anymore
        assertFalse(housingUnit.getAllowPurpleAlien());

        // update purple alien hosting test with another orange_unit
        Spaceship spaceship3 = new Spaceship(2, 0);
        BuildingBoard buildingBoard3 = spaceship3.getBuildingBoard();

        buildingBoard3.setHandComponent(housingUnit);
        buildingBoard3.placeComponent(2, 2, 0);

        buildingBoard3.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard3.placeComponent(1, 2, 0);

        buildingBoard3.setHandComponent(new HousingUnit(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard3.placeComponent(2, 1, 0);

        buildingBoard3.initSpaceshipParams();
        buildingBoard3.destroyComponent(2, 1);
        assertTrue(buildingBoard3.checkShipValidityAndTryToFix());// Removes the purple alien unit, another unit is present
        assertTrue(housingUnit.getAllowPurpleAlien());

        // reset spaceship //
        buildingBoard3.destroyComponent(1, 2);
        assertTrue(buildingBoard3.checkShipValidityAndTryToFix());


        // Removing frontal cannon //
        Spaceship spaceship4 = new Spaceship(2, 0);
        BuildingBoard buildingBoard4 = spaceship4.getBuildingBoard();

        buildingBoard4.setHandComponent(housingUnit);
        buildingBoard4.placeComponent(2, 2, 0);

        buildingBoard4.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard4.placeComponent(1, 2, 0);

        buildingBoard4.initSpaceshipParams();
        assertEquals(1, spaceship4.getNormalShootingPower());

        buildingBoard4.destroyComponent(1, 2);
        assertTrue(buildingBoard4.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship4.getNormalShootingPower());

        // Removing tilted cannon //
        Spaceship spaceship5 = new Spaceship(2, 0);
        BuildingBoard buildingBoard5 = spaceship5.getBuildingBoard();

        buildingBoard5.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard5.placeComponent(2, 2, 0);

        buildingBoard5.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard5.placeComponent(1, 2, 1);

        buildingBoard5.initSpaceshipParams();
        assertEquals(0.5, spaceship5.getNormalShootingPower());

        buildingBoard5.destroyComponent(1, 2);
        assertTrue(buildingBoard5.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship5.getNormalShootingPower());


        // Removing double cannon //
        Spaceship spaceship6 = new Spaceship(2, 0);
        BuildingBoard buildingBoard6 = spaceship6.getBuildingBoard();

        buildingBoard6.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard6.placeComponent(2, 2, 0);

        buildingBoard6.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard6.placeComponent(1, 2, 0);

        buildingBoard6.initSpaceshipParams();
        assertEquals(1, spaceship6.getFullDoubleCannonCount());

        buildingBoard6.destroyComponent(1, 2);
        assertTrue(buildingBoard6.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship6.getFullDoubleCannonCount());


        // Removing engine //
        Spaceship spaceship7 = new Spaceship(2, 0);
        BuildingBoard buildingBoard7 = spaceship7.getBuildingBoard();

        buildingBoard7.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard7.placeComponent(2, 2, 0);

        buildingBoard7.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard7.placeComponent(1, 2, 0);

        buildingBoard7.initSpaceshipParams();
        assertEquals(1, spaceship7.getNormalEnginePower());

        buildingBoard7.destroyComponent(1, 2);
        assertTrue(buildingBoard7.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship7.getNormalEnginePower());

        // Removing double engine //
        Spaceship spaceship8 = new Spaceship(2, 0);
        BuildingBoard buildingBoard8 = spaceship8.getBuildingBoard();

        buildingBoard8.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard8.placeComponent(2, 2, 0);

        buildingBoard8.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard8.placeComponent(1, 2, 0);

        buildingBoard8.initSpaceshipParams();
        assertEquals(1, spaceship8.getDoubleEngineCount());

        buildingBoard8.destroyComponent(1, 2);
        assertTrue(buildingBoard8.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship8.getDoubleEngineCount());


        // Removing left-up shield //
        Spaceship spaceship9 = new Spaceship(2, 0);
        BuildingBoard buildingBoard9 = spaceship9.getBuildingBoard();

        buildingBoard9.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard9.placeComponent(2, 2, 0);

        buildingBoard9.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard9.placeComponent(1, 2, 0);

        buildingBoard9.initSpaceshipParams();
        assertEquals(1, spaceship9.getIdxShieldCount(0));
        assertEquals(1, spaceship9.getIdxShieldCount(1));

        buildingBoard9.destroyComponent(1, 2);
        assertTrue(buildingBoard9.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship9.getIdxShieldCount(0));
        assertEquals(0, spaceship9.getIdxShieldCount(1));


        // Removing up-right shield //
        Spaceship spaceship10 = new Spaceship(2, 0);
        BuildingBoard buildingBoard10 = spaceship10.getBuildingBoard();

        buildingBoard10.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard10.placeComponent(2, 2, 0);

        buildingBoard10.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard10.placeComponent(1, 2, 1);

        buildingBoard10.initSpaceshipParams();
        assertEquals(1, spaceship10.getIdxShieldCount(1));
        assertEquals(1, spaceship10.getIdxShieldCount(2));

        buildingBoard10.destroyComponent(1, 2);
        assertTrue(buildingBoard10.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship10.getIdxShieldCount(1));
        assertEquals(0, spaceship10.getIdxShieldCount(2));


        // Removing right-down shield //
        Spaceship spaceship11 = new Spaceship(2, 0);
        BuildingBoard buildingBoard11 = spaceship11.getBuildingBoard();

        buildingBoard11.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard11.placeComponent(2, 2, 0);

        buildingBoard11.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard11.placeComponent(1, 2, 2);

        buildingBoard11.initSpaceshipParams();
        assertEquals(1, spaceship11.getIdxShieldCount(2));
        assertEquals(1, spaceship11.getIdxShieldCount(3));

        buildingBoard11.destroyComponent(1, 2);
        assertTrue(buildingBoard11.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship11.getIdxShieldCount(2));
        assertEquals(0, spaceship11.getIdxShieldCount(3));


        // Removing down-left shield //
        Spaceship spaceship12 = new Spaceship(2, 0);
        BuildingBoard buildingBoard12 = spaceship12.getBuildingBoard();

        buildingBoard12.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard12.placeComponent(2, 2, 0);

        buildingBoard12.setHandComponent(new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard12.placeComponent(1, 2, 3);

        buildingBoard12.initSpaceshipParams();
        assertEquals(1, spaceship12.getIdxShieldCount(0));
        assertEquals(1, spaceship12.getIdxShieldCount(3));

        buildingBoard12.destroyComponent(1, 2);
        assertTrue(buildingBoard12.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship12.getIdxShieldCount(0));
        assertEquals(0, spaceship12.getIdxShieldCount(3));

        // removing housing unit with alien orange
        Spaceship spaceship13 = new Spaceship(2, 0);
        BuildingBoard buildingBoard13 = spaceship13.getBuildingBoard();

        buildingBoard13.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard13.placeComponent(2, 2, 0);

        housingUnit.setAlienOrange(true);
        buildingBoard13.destroyComponent(2, 2);
        assertTrue(buildingBoard13.checkShipValidityAndTryToFix());
        assertFalse(spaceship13.getAlienOrange());

        // removing housing unit with alien purple
        Spaceship spaceship14 = new Spaceship(2, 0);
        BuildingBoard buildingBoard14 = spaceship14.getBuildingBoard();

        buildingBoard14.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard14.placeComponent(2, 2, 0);

        housingUnit.setAlienPurple(true);
        buildingBoard14.destroyComponent(2, 2);
        assertTrue(buildingBoard14.checkShipValidityAndTryToFix());
        assertFalse(spaceship14.getAlienPurple());


        // removing structural unit
        Spaceship spaceship15 = new Spaceship(2, 0);
        BuildingBoard buildingBoard15 = spaceship15.getBuildingBoard();

        buildingBoard15.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard15.placeComponent(2, 2, 0);

        buildingBoard15.initSpaceshipParams();
        assertDoesNotThrow(() -> buildingBoard15.destroyComponent(2, 2));
        assertTrue(buildingBoard.checkShipValidityAndTryToFix());

        // removing battery storage
        Spaceship spaceship16 = new Spaceship(2, 0);
        BuildingBoard buildingBoard16 = spaceship16.getBuildingBoard();

        buildingBoard16.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard16.placeComponent(2, 2, 0);

        buildingBoard16.initSpaceshipParams();
        assertEquals(2, spaceship16.getBatteriesCount());

        buildingBoard16.destroyComponent(2, 2);
        assertTrue(buildingBoard16.checkShipValidityAndTryToFix());

        assertEquals(0, spaceship16.getBatteriesCount());


        // removing box storage
        Spaceship spaceship17 = new Spaceship(2, 0);
        BuildingBoard buildingBoard17 = spaceship17.getBuildingBoard();

        BoxStorage boxStorage = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard17.setHandComponent(boxStorage);
        buildingBoard17.placeComponent(2, 2, 0);

        boxStorage.addBox(spaceship17, Box.YELLOW, 0);
        buildingBoard17.initSpaceshipParams();

        assertArrayEquals(new int[]{0, 1, 0, 0}, spaceship17.getBoxCounts());

        buildingBoard17.destroyComponent(2, 2);
        assertTrue(buildingBoard17.checkShipValidityAndTryToFix());
        assertArrayEquals(new int[]{0, 0, 0, 0}, spaceship17.getBoxCounts());

        // removing redBox storage //
        Spaceship spaceship18 = new Spaceship(2, 0);
        BuildingBoard buildingBoard18 = spaceship18.getBuildingBoard();

        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard18.setHandComponent(boxStorage1);
        buildingBoard18.placeComponent(2, 2, 0);
        boxStorage1.addBox(spaceship18, Box.RED, 0);
        buildingBoard18.initSpaceshipParams();

        assertArrayEquals(new int[]{1, 0, 0, 0}, spaceship18.getBoxCounts());

        buildingBoard18.destroyComponent(2, 2);
        assertTrue(buildingBoard18.checkShipValidityAndTryToFix());
        assertArrayEquals(new int[]{0, 0, 0, 0}, spaceship18.getBoxCounts());

        //  Connectors count update test
        Spaceship spaceship19 = new Spaceship(2, 0);
        BuildingBoard buildingBoard19 = spaceship19.getBuildingBoard();

        buildingBoard19.setHandComponent(new HousingUnit(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 0));
        buildingBoard19.placeComponent(2, 2, 0);

        // update connectors count
        buildingBoard19.checkStartShipValidity();
        assertEquals(6, spaceship19.getExposedConnectorsCount());

        // remove component and check new connectors count
        buildingBoard19.destroyComponent(2, 2);
        buildingBoard19.checkStartShipValidity();
        assertEquals(4, spaceship19.getExposedConnectorsCount());

        // destroy null component
        assertThrows(IllegalStateException.class, () -> buildingBoard19.destroyComponent(3, 4));
        assertTrue(buildingBoard19.checkShipValidityAndTryToFix());
    }

    @Test
    void areConnected() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // No Connectors - Triple Connector
        Component c1 = new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath");
        Component c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 3}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // Single Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 1, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Double Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 2, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // No Connectors - No Connectors
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 0}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertFalse(buildingBoard.areConnected(c1, c2));

        // Double Connector - Double Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 2, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 2}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 3}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Double Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 2}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Single Connector
        c1 = new Component(ComponentType.SHIELD, new int[]{2, 3, 1, 1}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{2, 1, 1, 1}, "imgPath");

        c1.setX(0);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector, c1 on the right
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX(2);
        c1.setY(0);
        c2.setX(1);
        c2.setY(0);

        assertTrue(buildingBoard.areConnected(c1, c2));


        // Triple Connector - Triple Connector, c1 on top
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX(1);
        c1.setY(0);
        c2.setX(1);
        c2.setY(1);

        assertTrue(buildingBoard.areConnected(c1, c2));

        // Triple Connector - Triple Connector, c1 under
        c1 = new Component(ComponentType.SHIELD, new int[]{3, 3, 3, 3}, "imgPath");
        c2 = new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath");

        c1.setX(1);
        c1.setY(2);
        c2.setX(1);
        c2.setY(1);

        assertTrue(buildingBoard.areConnected(c1, c2));

    }

    @Test
    void checkStartShipValidity() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));

        buildingBoard.placeComponent(3, 1, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(1, 1, 1);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(4, 1, 1);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(5, 1, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 2);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(2, 3, 0);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{2, 1, 1, 1}, "imgPath", 3));
        buildingBoard.placeComponent(2, 2, 0);

        //printBoard(buildingBoard);

        assertFalse(buildingBoard.checkStartShipValidity().getKey());

        // Case cannon (up) not valid
        Spaceship spaceship1 = new Spaceship(2, 1);

        BuildingBoard buildingBoard1 = spaceship1.getBuildingBoard();

        buildingBoard1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard1.placeComponent(4, 2, 0);

        buildingBoard1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard1.placeComponent(4, 1, 0);

        //printBoard(buildingBoard1);

        assertFalse(buildingBoard1.checkStartShipValidity().getKey());

        // Case cannon (right) not valid
        Spaceship spaceship2 = new Spaceship(2, 1);

        BuildingBoard buildingBoard2 = spaceship2.getBuildingBoard();

        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(4, 2, 1);
        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(5, 2, 1);

        //printBoard(buildingBoard2);

        assertFalse(buildingBoard2.checkStartShipValidity().getKey());

        // Case cannon (down) not valid
        Spaceship spaceship3 = new Spaceship(2, 1);

        BuildingBoard buildingBoard3 = spaceship3.getBuildingBoard();

        buildingBoard3.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard3.placeComponent(4, 2, 2);
        buildingBoard3.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard3.placeComponent(4, 3, 2);

        //printBoard(buildingBoard3);

        assertFalse(buildingBoard3.checkStartShipValidity().getKey());

        // Case cannon (left) not valid
        Spaceship spaceship4 = new Spaceship(2, 1);

        BuildingBoard buildingBoard4 = spaceship4.getBuildingBoard();

        buildingBoard4.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard4.placeComponent(2, 2, 3);
        buildingBoard4.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard4.placeComponent(1, 2, 3);

        //printBoard(buildingBoard4);

        assertFalse(buildingBoard4.checkStartShipValidity().getKey());

        // Case incorrect connection
        Spaceship spaceship5 = new Spaceship(2, 1);
        BuildingBoard buildingBoard5 = spaceship5.getBuildingBoard();

        buildingBoard5.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard5.placeComponent(2, 2, 1);

        //printBoard(buildingBoard5);

        assertFalse(buildingBoard5.checkStartShipValidity().getKey());

        Spaceship spaceship6 = new Spaceship(2, 1);
        BuildingBoard buildingBoard6 = spaceship6.getBuildingBoard();

        buildingBoard6.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard6.placeComponent(2, 2, 0);

        //printBoard(buildingBoard6);

        assertTrue(buildingBoard6.checkStartShipValidity().getKey());
    }

    @Test
    void initSpaceshipParams(){
        Spaceship spaceship = new Spaceship(2, 1);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // Cannons (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 1, 0);
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(4, 1, 1);

        // Engine
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 0);

        // DoubleEngine
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(4, 3, 0);

        // Shields (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(5, 3, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 3);

        // BatteryStorage
        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(2, 3, 0);

        // DoubleCannon
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 3, 0);

        //printBoard(buildingBoard);

        assertTrue(buildingBoard.checkStartShipValidity().getKey());

        buildingBoard.initSpaceshipParams();
        assertEquals(1.5, spaceship.getNormalShootingPower());
        assertEquals(1, spaceship.getFullDoubleCannonCount());
        assertEquals(1, spaceship.getNormalEnginePower());
        assertEquals(1, spaceship.getDoubleEngineCount());
        assertEquals(1, spaceship.getIdxShieldCount(1));
        assertEquals(1, spaceship.getIdxShieldCount(2));
        assertEquals(1, spaceship.getIdxShieldCount(0));
        assertEquals(1, spaceship.getIdxShieldCount(3));
        assertEquals(2, spaceship.getBatteriesCount());
    }

    @Test
    void checkShipValidityAndTryToFix(){

        // TEST DISCONNECTED COMPONENTS

        Spaceship spaceship = new Spaceship(2, 1);
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // Cannon
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 1, 0);

        // HousingUnit
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(4, 1, 1);

        // Engine
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 0);

        // DoubleEngine
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(4, 3, 0);

        // Shields (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(5, 3, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 3);

        // BatteryStorage
        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(2, 3, 0);

        // DoubleCannon
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 3, 0);

        assertTrue(buildingBoard.initSpaceshipParams());

        assertEquals(4, spaceship.getCrewCount());

        buildingBoard.destroyComponent( 3, 1);

        assertTrue(buildingBoard.checkShipValidityAndTryToFix());

        assertEquals(2, spaceship.getCrewCount());


        // TEST ALIENS

        Spaceship spaceship2 = new Spaceship(2, 1);
        BuildingBoard buildingBoard2 = spaceship2.getBuildingBoard();


        // HousingUnit
        HousingUnit hu = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        buildingBoard2.setHandComponent(hu);
        buildingBoard2.placeComponent(3, 1, 1);

        // PurpleAlien
        buildingBoard2.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(4, 1, 0);

        // Engine
        buildingBoard2.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard2.placeComponent(3, 3, 0);

        // DoubleEngine
        buildingBoard2.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard2.placeComponent(4, 3, 0);

        // Shields (x2)
        buildingBoard2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(5, 3, 1);

        buildingBoard2.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(2, 1, 3);

        // BatteryStorage
        buildingBoard2.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard2.placeComponent(2, 3, 0);

        // DoubleCannon
        buildingBoard2.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(1, 3, 0);


        //printBoard(buildingBoard2);

        assertFalse(buildingBoard2.initSpaceshipParams());

        assertEquals(2, spaceship2.getCrewCount());

        hu.setAlienPurple(true);
        spaceship2.addCrewCount(1);

        assertTrue(hu.getHasPurpleAlien());

        buildingBoard2.destroyComponent(4, 1);

        assertTrue(buildingBoard2.checkShipValidityAndTryToFix());
        assertEquals(2, spaceship2.getCrewCount());
        assertFalse(hu.getHasPurpleAlien());

        buildingBoard2.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(4, 1, 0);

        hu.setAlienOrange(true);
        spaceship2.addCrewCount(1);

        assertTrue(hu.getHasOrangeAlien());

        buildingBoard2.destroyComponent(4, 1);

        assertTrue(buildingBoard2.checkShipValidityAndTryToFix());
        assertEquals(2, spaceship2.getCrewCount());
        assertFalse(hu.getHasOrangeAlien());

        //printBoard(buildingBoard2);
    }

    @Test
    void keepSpaceshipPart() throws RemoteException {

        // TEST DISCONNECTED COMPONENTS

        Player player = new Player("a", 1, 2);

        Spaceship spaceship = player.getSpaceship();
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // Cannon
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 1, 0);

        // HousingUnit
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(4, 1, 1);

        // Engine
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 3, 0);

        // DoubleEngine
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        buildingBoard.placeComponent(4, 3, 0);

        // Shields (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(5, 3, 1);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 3);

        // BatteryStorage
        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(2, 3, 0);

        // DoubleCannon
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 3, 0);

        assertTrue(buildingBoard.initSpaceshipParams());
        assertEquals(4, spaceship.getCrewCount());

        //printBoard(buildingBoard);

        // Destroy
        buildingBoard.destroyComponent(3, 2);
        assertFalse(buildingBoard.checkShipValidityAndTryToFix());
        player.getSpaceship().getBuildingBoard().keepSpaceshipPart(2, 3);

        //printBoard(buildingBoard);

        // Destroy
        buildingBoard.destroyComponent(3, 3);

        assertFalse(buildingBoard.checkShipValidityAndTryToFix());

        //printBoard(buildingBoard);
        player.getSpaceship().getBuildingBoard().keepSpaceshipPart(4, 3);

        //printBoard(buildingBoard);
        assertEquals(3, spaceship.getExposedConnectorsCount());
    }

    @Test
    void placeAlienComponent() {
        Spaceship spaceship = new Spaceship(1, 0);
        BuildingBoard buildingBoard = new BuildingBoard(spaceship, 0);

        HousingUnit hu = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2);
        hu.setAllowOrangeAlien(true);

        buildingBoard.setHandComponent(hu);
        buildingBoard.placeComponent(2, 1, 0);

        // Populate with humans
        buildingBoard.fillHumans();
        assertEquals(2, hu.getCrewCount());

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("human", 2, 1);
        });
        assertEquals("ComponentAlreadyOccupied", exception.getMessage());

        hu.decrementCrewCount(spaceship, 2);

        // Populate with orange alien
        buildingBoard.placeAlienComponent("orange", 2, 1);
        assertTrue(hu.getHasOrangeAlien());

        hu.setAlienOrange(false);

        hu.decrementCrewCount(spaceship, 1);
        hu.setAllowPurpleAlien(true);

        // Populate with purple alien
        buildingBoard.placeAlienComponent("purple", 2, 1);
        assertTrue(hu.getHasPurpleAlien());

        hu.setAlienPurple(false);

        // Alien exceptions
        hu.setAllowOrangeAlien(false);
        hu.setAllowPurpleAlien(false);

        hu.decrementCrewCount(spaceship, 1);

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("orange", 2, 1);
        });
        assertEquals("CannotContainOrangeAlien", exception.getMessage());

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("purple", 2, 1);

        });

        // Not valid coordinates/components
        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("human", -1, 0);
        });
        assertEquals("NotValidCoordinates", exception.getMessage());

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("human", 3, 3);
        });
        assertEquals("NotValidCoordinates", exception.getMessage());

        Component nonHousing = new Component(ComponentType.STRUCTURAL_UNIT, new int[]{1, 1, 1, 1}, "imgSrc");
        buildingBoard.setHandComponent(nonHousing);
        buildingBoard.placeComponent(3, 2, 0);

        exception = assertThrows(IllegalStateException.class, () -> {
            buildingBoard.placeAlienComponent("human", 3, 2);
        });
        assertEquals("NotValidCoordinates", exception.getMessage());
    }
}