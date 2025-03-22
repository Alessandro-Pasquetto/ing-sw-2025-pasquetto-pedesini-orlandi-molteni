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
    }

    @Test
    void getLevel() {
    }

    @Test
    void getPhase() {
    }

    @Test
    void getPlayers() {
    }

    @Test
    void getPlayersSize() {
    }

    @Test
    void getMaxNumPlayers() {
    }

    @Test
    void getBoard() {
    }

    @Test
    void setPhase() {
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