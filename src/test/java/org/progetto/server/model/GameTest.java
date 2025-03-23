package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.EventCard;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getId() {
        Game game1 = new Game(1, 4, 2);
        assertEquals(1, game1.getId());

        Game game2 = new Game(2, 4, 2);
        assertEquals(2, game2.getId());
    }

    @Test
    void getLevel() {
        Game game1 = new Game(1, 4, 1);
        assertEquals(1, game1.getLevel());

        Game game2 = new Game(2, 4, 2);
        assertEquals(2, game2.getLevel());
    }

    @Test
    void getPhase() {
        Game game = new Game(1, 4, 2);
        assertEquals(GamePhase.INIT, game.getPhase());
    }

    @Test
    void getPlayers() {
        Game game = new Game(1, 4, 2);

        // Test initial empty player list
        assertTrue(game.getPlayers().isEmpty());

        // Add a player and test
        Player player = new Player("gino", 0, 2);
        game.addPlayer(player);

        ArrayList<Player> players = game.getPlayers();
        assertEquals(1, players.size());
        assertEquals("gino", players.get(0).getName());

        // Verify that getPlayers returns a new ArrayList (defensive copy)
        ArrayList<Player> players1 = game.getPlayers();
        ArrayList<Player> players2 = game.getPlayers();
        assertNotSame(players1, players2);
    }

    @Test
    void getPlayersSize() {
        Game game = new Game(1, 4, 2);

        // Test initial size
        assertEquals(0, game.getPlayersSize());

        // Add players and test size
        game.addPlayer(new Player("gino", 0, 2));
        assertEquals(1, game.getPlayersSize());

        game.addPlayer(new Player("andrea", 1, 2));
        assertEquals(2, game.getPlayersSize());
    }

    @Test
    void getMaxNumPlayers() {
        Game game1 = new Game(1, 4, 2);
        assertEquals(4, game1.getMaxNumPlayers());

        Game game2 = new Game(2, 2, 2);
        assertEquals(2, game2.getMaxNumPlayers());

        Game game3 = new Game(3, 3, 2);
        assertEquals(3, game3.getMaxNumPlayers());
    }

    @Test
    void getBoard() {
        // Test for level 1
        Game game1 = new Game(1, 4, 1);
        assertNotNull(game1.getBoard());
        assertEquals(18, game1.getBoard().getTrack().length);

        // Test for level 2
        Game game2 = new Game(1, 4, 2);
        assertNotNull(game2.getBoard());
        assertEquals(24, game2.getBoard().getTrack().length);
    }

    @Test
    void setPhase() {
        Game game = new Game(1, 4, 2);

        // Test initial phase
        assertEquals(GamePhase.INIT, game.getPhase());

        // Test setting to BUILDING
        game.setPhase(GamePhase.BUILDING);
        assertEquals(GamePhase.BUILDING, game.getPhase());

        // Test setting to TRAVEL
        game.setPhase(GamePhase.TRAVEL);
        assertEquals(GamePhase.TRAVEL, game.getPhase());

        // Test setting to EVENT
        game.setPhase(GamePhase.EVENT);
        assertEquals(GamePhase.EVENT, game.getPhase());

        // Test setting to END
        game.setPhase(GamePhase.ENDGAME);
        assertEquals(GamePhase.ENDGAME, game.getPhase());
    }

    @Test
    void saveGame() {
    }

    @Test
    void endGame() {
        Player mario = new Player("mario",1,2);
        Player alice = new Player("alice",1,2);
        Player bob = new Player("bob",1,2);

        Game game = new Game(0, 3,2);
        game.addPlayer(mario);
        game.addPlayer(alice);
        game.addPlayer(bob);

        mario.addCredits(2);
        alice.addCredits(2);

        ArrayList<Player> winners = game.endGame();

        assertTrue(winners.contains(mario));
        assertTrue(winners.contains(alice));
        assertFalse(winners.contains(bob));
    }

    @Test
    void addPlayer() {
        Player mario = new Player("mario",1,2);
        Player alice = new Player("alice",1,2);

        Game game = new Game(0, 3,2);
        game.addPlayer(mario);
        game.addPlayer(alice);

        assertTrue(game.getPlayers().contains(mario));
        assertTrue(game.getPlayers().contains(alice));

    }

    @Test
    void pickHiddenComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);
        Component component = game.pickHiddenComponent(mario);
        assertEquals(component, mario.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void pickVisibleComponent() {

    }

    @Test
    void pickEventCard() {
    }

    @Test
    void tryAddPlayer() {
    }
}