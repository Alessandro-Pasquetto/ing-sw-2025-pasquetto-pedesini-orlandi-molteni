package org.progetto.server.model.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import static org.junit.jupiter.api.Assertions.*;

class EpidemicTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void epidemicResult() {
        Game game = new Game(0, 3, 1);
        Board board = game.getBoard();

        Player player = new Player("gino");

        game.addPlayer(player);
        game.initPlayersSpaceship();

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        board.addTraveler(player);

        HousingUnit temp;

        // Added components
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(2, 1, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[1][2];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(4, 2, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][4];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(4, 3, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][4];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(3, 3, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][3];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 0, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 2, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][1];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{0, 3, 3, 0}, "imgPath"));
        buildingBoard.placeComponent(0, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 3, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][0];
        temp.setAlienPurple(true);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 4, 0);
        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[4][0];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 3, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath"));
        buildingBoard.placeComponent(1, 4, 0);

        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][1];
        temp.setAlienOrange(true);

        Epidemic epidemic = new Epidemic(CardType.EPIDEMIC, 2, "imgSrc");

        epidemic.epidemicResult(player);

        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[1][2]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][4]).getCrewCount());
        assertEquals(0, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][4]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][3]).getCrewCount());
        assertEquals(2, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][1]).getCrewCount());
        assertFalse(((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][0]).getHasPurpleAlien());
        assertFalse(((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][1]).getHasOrangeAlien());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[4][0]).getCrewCount());
    }
}