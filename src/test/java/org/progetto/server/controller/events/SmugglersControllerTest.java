package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.Slavers;
import org.progetto.server.model.events.Smugglers;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SmugglersControllerTest {

    @Test
    void smugglersControllerTest() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);
        ArrayList<Box> rewardBoxes = new ArrayList<>();
        rewardBoxes.add(Box.RED);
        rewardBoxes.add(Box.GREEN);
        Smugglers smugglers = new Smugglers(CardType.SMUGGLERS,2, "imgPath", 5, 2, -3, rewardBoxes);
        gameManager.getGame().setActiveEventCard(smugglers);

        Player p1 = new Player("mario", 0, 1);
        Player p2 = new Player("alice", 0, 1);
        Player p3 = new Player("alessio", 0, 1);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        VirtualClient sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        gameManager.addRmiClient(p1, sender);
        gameManager.addRmiClient(p2, sender);
        gameManager.addRmiClient(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        BoxStorage bx1 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BoxStorage bx2 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BoxStorage bx3 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);

        BuildingBoard bb = p1.getSpaceship().getBuildingBoard();
        bb.setHandComponent(bx1);
        bb.placeComponent(2, 1, 0);
        bb.setHandComponent(bx2);
        bb.placeComponent(1, 1, 0);
        bb.setHandComponent(bx3);
        bb.placeComponent(3, 1, 0);

        bx1.addBox(p1.getSpaceship(), Box.YELLOW , 0);
        bx1.addBox(p1.getSpaceship(), Box.BLUE, 1);
        bx2.addBox(p1.getSpaceship(), Box.RED, 0);
        bx2.addBox(p1.getSpaceship(), Box.GREEN, 1);
        bx3.addBox(p1.getSpaceship(), Box.YELLOW, 0);
        bx3.addBox(p1.getSpaceship(), Box.GREEN, 1);

        BatteryStorage batteryStorage2 = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();
        bb2.setHandComponent(batteryStorage2);
        bb2.placeComponent(2, 1, 0);

        p2.getSpaceship().addBatteriesCount(2);

        BatteryStorage batteryStorage = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BuildingBoard bb3 = p3.getSpaceship().getBuildingBoard();
        bb3.setHandComponent(batteryStorage);
        bb3.placeComponent(2, 1, 0);

        batteryStorage.incrementItemsCount(p3.getSpaceship(), 2);
        p3.getSpaceship().addBatteriesCount(2);

        BoxStorage bx4 = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);
        BoxStorage bx5 = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{1,1,1,1}, "img", 2);
        bb3.setHandComponent(bx4);
        bb3.placeComponent(1, 1, 0);
        bb3.setHandComponent(bx5);
        bb3.placeComponent(3, 1, 0);

        p1.getSpaceship().addNormalShootingPower(3);
        p2.getSpaceship().addNormalShootingPower(5);
        p2.getSpaceship().addFullDoubleCannonCount(1);
        p3.getSpaceship().addNormalShootingPower(5);
        p3.getSpaceship().addFullDoubleCannonCount(2);

        // Controller
        SmugglersController controller = new SmugglersController(gameManager);

        GameThread gameThread = new GameThread(gameManager) {

            @Override
            public void run(){
                try {
                    controller.start();
                } catch (RemoteException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());

        Thread.sleep(200);
        // Discarded boxes
        controller.receiveDiscardedBox(p1, 2, 1, 1, sender);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());

        Thread.sleep(200);
        // Discarded boxes
        controller.receiveDiscardedBox(p1, 1, 1, 0, sender);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());

        Thread.sleep(200);
        controller.receiveDiscardedBox(p1, 2, 1, 0, sender);
        assertEquals(EventPhase.DISCARDED_BOXES, controller.getPhase());

        Thread.sleep(200);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.receiveHowManyCannonsToUse(p2, 0, sender);

        Thread.sleep(500);
        assertEquals(EventPhase.CANNON_NUMBER, controller.getPhase());

        Thread.sleep(200);
        controller.receiveHowManyCannonsToUse(p3, 1, sender);
        assertEquals(EventPhase.DISCARDED_BATTERIES, controller.getPhase());

        Thread.sleep(200);
        controller.receiveDiscardedBatteries(p3, 2, 1, sender);

        Thread.sleep(200);
        assertEquals(EventPhase.REWARD_DECISION, controller.getPhase());

        Thread.sleep(200);
        controller.receiveRewardDecision(p3, "YES", sender);

        Thread.sleep(200);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 0, 1, 1, 0, sender);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 0, 3, 1, 0, sender);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        Thread.sleep(200);
        controller.leaveReward(p3, sender);
        assertEquals(EventPhase.PENALTY_DAYS, controller.getPhase());

        assertEquals(1, p3.getSpaceship().getBoxCounts()[0]);
        assertEquals(-3, p3.getPosition());
    }
}