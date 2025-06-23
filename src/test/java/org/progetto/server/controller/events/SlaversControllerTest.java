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
import org.progetto.server.model.events.Slavers;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class SlaversControllerTest {

    @Test
    void slaversControllerTest() throws InterruptedException, RemoteException {
        GameManager gameManager = new GameManager(0, 3, 1);
        Slavers slavers = new Slavers(CardType.SLAVERS, 2, "imgPath", 5, 2, -3, 3);
        gameManager.getGame().setActiveEventCard(slavers);

        Player p1 = new Player("mario");
        Player p2 = new Player("alice");
        Player p3 = new Player("alessio");

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        gameManager.getGame().initPlayersSpaceship();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }
        };

        gameManager.addSender(p1, sender);
        gameManager.addSender(p2, sender);
        gameManager.addSender(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        HousingUnit hu1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        HousingUnit hu2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        HousingUnit hu3 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);

        BuildingBoard bb = p1.getSpaceship().getBuildingBoard();
        bb.setHandComponent(hu1);
        bb.placeComponent(2, 1, 0);
        bb.setHandComponent(hu2);
        bb.placeComponent(1, 1, 0);
        bb.setHandComponent(hu3);
        bb.placeComponent(3, 1, 0);

        hu1.setCrewCount(2);
        hu2.setCrewCount(2);
        hu3.setCrewCount(2);

        p1.getSpaceship().addCrewCount(4);

        BatteryStorage batteryStorage2 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "img", 2);
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(batteryStorage2);
        bb2.placeComponent(2, 1, 0);

        p2.getSpaceship().addBatteriesCount(2);

        BatteryStorage batteryStorage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "img", 2);
        BuildingBoard bb3 = p3.getSpaceship().getBuildingBoard();
        bb3.setHandComponent(batteryStorage);
        bb3.placeComponent(2, 1, 0);

        batteryStorage.incrementItemsCount(p3.getSpaceship(), 2);
        p3.getSpaceship().addBatteriesCount(2);

        p1.getSpaceship().addNormalShootingPower(3);
        p2.getSpaceship().addNormalShootingPower(5);
        p2.getSpaceship().addFullDoubleCannonCount(1);
        p3.getSpaceship().addNormalShootingPower(5);
        p3.getSpaceship().addFullDoubleCannonCount(2);

        // Controller
        SlaversController controller = new SlaversController(gameManager);

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

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());

        Thread.sleep(200);
        // Discarded crew
        controller.receiveDiscardedCrew(p1, 2, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());
        controller.reconnectPlayer(p1, sender);
        controller.receiveDiscardedCrew(p1, 3, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.reconnectPlayer(p2, sender);
        controller.receiveHowManyCannonsToUse(p2, 0, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.receiveHowManyCannonsToUse(p3, 1, sender);
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());

        Thread.sleep(200);
        controller.reconnectPlayer(p3, sender);
        controller.receiveDiscardedBatteries(p3, 2, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.REWARD_DECISION, controller.getPhase());

        Thread.sleep(200);
        controller.reconnectPlayer(p3, sender);
        controller.receiveRewardDecision(p3, "YES", sender);

        Thread.sleep(200);
        assertEquals(EventPhase.EFFECT, controller.getPhase());

        assertEquals(3, p3.getCredits());
        assertEquals(-3, p3.getPosition());
    }
}