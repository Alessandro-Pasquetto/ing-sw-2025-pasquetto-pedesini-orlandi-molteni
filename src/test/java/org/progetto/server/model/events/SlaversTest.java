package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import static org.junit.jupiter.api.Assertions.*;

class SlaversTest {

    @Test
    void getFirePowerRequired() {
        Slavers slavers = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        assertEquals(5, slavers.getFirePowerRequired());
    }

    @Test
    void getPenaltyCrew() {
        Slavers slavers = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        assertEquals(2, slavers.getPenaltyCrew());
    }

    @Test
    void getPenaltyDays() {
        Slavers slavers = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        assertEquals(-3, slavers.getPenaltyDays());
    }

    @Test
    void getRewardCredits() {
        Slavers slavers = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        assertEquals(3, slavers.getRewardCredits());
    }

    @Test
    void chooseDiscardedCrew() {

        Player mario = new Player("mario",0,2);

        HousingUnit notHouse = new HousingUnit(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit crew = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit orange = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit purple = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Slavers slavers = new Slavers(CardType.SLAVERS, 2,"imgPath", 5, 2, -3, 3);
        crew.incrementCrewCount(mario.getSpaceship(),2);

        //returns false if component is not a Housing Unit
        assertFalse(slavers.chooseDiscardedCrew(mario.getSpaceship(),notHouse));

        //removes one crew member from the Housing Unit
        assertTrue(slavers.chooseDiscardedCrew(mario.getSpaceship(),crew));
        assertEquals(1, crew.getCrewCount());
        //removes an orange alien
        orange.setAlienOrange(true);
        assertTrue(slavers.chooseDiscardedCrew(mario.getSpaceship(),orange));
        assertFalse(crew.getHasOrangeAlien());

        //removes a purple alien
        purple.setAlienPurple(true);
        assertTrue(slavers.chooseDiscardedCrew(mario.getSpaceship(),purple));
        assertFalse(crew.getHasPurpleAlien());
    }

    @Test
    void chooseDiscardedBattery() {

        Player mario = new Player("mario",0,2);

        Slavers slavers = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(mario.getSpaceship(),2);

        // Returns false if component is not a Housing Unit
        assertFalse(slavers.chooseDiscardedBattery(mario.getSpaceship(),(BatteryStorage) notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(slavers.chooseDiscardedBattery(mario.getSpaceship(),battery));
        assertEquals(1, battery.getItemsCount());

        // Remove another battery from the storage
        assertTrue(slavers.chooseDiscardedBattery(mario.getSpaceship(), battery));
        assertEquals(0, battery.getItemsCount());

        // Tries to remove another battery from an empty storage
        assertFalse(slavers.chooseDiscardedBattery(mario.getSpaceship(),battery));
        assertEquals(0, battery.getItemsCount());
    }

    @Test
    void rewardPenalty() {
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Board board = new Board(1);
        board.addTraveler(player1, 1);
        board.addTraveler(player2, 1);
        Player[] track;
        track = board.getTrack();
        Slavers slavers1 = new Slavers(CardType.SLAVERS,2, "imgPath", 5, 2, -3, 3);
        Slavers slavers2 = new Slavers(CardType.SLAVERS,2, "imgPath", 6, 3, -2, 4);

        slavers1.rewardPenalty(board, player1);

        //adds 3 credits to player1
        assertEquals(3, player1.getCredits());

        //moves player1 back 3
        assertEquals(0, player1.getPosition());

        slavers2.rewardPenalty(board, player2);

        //adds 2 credits to player2
        assertEquals(4, player2.getCredits());

        //moves player2 back 2
        assertEquals(0, player1.getPosition());
    }

    @Test
    void battleResult() {
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Slavers slavers1 = new Slavers(CardType.SLAVERS, 2,"imgPath", 5, 2, -3, 3);
        Slavers slavers2 = new Slavers(CardType.SLAVERS, 2,"imgPath", 6, 3, -2, 4);

        //compares a power equal to the one required
        assertEquals(0, slavers1.battleResult(player1, 5));

        //compares a lower power than required
        assertEquals(-1, slavers2.battleResult(player1, 5));

        //compares a higher power than required
        assertEquals(1, slavers1.battleResult(player2, 8));
    }
}