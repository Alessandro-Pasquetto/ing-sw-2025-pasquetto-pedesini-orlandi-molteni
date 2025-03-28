package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getName() {
        Player player = new Player("gino", 1, 1);

        assertEquals("gino", player.getName());
    }

    @Test
    void getCredits() {
        Player player = new Player("gino", 1, 1);

        assertEquals(0, player.getCredits());

        player.addCredits(100);
        assertEquals(100, player.getCredits());
    }

    @Test
    void getColor() {
        Player player = new Player("gino", 2, 1);

        assertEquals(2, player.getColor());
    }

    @Test
    void getPosition() {
        Player player = new Player("gino", 1, 1);

        assertEquals(0, player.getPosition());

        player.setPosition(5);
        assertEquals(5, player.getPosition());
    }

    @Test
    void getSpaceship() {
        Player player = new Player("gino", 1, 1);

        assertNotNull(player.getSpaceship());
    }

    @Test
    void getHasLeft() {
        Player player = new Player("gino", 0, 1);

        assertFalse(player.getHasLeft());

        player.setHasLeft(true);
        assertTrue(player.getHasLeft());
    }

    @Test
    void setPosition() {
        Player player = new Player("gino", 0, 1);

        player.setPosition(10);
        assertEquals(10, player.getPosition());
    }

    @Test
    void setHasLeft() {
        Player player = new Player("gino", 0, 1);

        player.setHasLeft(true);
        assertTrue(player.getHasLeft());
    }

    @Test
    void setIsReady() {
        Player player = new Player("gino", 0, 1);
        Game game = new Game(0, 4, 0);

        player.setIsReady(true, game);

        assertEquals(1, game.getNumReadyPlayers());
    }

    @Test
    void addCredits() {
        Player player = new Player("gino", 0, 1);

        player.addCredits(50);
        assertEquals(50, player.getCredits());

        // Adding negative credits
        player.addCredits(-30);
        assertEquals(20, player.getCredits());
    }

    @Test
    void rollDice() {
        Player p = new Player("gino", 0, 1);

        // Checks that dice result is always between 2 and 12
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
        assertTrue(p.rollDice() >= 2 && p.rollDice() <= 12);
    }
}