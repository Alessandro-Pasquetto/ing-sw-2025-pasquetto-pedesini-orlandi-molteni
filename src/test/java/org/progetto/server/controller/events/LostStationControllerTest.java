package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.LostStation;
import org.progetto.server.model.events.OpenSpace;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LostStationControllerTest {

    @Test
    void lostStationController() throws RemoteException, InterruptedException {
        GameManager gameManager = new GameManager(0, 3, 1);

        ArrayList<Box> boxes = new ArrayList<>();
        boxes.add(Box.RED);
        boxes.add(Box.YELLOW);
        boxes.add(Box.GREEN);
        boxes.add(Box.BLUE);

        LostStation lostStation = new LostStation(CardType.LOSTSTATION, 2, "imgPath", 3, boxes, -1);
        gameManager.getGame().setActiveEventCard(lostStation);

        Player p1 = new Player("mario", 0, 1);
        Player p2 = new Player("alice", 0, 1);
        Player p3 = new Player("alessio", 0, 1);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().addPlayer(p3);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {

            }
        };

        gameManager.addSender(p1, sender);
        gameManager.addSender(p2, sender);
        gameManager.addSender(p3, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);
        gameManager.getGame().getBoard().addTraveler(p3);

        Spaceship spaceship1 = p1.getSpaceship();
        Spaceship spaceship2 = p2.getSpaceship();
        Spaceship spaceship3 = p3.getSpaceship();

        spaceship1.addCrewCount(1);
        spaceship2.addCrewCount(3);
        spaceship3.addCrewCount(2);
        spaceship3.setAlienPurple(true);

        BuildingBoard bb3 = spaceship3.getBuildingBoard();
        BoxStorage boxStorage = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 4);
        bb3.setHandComponent(boxStorage);
        bb3.placeComponent(2, 1, 0);

        // Controller
        LostStationController controller = new LostStationController(gameManager);

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
        controller.receiveDecisionToLand(p2, "NO", sender);

        Thread.sleep(200);
        assertEquals(EventPhase.LAND, controller.getPhase());

        Thread.sleep(200);
        controller.receiveDecisionToLand(p3, "YES", sender);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 0, 2, 1, 0, sender);
        assertNull(boxStorage.getBoxStorage()[0]);

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 1, 2, 1, 0, sender);
        assertEquals(Box.YELLOW, boxStorage.getBoxStorage()[0]);

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 1, 2, 1, 0, sender);
        assertEquals(Box.YELLOW, boxStorage.getBoxStorage()[0]);

        Thread.sleep(200);
        controller.receiveRewardBox(p3, 1, 2, 1, 1, sender);
        assertEquals(Box.GREEN, boxStorage.getBoxStorage()[1]);

        Thread.sleep(200);
        controller.receiveRewardBox(p3, -1, 2, 1, 1, sender);
        assertEquals(-1, p3.getPosition());
    }
}