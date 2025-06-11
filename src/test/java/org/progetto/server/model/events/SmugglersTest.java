package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.*;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SmugglersTest {

    @Test
    void getFirePowerRequired() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(5, smugglers.getFirePowerRequired());
    }

    @Test
    void getPenaltyBoxes() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(2, smugglers.getPenaltyBoxes());
    }

    @Test
    void getPenaltyDays() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(-3, smugglers.getPenaltyDays());
    }

    @Test
    void getRewardBoxes() {
        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(Box.RED);
        rewardBoxes.add(Box.GREEN);
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, rewardBoxes);
        assertEquals(rewardBoxes, smugglers.getRewardBoxes());
    }

    @Test
    void chooseRewardBox() {
        Game game = new Game(0, 4, 2);
        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.initPlayersSpaceship();

        Board board = game.getBoard();
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
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 6, 2, -2, rewardBoxes2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Box boxR = Box.RED;
        Box boxY = Box.YELLOW;
        Box boxG = Box.GREEN;
        Box boxB = Box.BLUE;

        //adds a red box to a red box storage
        smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxR, 0);

        //tries to add a red box to a box storage
        assertThrows(IllegalStateException.class, () -> smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxR, 0));

        //tries to add a red box to a red box storage in an already taken place
        assertThrows(IllegalStateException.class, () -> smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxR, 0));

        //adds a green box to a red box storage
        smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, boxG, 1);

        //adds a yellow box in a box storage
        smugglers2.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxY, 0);

        //adds a blue box in a box storage
        smugglers2.chooseRewardBox(player1.getSpaceship(), boxStorage2, boxB, 1);
    }

    @Test
    void penalty() {
        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");
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
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 6, 2, -2, rewardBoxes2);

        //moves player1 back 3
        smugglers1.penalty(board, player1);
        assertEquals(0, player1.getPosition());

        //moves player2 back 2
        smugglers2.penalty(board, player2);
        assertEquals(0, player1.getPosition());
    }

    @Test
    void randomDiscardBoxes() {
        Game game = new Game(0, 3, 2);

        Player mario = new Player("mario");

        game.addPlayer(mario);
        game.initPlayersSpaceship();

        BuildingBoard buildingBoard = mario.getSpaceship().getBuildingBoard();

        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "", 3);
        bs1.addBox(mario.getSpaceship(), Box.YELLOW, 2);
        bs1.addBox(mario.getSpaceship(), Box.GREEN, 1);
        buildingBoard.setHandComponent(bs1);
        buildingBoard.placeComponent(3, 1, 0);
        mario.getSpaceship().getBuildingBoard().initSpaceshipParams();

        // Removes one box from the Box storage
        smugglers.randomDiscardBoxes(mario.getSpaceship(), 1);
        assertEquals(1, mario.getSpaceship().getBoxesCount());

        // Remove another box from the box storage
        smugglers.randomDiscardBoxes(mario.getSpaceship(), 1);
        assertEquals(0, mario.getSpaceship().getBoxesCount());

        // Tries to remove another box from an empty box storage
        smugglers.randomDiscardBoxes(mario.getSpaceship(), 1);
        assertEquals(0, mario.getSpaceship().getBoxesCount());
    }

    @Test
    void discardAllBoxes() {
        Game game = new Game(0, 3, 2);

        Player mario = new Player("mario");

        game.addPlayer(mario);
        game.initPlayersSpaceship();

        BuildingBoard buildingBoard = mario.getSpaceship().getBuildingBoard();

        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "", 3);
        bs1.addBox(mario.getSpaceship(), Box.YELLOW, 2);
        bs1.addBox(mario.getSpaceship(), Box.GREEN, 1);
        buildingBoard.setHandComponent(bs1);
        buildingBoard.placeComponent(3, 1, 0);
        mario.getSpaceship().getBuildingBoard().initSpaceshipParams();
        assertEquals(2, mario.getSpaceship().getBoxesCount());

        // Removes one box from the Box storage
        smugglers.discardAllBoxes(mario.getSpaceship());
        assertEquals(0, mario.getSpaceship().getBoxesCount());
    }

    @Test
    void battleResult() {
        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
        Player player1 = new Player("Max");
        Player player2 = new Player("Mindy");
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 6, 2, -2, rewardBoxes2);

        //compares a power equal to the one required
        assertEquals(0, smugglers1.battleResult(5));

        //compares a lower power than required
        assertEquals(-1, smugglers2.battleResult(5));

        //compares a higher power than required
        assertEquals(1, smugglers1.battleResult(8));
    }

    @Test
    void randomDiscardedBattery() {
        Game game = new Game(0, 3, 2);

        Player mario = new Player("mario");

        game.addPlayer(mario);
        game.initPlayersSpaceship();

        BuildingBoard buildingBoard = mario.getSpaceship().getBuildingBoard();

        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2, "imgPath", 5, 2, -3, new ArrayList<>());
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        buildingBoard.setHandComponent(battery);
        buildingBoard.placeComponent(3, 1, 0);
        mario.getSpaceship().getBuildingBoard().initSpaceshipParams();

        // Removes one battery from the Battery Storage
        smugglers.randomDiscardBatteries(mario.getSpaceship(), 1);
        assertEquals(1, mario.getSpaceship().getBatteriesCount());

        // Remove another battery from the storage
        smugglers.randomDiscardBatteries(mario.getSpaceship(), 1);
        assertEquals(0, mario.getSpaceship().getBatteriesCount());

        // Tries to remove another battery from an empty storage
        smugglers.randomDiscardBatteries(mario.getSpaceship(), 1);
        assertEquals(0, mario.getSpaceship().getBatteriesCount());
    }
}