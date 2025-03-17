import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.io.IOException;

public class BuildingBoardtest {

    @Test
    void Loadingtest() throws IOException {

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
}


