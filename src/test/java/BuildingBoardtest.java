import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;

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


}