import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import java.io.IOException;

public class BuildingBoardtest {


    @Test
    void Loadingtest() throws IOException {
    BuildingBoard buildingboard = new BuildingBoard(2);

    int[][] mat = buildingboard.getBoardMask();

    for (int i = 0; i < mat.length; i++) {
        System.out.println();
        for (int j = 0; j < mat[i].length; j++) {
            System.out.print(mat[i][j] + " ");
        }
    }


    }


}