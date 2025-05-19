package org.progetto.server.model.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PlanetsTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getPlanetsTaken() {
        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>();

        ArrayList<Box> planet1 = new ArrayList<>();
        planet1.add(Box.RED);
        planet1.add(Box.YELLOW);

        ArrayList<Box> planet2 = new ArrayList<>();
        planet2.add(Box.RED);
        planet2.add(Box.YELLOW);

        ArrayList<Box> planet3 = new ArrayList<>();
        planet3.add(Box.RED);
        planet3.add(Box.YELLOW);

        rewardsForPlanets.add(planet1);
        rewardsForPlanets.add(planet2);
        rewardsForPlanets.add(planet3);

        // Card creation
        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", rewardsForPlanets, -2);

        assertArrayEquals(new boolean[]{false, false, false}, planets.getPlanetsTaken());
    }

    @Test
    void getRewardsForPlanets() {
        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>();

        ArrayList<Box> planet1 = new ArrayList<>();
        planet1.add(Box.RED);
        planet1.add(Box.YELLOW);

        ArrayList<Box> planet2 = new ArrayList<>();
        planet2.add(Box.RED);
        planet2.add(Box.YELLOW);

        ArrayList<Box> planet3 = new ArrayList<>();
        planet3.add(Box.RED);
        planet3.add(Box.YELLOW);

        rewardsForPlanets.add(planet1);
        rewardsForPlanets.add(planet2);
        rewardsForPlanets.add(planet3);

        // Card creation
        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", rewardsForPlanets, -2);

        assertEquals(rewardsForPlanets, planets.getRewardsForPlanets());
    }

    @Test
    void getPenaltyDays() {
        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", new ArrayList<ArrayList<Box>>(), -2);
        assertEquals(-2, planets.getPenaltyDays());
    }

    @Test
    void choosePlanet() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("alba", 1, 1);
        Player p3 = new Player("andrea", 2, 1);
        Player p4 = new Player("arianna", 3, 1);

        // List of rewards for each planet creation
        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>();

        ArrayList<Box> planet1 = new ArrayList<>();
        planet1.add(Box.RED);
        planet1.add(Box.YELLOW);

        ArrayList<Box> planet2 = new ArrayList<>();
        planet2.add(Box.RED);
        planet2.add(Box.YELLOW);

        ArrayList<Box> planet3 = new ArrayList<>();
        planet3.add(Box.RED);
        planet3.add(Box.YELLOW);

        rewardsForPlanets.add(planet1);
        rewardsForPlanets.add(planet2);
        rewardsForPlanets.add(planet3);

        // Card creation
        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", rewardsForPlanets, -2);

        // Player 1 chooses planet1
        assertTrue(planets.choosePlanet(p1, 0));
        assertTrue(planets.getPlanetsTaken()[0]);
        assertEquals(p1, planets.getLandedPlayers().peek());

        // Player 2 tries to choose planet1 (already taken)
        assertFalse(planets.choosePlanet(p2, 0));
        assertEquals(p1, planets.getLandedPlayers().peek());

        // Player 3 tries to pick a planet outside index bounds
        assertFalse(planets.choosePlanet(p3, -1));

        // Player 4 tries to pick a planet outside index bounds
        assertFalse(planets.choosePlanet(p4, 3));
    }

    @Test
    void chooseRewardsBox() {
        Player player1 = new Player("gino", 0, 1);
        Player player2 = new Player("stuart", 1, 2);

        Board board = new Board(1, 2);
        board.addTraveler(player1);
        board.addTraveler(player2);

        Player[] track;
        track = board.getTrack();

        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", new ArrayList<ArrayList<Box>>(), -2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Box boxR = Box.RED;
        Box boxY = Box.YELLOW;
        Box boxG = Box.GREEN;
        Box boxB = Box.BLUE;

        //adds a red box to a red box storage
        assertTrue(planets.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //tries to add a red box to a box storage
        assertFalse(planets.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxR));

        //tries to add a red box to a red box storage in an already taken place
        assertFalse(planets.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //adds a green box to a red box storage
        assertTrue(planets.chooseRewardBox(player1.getSpaceship(), boxStorage1, 1, boxG));

        //adds a yellow box in a box storage
        assertTrue(planets.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxY));

        //adds a blue box in a box storage
        assertTrue(planets.chooseRewardBox(player1.getSpaceship(), boxStorage2, 1, boxB));
    }

    @Test
    void penalty() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("alba", 1, 1);
        Player p3 = new Player("andrea", 2, 1);
        Player p4 = new Player("arianna", 3, 1);

        Board board = new Board(1, 4);

        board.addTraveler(p1);
        board.addTraveler(p2);
        board.addTraveler(p3);
        board.addTraveler(p4);

        board.addTravelersOnTrack(1);

        board.movePlayerByDistance(p1, 5);
        board.movePlayerByDistance(p2, 5);
        board.movePlayerByDistance(p3, 4);
        board.movePlayerByDistance(p4, 4);

        // List of rewards for each planet creation
        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>();

        ArrayList<Box> planet1 = new ArrayList<>();
        planet1.add(Box.RED);
        planet1.add(Box.YELLOW);

        ArrayList<Box> planet2 = new ArrayList<>();
        planet2.add(Box.RED);
        planet2.add(Box.YELLOW);

        ArrayList<Box> planet3 = new ArrayList<>();
        planet3.add(Box.RED);
        planet3.add(Box.YELLOW);

        rewardsForPlanets.add(planet1);
        rewardsForPlanets.add(planet2);
        rewardsForPlanets.add(planet3);

        // Card creation
        Planets planets = new Planets(CardType.PLANETS, 2, "imgSrc", rewardsForPlanets, -2);

        // Player 1 chooses planet2
        planets.choosePlanet(p1, 1);

        // Player 2 chooses planet1
        planets.choosePlanet(p2, 0);

        // Player 4 chooses planet3
        planets.choosePlanet(p4, 2);

        // Give to landed player their penalty
        planets.penalty(board);

        // Check if players are in correct position
        assertEquals(p1, board.getTrack()[7]);
        assertEquals(p2, board.getTrack()[4]);
        assertEquals(p3, board.getTrack()[5]);
        assertEquals(p4, board.getTrack()[2]);
    }
}