package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SmugglersTest {

    @Test
    void getFirePowerRequired() {
    }

    @Test
    void getPenaltyBoxes() {
    }

    @Test
    void getPenaltyDays() {
    }

    @Test
    void getRewardBoxes() {
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
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS,2, "imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS,2, "imgPath", 6, 2, -2, rewardBoxes2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2, true);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2, false);
        Box boxR = new Box(BoxType.RED, 4);
        Box boxY = new Box(BoxType.YELLOW, 3);
        Box boxG = new Box(BoxType.GREEN, 2);
        Box boxB = new Box(BoxType.BLUE, 1);

        //adds a red box to a red box storage
        assertTrue(smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //tries to add a red box to a box storage
        assertFalse(smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxR));

        //tries to add a red box to a red box storage in an already taken place
        assertFalse(smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 0, boxR));

        //adds a green box to a red box storage
        assertTrue(smugglers1.chooseRewardBox(player1.getSpaceship(), boxStorage1, 1, boxG));

        //adds a yellow box in a box storage
        assertTrue(smugglers2.chooseRewardBox(player1.getSpaceship(), boxStorage2, 0, boxY));

        //adds a blue box in a box storage
        assertTrue(smugglers2.chooseRewardBox(player1.getSpaceship(), boxStorage2, 1, boxB));
    }

    @Test
    void chooseDiscardedBox() {
    }

    @Test
    void chooseDiscardedBattery() {

        Player player1 = new Player("Max", 0, 1);

        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(new Box(BoxType.RED, 4));
        rewardBoxes.add(new Box(BoxType.GREEN, 2));
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, rewardBoxes);
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(2);

        // Returns false if component is not a Housing Unit
        assertFalse(smugglers.chooseDiscardedBattery((BatteryStorage) notBattery,player1));

        // Removes one battery member from the Housing Unit
        assertTrue(smugglers.chooseDiscardedBattery(battery,player1));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(smugglers.chooseDiscardedBattery(battery,player1));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(smugglers.chooseDiscardedBattery(battery,player1));
        assertEquals(0, battery.getItemsCount());
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
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 6, 2, -2, rewardBoxes2);

        //moves player1 back 3
        smugglers1.penalty(board, player1);
        assertEquals(0, player1.getPosition());

        //moves player2 back 2
        smugglers2.penalty(board, player2);
        assertEquals(0, player1.getPosition());
    }

    @Test
    void battleResult() {
        ArrayList<Box> rewardBoxes1 = new ArrayList<>();
        rewardBoxes1.add(new Box(BoxType.RED, 4));
        rewardBoxes1.add(new Box(BoxType.GREEN, 2));
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(new Box(BoxType.YELLOW, 4));
        rewardBoxes2.add(new Box(BoxType.BLUE, 2));
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 6, 2, -2, rewardBoxes2);

        //compares a power equal to the one required
        assertEquals(0, smugglers1.battleResult(player1, 5));

        //compares a lower power than required
        assertEquals(-1, smugglers2.battleResult(player1, 5));

        //compares a higher power than required
        assertEquals(1, smugglers1.battleResult(player2, 8));
    }
}