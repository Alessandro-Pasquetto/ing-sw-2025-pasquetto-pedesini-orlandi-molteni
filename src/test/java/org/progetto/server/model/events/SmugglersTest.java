package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SmugglersTest {

    @Test
    void getFirePowerRequired() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(5, smugglers.getFirePowerRequired());
    }

    @Test
    void getPenaltyBoxes() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(2, smugglers.getPenaltyBoxes());
    }

    @Test
    void getPenaltyDays() {
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, new ArrayList<>());
        assertEquals(-3, smugglers.getPenaltyDays());
    }

    @Test
    void getRewardBoxes() {
        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(Box.RED);
        rewardBoxes.add(Box.GREEN);
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, rewardBoxes);
        assertEquals(rewardBoxes, smugglers.getRewardBoxes());
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
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
        Smugglers smugglers1 = new Smugglers(CardType.SMUGGLERS,2, "imgPath", 5, 2, -3, rewardBoxes1);
        Smugglers smugglers2 = new Smugglers(CardType.SMUGGLERS,2, "imgPath", 6, 2, -2, rewardBoxes2);
        BoxStorage boxStorage1 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BoxStorage boxStorage2 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Box boxR = Box.RED;
        Box boxY = Box.YELLOW;
        Box boxG = Box.GREEN;
        Box boxB = Box.BLUE;

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
        Spaceship s = new Spaceship(1, 0);
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, new ArrayList<>());

        BoxStorage bs1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1,1,1,1}, "", 3);
        bs1.addBox(s, Box.YELLOW,2);
        bs1.addBox(s, Box.GREEN,1);

        for (int i = 0; i < bs1.getCapacity(); i++) {
            if(bs1.getBoxStorage()[i] == null)
                System.out.print("NULL, ");
            else
                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
        }
        System.out.println();

        assertFalse(smugglers.chooseDiscardedBox(s, bs1, 0));
        assertFalse(smugglers.chooseDiscardedBox(s, bs1, 1));
        assertNotEquals(null, (bs1.getBoxStorage()[1]));
        assertTrue(smugglers.chooseDiscardedBox(s, bs1, 2));
        assertFalse(smugglers.chooseDiscardedBox(s, bs1, 3));
        assertTrue(smugglers.chooseDiscardedBox(s, bs1, 1));
        assertNull(bs1.getBoxStorage()[2]);

//        for (int i = 0; i < bs1.getCapacity(); i++) {
//            if(bs1.getBoxStorage()[i] == null)
//                System.out.print("NULL, ");
//            else
//                System.out.print(bs1.getBoxStorage()[i].getValue() + ", ");
//        }
//        System.out.println();


        BoxStorage bs2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "", 3);

        bs2.addBox(s, Box.GREEN,0);
        bs2.addBox(s, Box.RED,2);

//        for (int i = 0; i < bs2.getCapacity(); i++) {
//            if(bs2.getBoxStorage()[i] == null)
//                System.out.print("NULL, ");
//            else
//                System.out.print(bs2.getBoxStorage()[i].getValue() + ", ");
//        }
//        System.out.println();

        assertFalse(smugglers.chooseDiscardedBox(s, bs2, 0));
        assertFalse(smugglers.chooseDiscardedBox(s, bs2, 1));
        assertTrue(smugglers.chooseDiscardedBox(s, bs2, 2));
        assertFalse(smugglers.chooseDiscardedBox(s, bs2, 3));

        BoxStorage bs3 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "", 3);

        bs3.addBox(s, Box.GREEN,0);
        bs3.addBox(s, Box.BLUE,1);

        assertTrue(smugglers.chooseDiscardedBox(s, bs2, 0));
        assertFalse(smugglers.chooseDiscardedBox(s, bs3, 1));
        assertTrue(smugglers.chooseDiscardedBox(s, bs3, 0));
        assertFalse(smugglers.chooseDiscardedBox(s, bs1, 2));
        assertTrue(smugglers.chooseDiscardedBox(s, bs3, 1));
        assertFalse(smugglers.chooseDiscardedBox(s, bs3, 2));
    }

    @Test
    void chooseDiscardedBattery() {

        Player player1 = new Player("Max", 0, 1);

        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(Box.RED);
        rewardBoxes.add(Box.GREEN);
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS, 2,"imgPath", 5, 2, -3, rewardBoxes);
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(player1.getSpaceship(),2);

        // Returns false if component is not a Housing Unit
        assertFalse(smugglers.chooseDiscardedBattery(player1.getSpaceship(),(BatteryStorage) notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(smugglers.chooseDiscardedBattery(player1.getSpaceship(),battery));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(smugglers.chooseDiscardedBattery(player1.getSpaceship(),battery));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(smugglers.chooseDiscardedBattery(player1.getSpaceship(),battery));
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
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
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
        rewardBoxes1.add(Box.RED);
        rewardBoxes1.add(Box.GREEN);
        ArrayList<Box> rewardBoxes2 = new ArrayList<>();
        rewardBoxes2.add(Box.YELLOW);
        rewardBoxes2.add(Box.BLUE);
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