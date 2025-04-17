package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.client.connection.socket.SocketWriter;
import org.progetto.messages.toClient.EventCommon.CrewToDiscardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.LostShip;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LostShipControllerTest {

    @Test
    void start() {
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        player.getSpaceship().getBuildingBoard().setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2));
        player.getSpaceship().getBuildingBoard().placeComponent(2, 1, 0);

        LostShipController controller = new LostShipController(gameManager);
        assertDoesNotThrow(controller::start);
    }

    @Test
    void getRewardDecision() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        LostShipController controller = new LostShipController(gameManager);
        controller.phase = EventPhase.REWARD_DECISION;
        gameManager.getGame().setActivePlayer(player);

        Sender sender = message -> {};

        controller.getRewardDecision(player, "YES", sender);

        assertEquals(EventPhase.PENALTY_EFFECT, controller.phase);

        GameManager gameManager2 = new GameManager(0, 4, 1);
        Player player2 = new Player("mario", 0, 1);
        gameManager2.getGame().addPlayer(player2);

        LostShipController controller2 = new LostShipController(gameManager2);
        controller2.phase = EventPhase.REWARD_DECISION;
        gameManager2.getGame().setActivePlayer(player2);

        Sender sender2 = message -> {};

        controller2.getRewardDecision(player2, "NO", sender2);

        assertEquals(EventPhase.ASK_TO_LAND, controller2.phase);
    }

    @Test
    void penaltyEffect() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);
        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        gameManager.getGame().setActiveEventCard(lostShip);

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
                } catch (RemoteException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gameManager.setGameThread(gameThread);
        gameThread.start();

        Thread.sleep(200);
        assertEquals(EventPhase.REWARD_DECISION, controller.phase);

        controller.getRewardDecision(p2, "NO", sender);
        assertEquals(EventPhase.ASK_TO_LAND, controller.phase);
        Thread.sleep(200);

        // p3: Accepts, should go to DISCARD_CREW
        controller.getRewardDecision(p3, "YES", sender);
        assertEquals(EventPhase.DISCARDED_CREW, controller.getPhase());
    }

    @Test
    void receiveDiscardedCrew() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        LostShip lostShip = new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 3, 4, -2);
        gameManager.getGame().setActiveEventCard(lostShip);

        Player p3 = new Player("alessio", 0, 1);
        gameManager.getGame().addPlayer(p3);

        HousingUnit hu1 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);
        HousingUnit hu2 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);
        HousingUnit hu3 = new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1,1,1,1}, "img", 2);

        BuildingBoard bb = p3.getSpaceship().getBuildingBoard();
        bb.setHandComponent(hu1); bb.placeComponent(2, 1, 0);
        bb.setHandComponent(hu2); bb.placeComponent(1, 1, 0);
        bb.setHandComponent(hu3); bb.placeComponent(3, 1, 0);

        hu1.setCrewCount(2);
        hu2.setCrewCount(2);
        hu3.setCrewCount(2);

        p3.getSpaceship().addCrewCount(6);

        gameManager.getGame().setActivePlayer(p3);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        LostShipController controller = new LostShipController(gameManager);
        controller.phase = EventPhase.ASK_TO_LAND;
        controller.getRewardDecision(p3, "YES", sender);
        controller.phase = EventPhase.PENALTY_EFFECT;
        controller.penaltyEffect(p3, sender);
        controller.phase = EventPhase.DISCARDED_CREW;


        // Discarded crew (1 from each housing unit)
        controller.receiveDiscardedCrew(p3, 2, 1, sender);
        Sender sender1 = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertInstanceOf(CrewToDiscardMessage.class, message);
            }
        };
        controller.receiveDiscardedCrew(p3, 3, 1, sender);
        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertInstanceOf(CrewToDiscardMessage.class, message);
            }
        };
        controller.receiveDiscardedCrew(p3, 1, 1, sender);
        Sender sender3 = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("CrewMemberDiscarded", message);
            }
        };

        // Checks
        assertEquals(3, p3.getSpaceship().getCrewCount());
        assertEquals(1, hu1.getCrewCount());
        assertEquals(1, hu2.getCrewCount());
        assertEquals(1, hu3.getCrewCount());

        assertEquals(EventPhase.EFFECT, controller.phase);
    }
}