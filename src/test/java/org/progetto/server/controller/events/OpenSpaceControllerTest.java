package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.OpenSpace;
import org.progetto.server.model.events.Slavers;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class OpenSpaceControllerTest {

    @Test
    void openSpaceController() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);
        GameManager.setGameDisconnectionDetectionInterval(Integer.MAX_VALUE);

        OpenSpace openspace = new OpenSpace(CardType.OPENSPACE, 2, "imgPath");
        gameManager.getGame().setActiveEventCard(openspace);

        Player p1 = new Player("mario");
        Player p2 = new Player("alice");
        Player p3 = new Player("alessio");

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        gameManager.getGame().initPlayersSpaceship();

        Sender sender1 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        Sender sender3 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        gameManager.addSender(p1, sender1);
        gameManager.addSender(p2, sender2);
        gameManager.addSender(p3, sender3);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        BatteryStorage batteryStorage2 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "img", 2);
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(batteryStorage2);
        bb2.placeComponent(2, 1, 0);

        batteryStorage2.incrementItemsCount(p2.getSpaceship(), 2);
        p2.getSpaceship().addBatteriesCount(2);

        BatteryStorage batteryStorage3 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "img", 2);
        BuildingBoard bb3 = p3.getSpaceship().getBuildingBoard();
        bb3.setHandComponent(batteryStorage3);
        bb3.placeComponent(2, 1, 0);

        batteryStorage3.incrementItemsCount(p3.getSpaceship(), 2);
        p3.getSpaceship().addBatteriesCount(2);

        p1.getSpaceship().addNormalEnginePower(3);
        p2.getSpaceship().addNormalEnginePower(5);
        p2.getSpaceship().addDoubleEngineCount(1);
        p3.getSpaceship().addNormalEnginePower(5);
        p3.getSpaceship().addDoubleEngineCount(2);

        // Controller
        OpenSpaceController controller = new OpenSpaceController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                try {
                    controller.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200); // Wait for the controller to enter the waiting state
        assertEquals(EventPhase.ENGINE_NUMBER, controller.getPhase());
        controller.reconnectPlayer(p2, sender2);
        controller.receiveHowManyEnginesToUse(p2, 0, sender2);

        Thread.sleep(200); // Wait for the controller to enter the waiting state
        assertEquals(EventPhase.ENGINE_NUMBER, controller.getPhase());
        controller.receiveHowManyEnginesToUse(p3, 2, sender3);

        Thread.sleep(200); // Wait for the controller to enter the waiting state
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());
        controller.receiveDiscardedBatteries(p3, 2, 1, sender3);

        Thread.sleep(200); // Wait for the controller to enter the waiting state
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());
        controller.reconnectPlayer(p3, sender3);
        controller.receiveDiscardedBatteries(p3, 2, 1, sender3);

        Thread.sleep(200);
        assertEquals(EventPhase.EFFECT, controller.getPhase());

        assertEquals(3, p1.getPosition());
        assertEquals(6, p2.getPosition());
        assertEquals(11, p3.getPosition());
    }
}