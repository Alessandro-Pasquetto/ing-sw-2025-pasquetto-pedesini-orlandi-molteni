package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.ServerDisconnectionDetection;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.rmi.RemoteException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PlanetsControllerTest {

    @Test
    void planetsControllerTest() throws RemoteException, InterruptedException {

        //board setup
        GameManager gameManager = new GameManager(0, 2, 1);
        GameManager.setGameDisconnectionDetectionInterval(Integer.MAX_VALUE);

        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>();

        ArrayList<Box> planet1 = new ArrayList<>();
        planet1.add(Box.RED);
        planet1.add(Box.YELLOW);

        ArrayList<Box> planet2 = new ArrayList<>();
        planet2.add(Box.GREEN);
        planet2.add(Box.BLUE);

        rewardsForPlanets.add(planet1);
        rewardsForPlanets.add(planet2);

        //two planets and 3 days of penalty
        Planets planets = new Planets(CardType.PLANETS, 2, "imgPath", rewardsForPlanets, -2);
        gameManager.getGame().setActiveEventCard(planets);

        Player p1 = new Player("mario");
        Player p2 = new Player("alice");

        //save starting position
        p1.setPosition(6);
        p2.setPosition(2);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);

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

        gameManager.addSender(p1, sender1);
        gameManager.addSender(p2, sender2);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);

        //player_1 spaceship setup
        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
        bb1.placeComponent(1, 2, 0);

        bb1.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
        bb1.placeComponent(3, 2, 0);

        //player_2 spaceship setup
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();

        bb2.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
        bb2.placeComponent(1, 2, 0);

        bb2.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 3));
        bb2.placeComponent(3, 2, 0);

        bb2.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath", 2));
        bb2.placeComponent(2, 1, 0);

        // Controller
        PlanetsController controller = new PlanetsController(gameManager);

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
        assertEquals(EventPhase.LAND, controller.getPhase());

        Thread.sleep(200);

        //Testing first player

        //Test not your turn
        controller.receiveDecisionToLandPlanet(p2, 0, sender2);

        //Test index outOfBound
        controller.receiveDecisionToLandPlanet(p1, 5, sender1);

        //Test landing completed
        controller.reconnectPlayer(p1, sender1);
        controller.receiveDecisionToLandPlanet(p1, 0, sender1);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        //Test not valid coordinates
        controller.receiveRewardBox(p1, 0, 10, 55, 0, sender1);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        //Test first box chosen
        controller.reconnectPlayer(p1, sender1);
        controller.receiveRewardBox(p1, 0, 3, 2, 0, sender1);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        //Test second box chosen
        controller.receiveRewardBox(p1, 0, 3, 2, 1, sender1);


        Thread.sleep(200);
        assertEquals(EventPhase.LAND, controller.getPhase());

        //Test second player

        //Test not your turn
        Thread.sleep(200);
        controller.receiveDecisionToLandPlanet(p1, 0, sender1);
        assertEquals(EventPhase.LAND, controller.getPhase());

        //Test second land on a taken planet
        controller.receiveDecisionToLandPlanet(p2, 0, sender2);
        assertEquals(EventPhase.LAND, controller.getPhase());

        //Test second land completed
        controller.receiveDecisionToLandPlanet(p2, 1, sender2);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        //Test not a storage
        controller.receiveRewardBox(p2, 0, 2, 2, 0, sender2);

        //Test invalid box index
        try {
            controller.receiveRewardBox(p2, 5, 1, 2, 0, sender2);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }

        //Test valid selection
        controller.receiveRewardBox(p2, 0, 1, 2, 0, sender2);

        //Test wrong storage index
        controller.receiveRewardBox(p2, 0, 1, 2, 0, sender2);

        Thread.sleep(200);
        //Test exit
        controller.receiveRewardBox(p2, -1, 1, 2, 0, sender2);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());

        Thread.sleep(200);
        assertEquals(EventPhase.EFFECT, controller.getPhase());

        //Test correct effect application
        assertEquals(4, p1.getPosition());
        assertEquals(0, p2.getPosition());
        assertEquals(1, p1.getSpaceship().getBoxCounts()[0]);
        assertEquals(1, p2.getSpaceship().getBoxCounts()[2]);
        assertEquals(0, p2.getSpaceship().getBoxCounts()[3]);
    }
}