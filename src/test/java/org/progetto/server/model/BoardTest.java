package org.progetto.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getTrack() {
        // Level 1 board
        Board board1 = new Board(1, 3);
        assertEquals(18, board1.getTrack().length);

        // Level 2 board
        Board board2 = new Board(2, 3);
        assertEquals(24, board2.getTrack().length);

        // Verify that the returned array is the same instance
        Player[] track = board1.getTrack();
        assertSame(track, board1.getTrack());
    }

    @Test
    void getImgSrc() {
        // Level 1 board
        Board board1 = new Board(1, 3);
        assertEquals("board1.png", board1.getImgSrc());

        // Level 2 board
        Board board2 = new Board(2, 4);
        assertEquals("board2.png", board2.getImgSrc());
    }

    @Test
    void decideStartingPositionOnTruck() {
        Board board = new Board(1, 3);

        Player p1 = new Player("gino");
        Player p2 = new Player("arnoldo");
        Player p3 = new Player("andrea");

        assertThrows(IllegalStateException.class, () -> {
            board.decideStartingPositionOnTrack(p1, -2);
        });

        assertThrows(IllegalStateException.class, () -> {
            board.decideStartingPositionOnTrack(p1, 6);
        });

        board.decideStartingPositionOnTrack(p1, 2);
        board.decideStartingPositionOnTrack(p2, 0);
        board.decideStartingPositionOnTrack(p3, 1);

        assertArrayEquals(new Player[] {p2, p3, p1}, board.getStartingPositionsCopy());

        board.updateTravelersBasedOnStartingPosition();

        List<Player> expected = List.of(p2, p3, p1);

        assertEquals(expected, board.getCopyTravelers());
    }

    @Test
    void addTraveler() {
        Player p1 = new Player("gino");
        Player p2 = new Player("arnoldo");
        Player p3 = new Player("andrea");
        Player p4 = new Player("gianmaria");

        Game game = new Game(0, 4, 1);
        Board board;
        Player[] track;

        // Add travelers in level 1 spaceship
        board = new Board(1, 4);
        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        board.addTravelersOnTrack(1);

        track = board.getTrack();

        assertEquals(p1, track[4]);
        assertEquals(p2, track[2]);
        assertEquals(p3, track[1]);
        assertEquals(p4, track[0]);

        // Add travelers in level 1 spaceship
        board = new Board(2, 4);
        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        p1.setIsReady(true, game);
        p2.setIsReady(true, game);
        p3.setIsReady(true, game);
        p4.setIsReady(true, game);

        assertTrue(board.allTravelersReady());

        board.addTravelersOnTrack(2);
        track = board.getTrack();

        assertEquals(p1, track[6]);
        assertEquals(p2, track[3]);
        assertEquals(p3, track[1]);
        assertEquals(p4, track[0]);

        assertEquals(4, board.getNumTravelers());
    }

    @Test
    void removeTraveler() {
        Player p1 = new Player("gino");
        Player p2 = new Player("arnoldo");
        Player p3 = new Player("andrea");
        Player p4 = new Player("gianmaria");

        Game game = new Game(0, 4, 1);

        // Add travelers in level 1 spaceship
        Board board = new Board(1, 4);
        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        board.removeTraveler(p1);
        board.removeTraveler(p2);

        ArrayList<Player> players = new ArrayList<>();
        players.add(p3);
        players.add(p4);

        assertEquals(players, board.getCopyTravelers());
    }

    @Test
    void movePlayerByDistance() {
        Player p1 = new Player("gino");
        Player p2 = new Player("arnoldo");

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
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, 3);

        track = board.getTrack();

        assertEquals(p1, track[7]);

        // Move player behind
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, -3);

        track = board.getTrack();

        assertEquals(p1, track[1]);

        // Move ahead encountering a player
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p2, 2);

        track = board.getTrack();

        assertEquals(p2, track[5]);

        // Move behind encountering a player
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, -2);

        track = board.getTrack();

        assertEquals(p1, track[1]);

        // Move ahead player returning in the starting point of the track
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, 14);

        track = board.getTrack();

        assertEquals(p1, track[0]);

        // Move behind player returning in the end point of the track
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, -5);

        track = board.getTrack();

        assertEquals(p1, track[17]);
    }

    @Test
    void leaveTravel() {
        Player p1 = new Player("gino");

        Board board;
        Player[] track;

        // Move player ahead
        board = new Board(1, 2);
        board.addTraveler(p1);
        board.addTravelersOnTrack(1);

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
        Player p1 = new Player("gino");
        Player p2 = new Player("alessandro");
        Player p3 = new Player("giulia");
        Player p4 = new Player("arnoldo");

        Board board = new Board(1, 4);

        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);
        board.addTraveler(p1);

        p1.setPosition(4);
        p2.setPosition(3);
        p3.setPosition(2);
        p4.setPosition(1);

        board.updateTurnOrder();

        assertEquals(p1, board.getCopyTravelers().get(0));
        assertEquals(p2, board.getCopyTravelers().get(1));
        assertEquals(p3, board.getCopyTravelers().get(2));
        assertEquals(p4, board.getCopyTravelers().get(3));
    }

    @Test
    void checkLappedPlayer() {
        Player p1 = new Player("gino");
        Player p2 = new Player("alessandro");

        Board board = new Board(1, 2);

        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTravelersOnTrack(1);
        board.movePlayerByDistance(p1, 30);

        board.checkLappedPlayers(new ArrayList<>());

        assertTrue(p2.getHasLeft());

        Player p3 = new Player("gino");
        Player p4 = new Player("alessandro");

        Board board2 = new Board(1, 2);

        board2.addTraveler(p3);
        board2.addTraveler(p4);
        board2.addTravelersOnTrack(1);
        board2.movePlayerByDistance(p3, 3);

        assertNull(board2.checkLappedPlayers(new ArrayList<>()));
    }

    @Test
    void checkNoCrewPlayers() {
        Game game = new Game(0, 4, 2);
        Player p1 = new Player("gino");
        Player p2 = new Player("alessandro");
        Player p3 = new Player("gino");
        Player p4 = new Player("alessandro");

        Board board = game.getBoard();

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);
        game.addPlayer(p4);

        game.initPlayersSpaceship();

        board.addTraveler(p1);
        board.addTraveler(p2);

        p1.getSpaceship().addCrewCount(3);
        p2.getSpaceship().addCrewCount(0);

        board.checkNoCrewPlayers();

        assertTrue(p2.getHasLeft());

        Board board2 = new Board(1, 2);

        board2.addTraveler(p3);
        board2.addTraveler(p4);

        p3.getSpaceship().addCrewCount(3);
        p4.getSpaceship().addCrewCount(2);

        assertNull(board2.checkNoCrewPlayers());
    }
}