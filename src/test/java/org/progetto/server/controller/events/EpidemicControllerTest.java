package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.Epidemic;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class EpidemicControllerTest {

    @Test
    void epidemicController() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Epidemic epidemic = new Epidemic(CardType.EPIDEMIC, 2, "imgPath");
        gameManager.getGame().setActiveEventCard(epidemic);

        Player player = new Player("mario");

        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }
        };

        gameManager.addSender(player, sender);

        gameManager.getGame().getBoard().addTraveler(player);

        // Added components
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        HousingUnit temp;

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(2, 1, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(4, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(4, 3, 0);


        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(3, 3, 0);


        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 0, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 2, 0);


        buildingBoard.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{0, 3, 3, 0}, "imgPath"));
        buildingBoard.placeComponent(0, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 3, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 4, 0);


        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 3, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath"));
        buildingBoard.placeComponent(1, 4, 0);

        buildingBoard.initSpaceshipParams();

        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][0];
        temp.setAlienPurple(true);

        temp = (HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][1];
        temp.setAlienOrange(true);

        // Controller
        EpidemicController controller = new EpidemicController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                controller.start();
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[1][2]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][4]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][4]).getCrewCount());
        assertEquals(2, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][3]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[2][1]).getCrewCount());
        assertFalse(((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][0]).getHasPurpleAlien());
        assertFalse(((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[3][1]).getHasOrangeAlien());
        assertEquals(1, ((HousingUnit) buildingBoard.getSpaceshipMatrixCopy()[4][0]).getCrewCount());
    }
}