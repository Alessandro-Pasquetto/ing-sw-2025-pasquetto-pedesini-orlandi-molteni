package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
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
import org.progetto.server.model.events.OpenSpace;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class EpidemicControllerTest {

    @Test
    void epidemicController() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Epidemic epidemic = new Epidemic(CardType.EPIDEMIC, 2, "imgPath");
        gameManager.getGame().setActiveEventCard(epidemic);

        Player player = new Player("mario", 0, 1);

        gameManager.getGame().addPlayer(player);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {

            }
        };

        gameManager.addSender(player, sender);

        gameManager.getGame().getBoard().addTraveler(player);

        // Added components
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        HousingUnit temp;

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(2, 1, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[1][2];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new Component(ComponentType.STRUCTURAL_UNIT, new int[]{0, 3, 3, 3}, "imgPath"));
        buildingBoard.placeComponent(3, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(4, 2, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[2][4];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(4, 3, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][4];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(3, 3, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][3];
        temp.incrementCrewCount(player.getSpaceship(), 1);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 0, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 2, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[2][1];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new Component(ComponentType.PURPLE_HOUSING_UNIT, new int[]{0, 3, 3, 0}, "imgPath"));
        buildingBoard.placeComponent(0, 2, 0);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 3, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][0];
        temp.setAlienPurple(true);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath", 2));
        buildingBoard.placeComponent(0, 4, 0);
        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[4][0];
        temp.incrementCrewCount(player.getSpaceship(), 2);

        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 0, 3, 3}, "imgPath", 2));
        buildingBoard.placeComponent(1, 3, 0);

        buildingBoard.setHandComponent(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{3, 0, 0, 0}, "imgPath"));
        buildingBoard.placeComponent(1, 4, 0);

        temp = (HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][1];
        temp.setAlienOrange(true);

        // Controller
        EpidemicController controller = new EpidemicController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                try {
                    controller.start();
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(1, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[1][2]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[2][4]).getCrewCount());
        assertEquals(0, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][4]).getCrewCount());
        assertEquals(1, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][3]).getCrewCount());
        assertEquals(2, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[2][1]).getCrewCount());
        assertFalse(((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][0]).getHasPurpleAlien());
        assertFalse(((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[3][1]).getHasOrangeAlien());
        assertEquals(1, ((HousingUnit) buildingBoard.getCopySpaceshipMatrix()[4][0]).getCrewCount());
    }
}