package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getTrack() {
        // Level 1 board
        Board board1 = new Board(1);
        assertEquals(18, board1.getTrack().length);

        // Level 2 board
        Board board2 = new Board(2);
        assertEquals(24, board2.getTrack().length);

        // Verify that the returned array is the same instance
        Player[] track = board1.getTrack();
        assertSame(track, board1.getTrack());
    }

    @Test
    void getImgSrc() {
        // Level 1 board
        Board board1 = new Board(1);
        assertEquals("board1.png", board1.getImgSrc());

        // Level 2 board
        Board board2 = new Board(2);
        assertEquals("board2.png", board2.getImgSrc());
    }

    @Test
    void addTraveler() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("arnoldo", 1, 1);
        Player p3 = new Player("andrea", 2, 1);
        Player p4 = new Player("gianmaria", 3, 1);

        Board board;
        Player[] track;

        // Add travelers in level 1 spaceship
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.addActivePlayer(p2, 1);
        board.addActivePlayer(p3, 1);
        board.addActivePlayer(p4, 1);

        track = board.getTrack();

        assertEquals(p1, track[4]);
        assertEquals(p2, track[2]);
        assertEquals(p3, track[1]);
        assertEquals(p4, track[0]);

        // Add travelers in level 1 spaceship
        board = new Board(2);
        board.addActivePlayer(p1, 2);
        board.addActivePlayer(p2, 2);
        board.addActivePlayer(p3, 2);
        board.addActivePlayer(p4, 2);

        track = board.getTrack();

        assertEquals(p1, track[6]);
        assertEquals(p2, track[3]);
        assertEquals(p3, track[1]);
        assertEquals(p4, track[0]);
    }

    @Test
    void movePlayerByDistance() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("arnoldo", 1, 1);

        Board board;
        Player[] track;

//        For loop to print current track

//        int count = 0;
//        for (Player p : track) {
//            if (p != null) {
//                System.out.println(count + ": " + p);
//            } else {
//                System.out.println(count + ": empty");
//            }
//            count++;
//        }

        // Move player ahead
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.movePlayerByDistance(p1, 3);

        track = board.getTrack();

        assertEquals(p1, track[7]);

        // Move player behind
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.movePlayerByDistance(p1, -3);

        track = board.getTrack();

        assertEquals(p1, track[1]);

        // Move ahead encountering a player
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.addActivePlayer(p2, 1);
        board.movePlayerByDistance(p2, 2);

        track = board.getTrack();

        assertEquals(p2, track[5]);

        // Move behind encountering a player
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.addActivePlayer(p2, 1);
        board.movePlayerByDistance(p1, -2);

        track = board.getTrack();

        assertEquals(p1, track[1]);

        // Move ahead player returning in the starting point of the track
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.movePlayerByDistance(p1, 14);

        track = board.getTrack();

        assertEquals(p1, track[0]);

        // Move behind player returning in the end point of the track
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.movePlayerByDistance(p1, -5);

        track = board.getTrack();

        assertEquals(p1, track[17]);
    }

    @Test
    void leaveTravel() {
        Player p1 = new Player("gino", 0, 1);

        Board board;
        Player[] track;

        // Move player ahead
        board = new Board(1);
        board.addActivePlayer(p1, 1);
        board.movePlayerByDistance(p1, 3);

        track = board.getTrack();

        assertEquals(p1, track[7]);

        // Player leaves
        board.leaveTravel(p1);

        assertNull(track[7]);
        assertTrue(p1.getHasLeft());
    }

    @Test
    void updateTurnOrder() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("alessandro", 1, 1);
        Player p3 = new Player("giulia", 2, 1);
        Player p4 = new Player("arnoldo", 3, 1);

        Board board = new Board(1);

        board.addActivePlayer(p2, 1);
        board.addActivePlayer(p3, 1);
        board.addActivePlayer(p4, 1);
        board.addActivePlayer(p1, 1);

        p1.setPosition(4);
        p2.setPosition(3);
        p3.setPosition(2);
        p4.setPosition(1);

        board.updateTurnOrder();

        assertEquals(p1, board.getCopyActivePlayers().get(0));
        assertEquals(p2, board.getCopyActivePlayers().get(1));
        assertEquals(p3, board.getCopyActivePlayers().get(2));
        assertEquals(p4, board.getCopyActivePlayers().get(3));
    }

    @Test
    void getReadyTravelers() {
        Player player = new Player("gino", 0, 2);
        Board board = new Board(2);

        board.addReadyToTravelPlayer(player);

        assertEquals(player, board.getReadyToTravelPlayers().getFirst());
    }

    @Test
    void addReadyTraveler() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("alessandro", 1, 1);
        Player p3 = new Player("giulia", 2, 1);
        Player p4 = new Player("arnoldo", 3, 1);

        Board board = new Board(1);

        // Adding first ready player
        board.addReadyToTravelPlayer(p1);
        assertEquals(p1, board.getReadyToTravelPlayers().get(0));

        // Adding second ready player
        board.addReadyToTravelPlayer(p2);
        assertEquals(p2, board.getReadyToTravelPlayers().get(1));

        // Tries to add again first player
        assertThrows(IllegalStateException.class, () -> board.addReadyToTravelPlayer(p1));
    }
}