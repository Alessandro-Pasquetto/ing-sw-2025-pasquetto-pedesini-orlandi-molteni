package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SabotageTest {

    @Test
    void lessPopulatedSpaceship() {
        Player p1 = new Player("gino", 0, 1);
        Player p2 = new Player("alba", 1, 1);
        Player p3 = new Player("andrea", 2, 1);
        Player p4 = new Player("arianna", 3, 1);

        p1.getSpaceship().addCrewCount(4);
        p1.getSpaceship().setAlienOrange(true);
        p1.getSpaceship().setAlienPurple(true);

        p2.getSpaceship().addCrewCount(4);
        p2.getSpaceship().setAlienOrange(true);
        p2.getSpaceship().setAlienPurple(false);
        p2.setPosition(1);

        p3.getSpaceship().addCrewCount(4);
        p3.getSpaceship().setAlienOrange(true);
        p3.getSpaceship().setAlienPurple(false);
        p3.setPosition(2);

        p4.getSpaceship().addCrewCount(6);

        ArrayList <Player> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);

        Sabotage sabotage = new Sabotage(CardType.SABOTAGE,2, "imgPath");

        // Asserts that p3 is the player with the less populated ship
        assertEquals(p3, sabotage.lessPopulatedSpaceship(players));
    }

    @Test
    void penalty() {
        Board board = new Board(1);
        Player player = new Player("gino", 0, 1);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        // Added components
        board.addTraveler(player, 1);
        board.movePlayerByDistance(player, 5);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 2, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.placeComponent(1, 2, 0);

        Sabotage sabotage = new Sabotage(CardType.SABOTAGE, 2,"imgPath");


        // Tries carry out the penalty on not occupied cell
        assertFalse(sabotage.penalty(1, 1, player));

        // Tries carry out the penalty on not fillable cell
        assertFalse(sabotage.penalty(0, 0, player));

        // Tries carry out the penalty outside the matrix
        assertFalse(sabotage.penalty(12, 0, player));
        assertFalse(sabotage.penalty(-1, 0, player));
        assertFalse(sabotage.penalty(0, 12, player));
        assertFalse(sabotage.penalty(0, -1, player));

        // Carries out the penalty on occupied cell, destroying the component
        assertTrue(sabotage.penalty(1, 2, player));
        assertNull(buildingBoard.getSpaceshipMatrix()[1][2]);
    }
}