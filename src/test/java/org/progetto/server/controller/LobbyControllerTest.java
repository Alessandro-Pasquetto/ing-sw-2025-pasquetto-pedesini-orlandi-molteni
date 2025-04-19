package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import static org.junit.jupiter.api.Assertions.*;

class LobbyControllerTest {

    @Test
    void createGame() {

        InternalGameInfo internalGameInfo = null;
        Player player = null;
        Game game = null;

        //Test game with lvl 1 initialized correctly
        internalGameInfo  = LobbyController.createGame("Mario",1,4);
        assertNotNull(internalGameInfo);
        player = internalGameInfo.getPlayer();
        assertNotNull(player);
        GameManager gameManager = internalGameInfo.getGameManager();
        game = gameManager.getGame();
        assertNotNull(game);

        //Test player attributes
        assertEquals("Mario", player.getName());
        assertEquals(1,player.getSpaceship().getLevelShip());

        //Test game attributes
        assertEquals(1,game.getLevel());
        assertNotNull(game.getPlayerByName(player.getName()));
        assertEquals(1,game.getPlayersSize());


        //Test game with lvl 2 initialized correctly
        internalGameInfo  = LobbyController.createGame("Giovanni",2,4);
        assertNotNull(internalGameInfo);
        player = internalGameInfo.getPlayer();
        assertNotNull(player);
        gameManager = internalGameInfo.getGameManager();
        game = gameManager.getGame();
        assertNotNull(game);

        //Test player attributes
        assertEquals("Giovanni", player.getName());
        assertEquals(2,player.getSpaceship().getLevelShip());

        //Test game attributes
        assertEquals(2,game.getLevel());
        assertNotNull(game.getPlayerByName(player.getName()));
        assertEquals(1,game.getPlayersSize());

    }

    @Test
    void joinGame() {

        InternalGameInfo internalGameInfo  = LobbyController.createGame("Mario",1,4);
        Player player_1 = internalGameInfo.getPlayer();
        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();

        Player player_2 = new Player("Ciro",1,1);

        //Test incorrect game_id
        assertThrows(IllegalStateException.class,()->LobbyController.joinGame(game.getId()+2, player_2.getName()));

        //Test correct joining
        assertDoesNotThrow(()->LobbyController.joinGame(game.getId(), player_2.getName()));

        Player player_3 = new Player("Pasquale",2,1);
        internalGameInfo = LobbyController.joinGame(game.getId(), player_3.getName());

        //Test game attributes
        assertNotNull(internalGameInfo);
        assertEquals(game.getLevel(),internalGameInfo.getGameManager().getGame().getLevel());
        assertNotNull(game.getPlayerByName(player_1.getName()));
        assertNotNull(game.getPlayerByName(player_2.getName()));
        assertNotNull(game.getPlayerByName(player_3.getName()));
        assertEquals(3,game.getPlayersSize());


    }
}