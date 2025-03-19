package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import static org.junit.jupiter.api.Assertions.*;
class BuildingBoardTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getSpaceship() {
    }

    @Test
    void getHandComponent() {
    }

    @Test
    void getSpaceshipMatrix() {
    }

    @Test
    void getBoardMask() {
        Spaceship spaceship = new Spaceship(1, 0);
        BuildingBoard buildingboard = spaceship.getBuildingBoard();

        int[][] mat = buildingboard.getBoardMask();

        for (int i = 0; i < mat.length; i++) {
            System.out.println();
            for (int j = 0; j < mat[i].length; j++) {
                System.out.print(mat[i][j] + " ");
            }
        }
    }

    @Test
    void getBooked() {
    }

    @Test
    void getImgSrc() {
    }

    @Test
    void setAsBooked() {
    }

    @Test
    void setHandComponent() {
    }

    @Test
    void placeComponent() {
        // TODO: secondo me conviene passare il component da aggiungere come parametro
        //  Perche', ad esempio, handComponent puo' essere null ora come ora

        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        Component c;

        boolean result; // function return value

        // Placed in a correct place
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(0, 2);

        assertEquals(c, spaceshipMatrix[0][2]);
        assertTrue(result);

        // Placed outside the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(0, 0);

        assertNull(spaceshipMatrix[0][0]);
        assertFalse(result);

        // Placed in the center cell of the spaceship
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{2, 0, 1, 1}, "imgPath"));
        c = buildingBoard.getHandComponent();

        result = buildingBoard.placeComponent(2, 3);

        assertNotEquals(c, spaceshipMatrix[2][3]);
        assertFalse(result);
    }

    @Test
    void destroyComponent() {
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
    }

    @Test
    void checkShipValidity() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);

        for (int i = 0; i < 4; i++)
            System.out.print(buildingBoard.getHandComponent().getConnections()[i] + " ");

        buildingBoard.placeComponent(1, 3);


        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);
        buildingBoard.placeComponent(1, 2);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 1);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 4);

        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 5);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(2);
        buildingBoard.placeComponent(3, 3);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);
        buildingBoard.placeComponent(3, 2);


        int[][] mask = buildingBoard.getBoardMask();

        for (int i = 0; i < mask.length; i++) {
            System.out.println();
            for (int j = 0; j < mask[i].length; j++) {
                System.out.printf("%-5s", mask[i][j] + " ");
            }
        }

        System.out.println();

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            System.out.println();
            for (int j = 0; j < spaceshipMatrix[0].length; j++) {
                String value = (spaceshipMatrix[i][j] == null) ? "NULL" : spaceshipMatrix[i][j].getType().toString() + "-" + spaceshipMatrix[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }

        System.out.println();
        System.out.println();

        System.out.println(buildingBoard.checkShipValidity());

        assertFalse(buildingBoard.checkShipValidity());
    }

    @Test
    void printBoard() {
    }
}