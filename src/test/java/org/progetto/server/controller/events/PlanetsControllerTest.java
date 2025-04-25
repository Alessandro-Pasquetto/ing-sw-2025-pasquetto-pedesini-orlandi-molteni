package org.progetto.server.controller.events;

import org.junit.jupiter.api.Test;
import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.EventCommon.AvailableBoxesMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanetsControllerTest {

    @Test
    void planetsControllerTest() throws RemoteException, InterruptedException {

        //board setup
        GameManager gameManager = new GameManager(0, 2, 1);
        ArrayList<ArrayList<Box>> rewardsForPlanets = new ArrayList<>(
                List.of(
                        new ArrayList<>(List.of(new Box[]{Box.RED, Box.YELLOW})), //fist planet has a red box and a yellow box
                        new ArrayList<>(List.of(new Box[]{Box.GREEN, Box.BLUE})) //second planet has a green box and a blue box
                )
        );

        //two planets and 3 days of penalty
        Planets planets = new Planets(CardType.PLANETS, 2, "imgPath", rewardsForPlanets, -2);
        gameManager.getGame().setActiveEventCard(planets);

        Player p1 = new Player("mario", 0, 1);
        Player p2 = new Player("alice", 1, 1);

        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);

        VirtualClient sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        gameManager.addRmiClient(p1, sender);
        gameManager.addRmiClient(p2, sender);

        gameManager.getGame().getBoard().addTraveler(p1);
        gameManager.getGame().getBoard().addTraveler(p2);

        //player_1 spaceship setup
        BuildingBoard bb1 = p1.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath",3));
        bb1.placeComponent(1, 2, 0);

        bb1.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath",3));
        bb1.placeComponent(3, 2, 0);



        //player_2 spaceship setup
        BuildingBoard bb2 = p2.getSpaceship().getBuildingBoard();

        bb2.setHandComponent(new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath",3));
        bb2.placeComponent(1, 2, 0);

        bb2.setHandComponent(new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgPath",3));
        bb2.placeComponent(3, 2, 0);

        bb2.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgPath",2));
        bb2.placeComponent(2, 1, 0);

        // Controller
        PlanetsController controller = new PlanetsController(gameManager);

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
        assertEquals(EventPhase.LAND, controller.getPhase());

        Thread.sleep(200);

        //Testing first player

        //Test not your turn
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotYourTurn", message);

            }
        };
        controller.receiveDecisionToLandPlanet(p2, 0, sender);


        //Test index outOfBound
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailablePlanetsMessage))
                    assertEquals("PlanetIdxNotValid", message);
            }
        };
        controller.receiveDecisionToLandPlanet(p1, 5, sender);

        //Test landing completed
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {}
        };
        controller.receiveDecisionToLandPlanet(p1, 0, sender);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());


        //Test not valid coordinates
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("InvalidCoordinates", message);

            }
        };
        controller.receiveRewardBox(p1, 0, 10, 55, 0, sender);


        //Test first box chosen
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("BoxChosen", message);

            }
        };
        controller.receiveRewardBox(p1, 0, 3, 2, 0, sender);

        //Test second box chosen
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {}
        };
        controller.receiveRewardBox(p1, 0, 3, 2, 1, sender);
        assertEquals(EventPhase.ASK_TO_LAND, controller.getPhase());


        //Test second player

        //Test not your turn
        Thread.sleep(200);
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotYourTurn", message);

            }
        };
        controller.receiveDecisionToLandPlanet(p1, 0, sender);

        //Test second land on a taken planet
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailablePlanetsMessage))
                    assertEquals("PlanetAlreadyTaken", message);
            }
        };
        controller.receiveDecisionToLandPlanet(p2, 0, sender);

        //Test second land completed
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {}
        };
        controller.receiveDecisionToLandPlanet(p2, 1, sender);
        assertEquals(EventPhase.CHOOSE_BOX, controller.getPhase());


        //Test not a storage
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("InvalidCoordinates", message);

            }
        };
        controller.receiveRewardBox(p2, 0, 2, 2, 0, sender);


        //Test invalid box index
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("InvalidStorageIndex", message);

            }
        };
        controller.receiveRewardBox(p2, 5, 1, 2, 0, sender);


        //Test valid selection
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("BoxChosen", message);

            }
        };
        controller.receiveRewardBox(p2, 0, 1, 2, 0, sender);


        //Test wrong storage index
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("NotValidBoxContainer", message);

            }
        };
        controller.receiveRewardBox(p2, 0, 1, 2, 0, sender);

        //save starting position
        p1.setPosition(6);
        p2.setPosition(2);

        //Test second valid selection
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof AvailableBoxesMessage))
                    assertEquals("BoxChosen", message);

            }
        };
        controller.receiveRewardBox(p2, 0, 1, 2, 1, sender);


        //Test unable to select
        sender = new VirtualClient() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("IncorrectPhase", message);
            }
        };
        controller.receiveRewardBox(p2, 0, 1, 2, 1, sender);

        Thread.sleep(200);
        //Test correct effect application
        assertEquals(4, p1.getPosition());
        assertEquals(0, p2.getPosition());

    }


}