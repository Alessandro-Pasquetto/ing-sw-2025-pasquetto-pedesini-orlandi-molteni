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
    void penalty() {
        Board board = new Board(2);
        Player player1 = new Player("gino", 0, 2);
        Player player2 = new Player("mario", 3, 2);
        Player player3 = new Player("anna", 1, 2);
        Player player4 = new Player("paola", 2, 2);

        board.addTraveler(player1);
        board.addTraveler(player2);
        board.addTraveler(player3);
        board.addTraveler(player4);

        board.addTravelersInTrack(2);

        BuildingBoard buildingBoard1 = player1.getSpaceship().getBuildingBoard();

        // Added components

        board.movePlayerByDistance(player1, 5);

        buildingBoard1.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard1.placeComponent(3, 1, 0);

        buildingBoard1.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard1.placeComponent(4, 2, 0);

        buildingBoard1.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 1, 2, 3}, "imgPath"));
        buildingBoard1.placeComponent(2, 2, 0);

        // Checks spaceship validity
        buildingBoard1.checkStartShipValidity();

        Stardust stardust = new Stardust(CardType.STARDUST,2, "imgSrc");

        // Calls penalty method
        stardust.penalty(board, player1);

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


        BuildingBoard buildingBoard2 = player2.getSpaceship().getBuildingBoard();
        BuildingBoard buildingBoard3 = player3.getSpaceship().getBuildingBoard();
        BuildingBoard buildingBoard4 = player4.getSpaceship().getBuildingBoard();

        board.movePlayerByDistance(player2, 3);
        board.movePlayerByDistance(player3, 0);
        board.movePlayerByDistance(player4, 1);

        // Added components
        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard2.placeComponent(3, 1, 0);

        buildingBoard2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 2, 0, 3}, "imgPath"));
        buildingBoard2.placeComponent(4, 2, 0);

        buildingBoard2.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 1}, "imgPath"));
        buildingBoard2.placeComponent(2, 2, 0);

        buildingBoard3.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 3, 3}, "imgPath"));
        buildingBoard3.placeComponent(3, 1, 0);

        buildingBoard3.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 3}, "imgPath"));
        buildingBoard3.placeComponent(4, 2, 0);

        buildingBoard3.setHandComponent(new Component(ComponentType.ENGINE, new int[]{0, 3, 0, 0}, "imgPath"));
        buildingBoard3.placeComponent(2, 2, 0);

        buildingBoard4.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 3, 0}, "imgPath"));
        buildingBoard4.placeComponent(3, 1, 0);

        buildingBoard4.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 3}, "imgPath"));
        buildingBoard4.placeComponent(4, 2, 0);

        buildingBoard4.setHandComponent(new Component(ComponentType.ENGINE, new int[]{0, 3, 0, 0}, "imgPath"));
        buildingBoard4.placeComponent(2, 2, 0);

        buildingBoard4.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 0, 0, 0}, "imgPath"));
        buildingBoard4.placeComponent(3, 3, 0);

        // Checks spaceship validity
        buildingBoard2.checkStartShipValidity();
        buildingBoard3.checkStartShipValidity();
        buildingBoard4.checkStartShipValidity();

        // Calls penalty method
        stardust.penalty(board, player2);

        stardust.penalty(board, player3);
        stardust.penalty(board, player4);

        // Check results
        assertEquals(player1, board.getTrack()[4]);
        assertEquals(player2, board.getTrack()[22]);
        assertEquals(player3, board.getTrack()[23]);
        assertEquals(player4, board.getTrack()[2]);
        assertEquals(4, player1.getPosition());
        assertEquals(-2, player2.getPosition());
        assertEquals(-1, player3.getPosition());
        assertEquals(2, player4.getPosition());

    }
}