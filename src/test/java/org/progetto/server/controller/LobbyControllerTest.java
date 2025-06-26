package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class LobbyControllerTest {

    @Test
    void createGame() throws RemoteException {

        Player player = null;
        Game game = null;

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){
            }

            public void sendPing() {}
        };

        //Test game with lvl 1 initialized correctly
        GameManager gameManager1 = LobbyController.createGame("Mario", 1, 4, sender);
        assertNotNull(gameManager1);
        player = gameManager1.getGame().getPlayersCopy().get(0);
        assertNotNull(player);
        game = gameManager1.getGame();
        assertNotNull(game);
        game.initPlayersSpaceship();

        //Test player attributes
        assertEquals("Mario", player.getName());
        assertEquals(1, player.getSpaceship().getLevelShip());

        //Test game attributes
        assertEquals(1, game.getLevel());
        assertNotNull(game.getPlayerByName(player.getName()));
        assertEquals(1, game.getPlayersSize());


        //Test game with lvl 2 initialized correctly
        GameManager gameManager2 = LobbyController.createGame("Giovanni", 2, 4, sender);
        assertNotNull(gameManager2);
        player = gameManager2.getGame().getPlayersCopy().get(0);
        assertNotNull(player);
        game = gameManager2.getGame();
        assertNotNull(game);
        game.initPlayersSpaceship();

        //Test player attributes
        assertEquals("Giovanni", player.getName());
        assertEquals(2, player.getSpaceship().getLevelShip());

        //Test game attributes
        assertEquals(2, game.getLevel());
        assertNotNull(game.getPlayerByName(player.getName()));
        assertEquals(1, game.getPlayersSize());

    }

    @Test
    void joinGame() throws RemoteException {

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){
            }

            public void sendPing() {}
        };

        GameManager gameManager  = LobbyController.createGame("Mario", 1, 4, sender);
        Player player1 = gameManager.getGame().getPlayersCopy().get(0);
        Game game = gameManager.getGame();

        Player player2 = new Player("Ciro");

        //Test incorrect game_id
        assertThrows(IllegalStateException.class, ()->LobbyController.joinGame(game.getId() + 2, player2.getName(), sender));

        //Test correct joining
        assertDoesNotThrow(()->LobbyController.joinGame(game.getId(), player2.getName(), sender));

        Player player3 = new Player("Pasquale");
        GameManager gameManager2 = LobbyController.joinGame(game.getId(), player3.getName(), sender);

        //Test game attributes
        assertNotNull(gameManager2);
        assertEquals(game.getLevel(), gameManager2.getGame().getLevel());
        assertNotNull(game.getPlayerByName(player1.getName()));
        assertNotNull(game.getPlayerByName(player2.getName()));
        assertNotNull(game.getPlayerByName(player3.getName()));
        assertEquals(3, game.getPlayersSize());


    }
}