package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.ServerDisconnectionDetection;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.LostShip;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class LostShipControllerTest {

    @Test
    void getRewardDecision() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);
        GameManager.setGameDisconnectionDetectionInterval(Integer.MAX_VALUE);

        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        gameManager.getGame().setActiveEventCard(lostShip);

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

            @Override
            public void sendPing() {
                ServerDisconnectionDetection.setPongIsArrived(this);
            }
        };

        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            @Override
            public void sendPing() {
                ServerDisconnectionDetection.setPongIsArrived(this);
            }
        };

        Sender sender3 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            @Override
            public void sendPing() {
                ServerDisconnectionDetection.setPongIsArrived(this);
            }
        };

        gameManager.addSender(p1, sender1);
        gameManager.addSender(p2, sender2);
        gameManager.addSender(p3, sender3);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        p1.getSpaceship().addCrewCount(0); // Not enough
        p2.getSpaceship().addCrewCount(4); // Refuses
        p3.getSpaceship().addCrewCount(6); // Accepts

        // Controller
        LostShipController controller = new LostShipController(gameManager);

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
        assertEquals(EventPhase.REWARD_DECISION, controller.getPhase());
        controller.reconnectPlayer(p2, sender2);
        controller.receiveRewardAndPenaltiesDecision(p2, "NO", sender2);

        Thread.sleep(200);
        // p3: Accepts, should go to DISCARD_CREW
        controller.receiveRewardAndPenaltiesDecision(p3, "YES", sender3);
        controller.reconnectPlayer(p3, sender3);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());
    }

    @Test
    void receiveDiscardedCrew() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 1, 1);
        GameManager.setGameDisconnectionDetectionInterval(Integer.MAX_VALUE);

        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        gameManager.getGame().setActiveEventCard(lostShip);

        Player p3 = new Player("alessio");
        gameManager.getGame().addPlayer(p3);
        gameManager.getGame().initPlayersSpaceship();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            @Override
            public void sendPing() {
                ServerDisconnectionDetection.setPongIsArrived(this);
            }
        };

        gameManager.addSender(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p3);

        HousingUnit hu1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        HousingUnit hu2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);
        HousingUnit hu3 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2);

        BuildingBoard bb = p3.getSpaceship().getBuildingBoard();
        bb.setHandComponent(hu1);
        bb.placeComponent(2, 1, 0);
        bb.setHandComponent(hu2);
        bb.placeComponent(1, 1, 0);
        bb.setHandComponent(hu3);
        bb.placeComponent(3, 1, 0);

        hu1.setCrewCount(2);
        hu2.setCrewCount(2);
        hu3.setCrewCount(2);

        p3.getSpaceship().addCrewCount(6);

        // Controller
        LostShipController controller = new LostShipController(gameManager);

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
        // p3: Accepts, should go to DISCARD_CREW
        controller.receiveRewardAndPenaltiesDecision(p3, "YES", sender);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());

        Thread.sleep(200);
        // Discarded crew (1 from each housing unit)
        controller.receiveDiscardedCrew(p3, 2, 1, sender);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());

        Thread.sleep(200);
        controller.receiveDiscardedCrew(p3, 3, 1, sender);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());

        Thread.sleep(200);
        controller.reconnectPlayer(p3, sender);
        controller.receiveDiscardedCrew(p3, 1, 1, sender);

        // Checks
        assertEquals(3, p3.getSpaceship().getCrewCount());
        assertEquals(1, hu1.getCrewCount());
        assertEquals(1, hu2.getCrewCount());
        assertEquals(1, hu3.getCrewCount());

        assertEquals(EventPhase.EFFECT, controller.getPhase());

        Thread.sleep(200);
        assertEquals(-2, p3.getPosition());
        assertEquals(4, p3.getCredits());
    }
}