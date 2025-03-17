import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;

import java.io.IOException;

public class GameTest {

    @Test
    void GameCreationTest() {
         Game game = new Game(1,4,1);
         Player mario = new Player("mario",0,game.getLevel());
         Player alice = new Player("alice",1,game.getLevel());

         game.addPlayer(mario);
         game.addPlayer(alice);


         Spaceship sp_1 = game.getPlayers().get(0).getSpaceship();            //get spaceship
         BuildingBoard bb_1 = sp_1.getBuildingBoard();                        //get building board
         Component component = game.pickHiddenComponent(mario);               //get fist component
         bb_1.setHandComponent(component);                                    //pick fist component
         bb_1.placeComponent(0,2);                                      //place component



         System.out.println("Hidden " + component.isHidden());
         System.out.println("Placed " + component.isPlaced());
         bb_1.printBoard();


    }



    @Test
    void Loadingtest() throws IOException {
        Game game = new Game(0,4,1);




    }


}