package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import static org.junit.jupiter.api.Assertions.*;

class LostShipTest {

    @Test
    void getPenaltyCrew() {
        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        assertEquals(3, lostShip.getPenaltyCrew());
    }

    @Test
    void getRewardCredits() {
        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        assertEquals(4, lostShip.getRewardCredits());
    }

    @Test
    void getPenaltyDays() {
        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        assertEquals(-2, lostShip.getPenaltyDays());
    }

    @Test
    void chooseDiscardedCrew() {

        Player mario = new Player("mario", 0 , 2);

        HousingUnit notHouse = new HousingUnit(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit crew = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit orange = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        HousingUnit purple = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        LostShip lostship = new LostShip(CardType.LOSTSHIP, 2, "imgPath", 1, 3, 3);
        crew.incrementCrewCount(mario.getSpaceship(), 2);

        //returns false if component is not a Housing Unit
        assertFalse(lostship.chooseDiscardedCrew(mario.getSpaceship(), notHouse));

        //removes one crew member from the Housing Unit
        assertTrue(lostship.chooseDiscardedCrew(mario.getSpaceship(), crew));
        assertEquals(1, crew.getCrewCount());

        //removes an orange alien
        orange.setAlienOrange(true);
        assertTrue(lostship.chooseDiscardedCrew(mario.getSpaceship(), orange));
        assertFalse(crew.getHasOrangeAlien());

        //removes a purple alien
        purple.setAlienPurple(true);
        assertTrue(lostship.chooseDiscardedCrew(mario.getSpaceship(), purple));
        assertFalse(crew.getHasPurpleAlien());
    }

    @Test
    void rewardPenalty() {
        Player player1 = new Player("Max", 0, 1);
        Player player2 = new Player("Mindy", 1, 2);
        Board board = new Board(1);
        board.addTraveler(player1);
        board.addTraveler(player2);
        board.addTravelersInTrack(1);

        LostShip lostShip1 = new LostShip(CardType.LOSTSHIP, 2, "imgPath", 1, 3, -3);
        LostShip lostShip2 = new LostShip(CardType.LOSTSHIP, 2, "imgPath", 1, 2, -2);

        lostShip1.rewardPenalty(board, player1);

        //adds 3 credits to player1
        assertEquals(3, player1.getCredits());

        //moves player1 back 3
        assertEquals(0, player1.getPosition());

        lostShip2.rewardPenalty(board, player2);

        //adds 2 credits to player2
        assertEquals(2, player2.getCredits());

        //moves player2 back 2
        assertEquals(0, player1.getPosition());
    }
}