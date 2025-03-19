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
    }

    @Test
    void getImgSrc() {
    }

    @Test
    void addTraveler() {
    }

    @Test
    void movePlayerByDistance() {
        Board board = new Board(18, "imgSrc");

        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("arnoldo", 1, 1);
        Player p3 = new Player("andrea", 2, 1);
        Player p4 = new Player("gianmaria", 3, 1);

        // Move player ahead
        board.addTraveler(p1);
        board.movePlayerByDistance(p1, -3);

        Player[] track = board.getTrack();

        int count = 0;
        for (Player p : track) {
            if (p != null) {
                System.out.println(count + ": " + p);
            } else {
                System.out.println(count + ": empty");
            }
            count++;
        }

        assertEquals(p1, track[7]);
    }

    @Test
    void leaveTravel() {
    }
}