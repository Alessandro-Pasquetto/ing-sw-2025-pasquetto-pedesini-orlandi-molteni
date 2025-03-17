import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildingBoardTest {

    @Test
    void LoadingTest() throws IOException {

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
    void RotationTest()
    {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{2, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(4);

        for (int i = 0; i < 4; i++) {
            System.out.print(buildingBoard.getHandComponent().getConnections()[i] + " ");
        }
    }

    @Test
    void areConnectedTest() {
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
}


