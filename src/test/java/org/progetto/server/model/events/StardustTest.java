package org.progetto.server.model.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import static org.junit.jupiter.api.Assertions.*;

class StardustTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getPenaltyDays() {
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
        buildingBoard.placeComponent(1, 2, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 3, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard.placeComponent(2, 1, 0);

        // Checks spaceship validity
        buildingBoard.checkShipValidity();

        Stardust stardust = new Stardust(CardType.STARDUST,2, "imgSrc");

        // Calls penalty method
        stardust.penalty(board, player);

        assertEquals(player, board.getTrack()[2]);

//        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();
//        for (int i = 0; i < spaceshipMatrix.length; i++) {
//            System.out.println();
//            for (int j = 0; j < spaceshipMatrix[0].length; j++) {
//                String value = (spaceshipMatrix[i][j] == null) ? "NULL" : spaceshipMatrix[i][j].getType().toString() + "-" + spaceshipMatrix[i][j].getRotation();
//                System.out.printf("%-20s", value);
//            }
//        }
//        System.out.println(buildingBoard.checkShipValidity());
//        System.out.println(player.getSpaceship().getExposedConnectorsCount());
    }
}