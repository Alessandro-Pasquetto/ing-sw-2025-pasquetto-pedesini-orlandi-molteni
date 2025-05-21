package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LostStationTest {

    @Test
    void getRequiredCrew() {
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 5, new ArrayList<>(), -3);
        assertEquals(5, lostStation1.getRequiredCrew());
    }

    @Test
    void getRewardBoxes() {
        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(Box.RED);
        rewardBoxes.add(Box.GREEN);
        LostStation lostStation = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 5, rewardBoxes, -3);
        assertEquals(rewardBoxes, lostStation.getRewardBoxes());
    }

    @Test
    void getPenaltyDays() {
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 5, new ArrayList<>(), -3);
        assertEquals(-3, lostStation1.getPenaltyDays());
    }

    @Test
    void chooseRewardBox() {
        Game game = new Game(0, 3, 2);

        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initPlayersSpaceship();

        Board board = new Board(1, 2);
        board.addTraveler(player1);
        board.addTraveler(player2);
        Player[] track;
        track = board.getTrack();
        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 5, rewardBoxes1, -3);
        LostStation lostStation2 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 6, rewardBoxes2, -2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Box boxR = Box.RED;
        Box boxY = Box.YELLOW;
        Box boxG = Box.GREEN;
        Box boxB = Box.BLUE;

        lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxR, 0);

        //tries to add a red box to a box storage
        assertThrows(IllegalStateException.class, () -> lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxR, 0));

        //tries to add a red box to a red box storage in an already taken place
        assertThrows(IllegalStateException.class, () -> lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxR, 0));

        //adds a green box to a red box storage
        lostStation1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxG, 1);

        //adds a yellow box in a box storage
        lostStation2.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxY, 0);

        //adds a blue box in a box storage
        lostStation2.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxB, 1);
    }

    @Test
    void penalty() {
        Game game = new Game(0, 3, 2);

        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initPlayersSpaceship();

        Board board = new Board(1, 2);
        board.addTraveler(player1);
        board.addTraveler(player2);

        board.addTravelersOnTrack(1);

        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
        LostStation lostStation1 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 5, rewardBoxes1, -3);
        LostStation lostStation2 = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 6, rewardBoxes2, -2);

        //moves player1 back
        lostStation1.penalty(board, player1);
        assertEquals(0, player1.getPosition());

        //moves player2 back 2
        lostStation2.penalty(board, player2);
        assertEquals(0, player1.getPosition());
    }
}