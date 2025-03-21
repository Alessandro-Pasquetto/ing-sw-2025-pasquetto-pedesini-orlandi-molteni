package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SlaversTest {

    @Test
    void getFirePowerRequired() {
    }

    @Test
    void getPenaltyCrew() {
    }

    @Test
    void getPenaltyDays() {
    }

    @Test
    void getRewardCredits() {
    }

    @Test
    void chooseDiscardedCrew() {
        HousingUnit notHouse = new HousingUnit(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit crew = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit orange = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit purple = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        Slavers slavers = new Slavers(CardType.SLAVERS, "imgPath", 5, 2, -3, 3);
        crew.incrementCrewCount(2);

        //returns false if component is not a Housing Unit
        assertFalse(slavers.chooseDiscardedCrew(notHouse));

        //removes one crew member from the Housing Unit
        assertTrue(slavers.chooseDiscardedCrew(crew));
        assertEquals(1, crew.getCrewCount());

        //removes an orange alien
        assertTrue(slavers.chooseDiscardedCrew(orange));
        assertFalse(crew.hasOrangeAlien());

        //removes a purple alien
        assertTrue(slavers.chooseDiscardedCrew(purple));
        assertFalse(crew.hasPurpleAlien());
    }

    @Test
    void chooseDiscardedBattery() {
        Slavers slavers = new Slavers(CardType.SLAVERS, "imgPath", 5, 2, -3, 3);
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(2);

        //returns false if component is not a Housing Unit
        assertFalse(slavers.chooseDiscardedBattery(notBattery));

        //removes one battery member from the Housing Unit
        assertTrue(slavers.chooseDiscardedBattery(battery));
        assertEquals(1, battery.getItemsCount());
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
        Slavers slavers1 = new Slavers(CardType.SLAVERS, "imgPath", 5, 2, -3, 3);
        Slavers slavers2 = new Slavers(CardType.SLAVERS, "imgPath", 6, 3, -2, 4);

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
        Slavers slavers1 = new Slavers(CardType.SLAVERS, "imgPath", 5, 2, -3, 3);
        Slavers slavers2 = new Slavers(CardType.SLAVERS, "imgPath", 6, 3, -2, 4);

        //compares a power equal to the one required
        assertEquals(0, slavers1.battleResult(player1, 5));

        //compares a lower power than required
        assertEquals(-1, slavers2.battleResult(player1, 5));

        //compares a higher power than required
        assertEquals(1, slavers1.battleResult(player2, 8));
    }
}