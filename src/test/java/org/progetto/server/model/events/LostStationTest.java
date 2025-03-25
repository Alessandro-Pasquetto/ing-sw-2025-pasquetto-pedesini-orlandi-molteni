package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.BoxType;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LostStationTest {

    @Test
    void getRequiredCrew() {
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION,2,"imgPath", 5, new ArrayList<>(), -3);
        assertEquals(5, lostStation1.getRequiredCrew());
    }

    @Test
    void getRewardBoxes() {
        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(new Box(BoxType.RED, 4));
        rewardBoxes.add(new Box(BoxType.GREEN, 2));
        LostStation lostStation = new LostStation(CardType.LOSTSTATION,2,"imgPath", 5, rewardBoxes, -3);
        assertEquals(rewardBoxes, lostStation.getRewardBoxes());
    }

    @Test
    void getPenaltyDays() {
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION,2,"imgPath", 5, new ArrayList<>(), -3);
        assertEquals(-3, lostStation1.getPenaltyDays());
    }

    @Test
    void chooseRewardBox() {
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Board board = new Board(1);
        board.addTraveler(player1, 1);
        board.addTraveler(player2, 1);
        Player[] track;
        track = board.getTrack();
        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(new Box(BoxType.RED, 4));
        rewardBoxes1.add(new Box(BoxType.GREEN, 2));
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(new Box(BoxType.YELLOW, 4));
        rewardBoxes2.add(new Box(BoxType.BLUE, 2));
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION,2, "imgPath", 5, rewardBoxes1, -3);
        LostStation lostStation2 = new LostStation(CardType.LOSTSTATION,2, "imgPath", 6, rewardBoxes2, -2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Box boxR = new Box(BoxType.RED, 4);
        Box boxY = new Box(BoxType.YELLOW, 3);
        Box boxG = new Box(BoxType.GREEN, 2);
        Box boxB = new Box(BoxType.BLUE, 1);

        //adds a red box to a red box storage
        assertTrue(lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //tries to add a red box to a box storage
        assertFalse(lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxR));

        //tries to add a red box to a red box storage in an already taken place
        assertFalse(lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //adds a green box to a red box storage
        assertTrue(lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 1, boxG));

        //adds a yellow box in a box storage
        assertTrue(lostStation2.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxY));

        //adds a blue box in a box storage
        assertTrue(lostStation2.chooseRewardBox(player1.getSpaceship(), boxStorage2, 1, boxB));
    }

    @Test
    void penalty() {
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Board board = new Board(1);
        board.addTraveler(player1, 1);
        board.addTraveler(player2, 1);
        Player[] track;
        track = board.getTrack();
        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(new Box(BoxType.RED, 4));
        rewardBoxes1.add(new Box(BoxType.GREEN, 2));
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(new Box(BoxType.YELLOW, 4));
        rewardBoxes2.add(new Box(BoxType.BLUE, 2));
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION,2,"imgPath", 5, rewardBoxes1, -3);
        LostStation lostStation2 = new LostStation(CardType.LOSTSTATION, 2,"imgPath", 6, rewardBoxes2, -2);

        //moves player1 back
        lostStation1.penalty(board, player1);
        assertEquals(0, player1.getPosition());

        //moves player2 back 2
        lostStation2.penalty(board, player2);
        assertEquals(0, player1.getPosition());
    }
}