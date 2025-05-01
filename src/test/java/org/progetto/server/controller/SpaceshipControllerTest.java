package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.messages.toClient.Building.DestroyedComponentMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.LostShip;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class SpaceshipControllerTest {

    @Test
    void updateSpaceship() throws RemoteException {

        Sender sender = null;

        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario", 0, 1);

        //component updatable
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals(message, "SpaceshipUpdated");

            }
        };
        Component component = new Component(ComponentType.HOUSING_UNIT, new int[]{1, 1, 2, 1}, "imgSrc");
        SpaceshipController.updateSpaceship(gameManager, player, component, sender);

        //not updatable component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals(message, "NotAnUpdatableComponent");

            }
        };
        component = new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgSrc");
        SpaceshipController.updateSpaceship(gameManager, player, component, sender);
    }

    @Test
    void moveBox() throws RemoteException, InterruptedException {
        //spaceship setup
        Sender sender = null;
        Component component = null;
        BoxStorage storage = null;
        Player player = new Player("mario", 0, 1);
        GameManager gameManager = new GameManager(0, 4, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setActivePlayer(player);
        gameManager.getGame().setActiveEventCard(new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 1, 1, 1));
        gameManager.createEventController();
        gameManager.getEventController().start();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //place red_box storage on the left of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a yellow box at 0 and a red box at 1
        storage.addBox(player.getSpaceship(), Box.YELLOW, 0);
        storage.addBox(player.getSpaceship(), Box.RED, 1);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(1, 2, 0);

        //place red_box storage under the central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a red box at 0
        storage.addBox(player.getSpaceship(), Box.RED, 0);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(2, 3, 0);

        //place box storage on the right of central-unit
        storage = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a green box at 0
        storage.addBox(player.getSpaceship(), Box.GREEN, 0);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(3, 2, 0);

        //place a housing unit on the top of central-unit
        buildingBoard.setHandComponent(new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgSrc"));
        buildingBoard.placeComponent(2, 1, 0);


        //test incorrect phase
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("PermissionDenied", message);}
        };
        SpaceshipController.moveBox(gameManager, player, 2, 1, 0, 2, 3, 0, sender);

        //set correct phase
        gameManager.getEventController().setPhase(EventPhase.CHOOSE_BOX);

        //test move to a noStorage component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotAStorageComponent", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 0, 1, 2, 0, sender);

        //test move in the same component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("BoxAlreadyThere", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 0, 2, 1, 0, sender);

        //test move redBox in a non RedBoxStorage component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("CantStoreInANonRedStorage", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 1, 2, 3, 1, sender);


        //test invalid idx in endBox for red_box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("RedBoxNotMoved", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 1, 3, 2, 5, sender);


        //test invalid idx in endBox for box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("BoxNotMoved", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 0, 2, 3, 5, sender);

       //test invalid coordinates
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("InvalidCoordinates", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 22, 11, 0, 25, 34, 5, sender);

        //test correct movement of a box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                if(!message.equals("SpaceshipUpdated"))
                    assertEquals("BoxMoved", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 0, 3, 2, 1, sender);

        //test correct movement of a red_box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                if(!message.equals("SpaceshipUpdated"))
                    assertEquals("RedBoxMoved", message);
            }};
        SpaceshipController.moveBox(gameManager, player, 2, 1, 1, 3, 2, 2, sender);
    }

    @Test
    void removeBox() throws RemoteException, InterruptedException {
        //spaceship setup
        Sender sender = null;
        Component component = null;
        BoxStorage storage = null;
        Player player = new Player("mario", 0, 1);
        GameManager gameManager = new GameManager(0, 4, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setActivePlayer(player);
        gameManager.getGame().setActiveEventCard(new LostShip(CardType.LOSTSHIP, 1, "imgSrc", 1, 1, 1));
        gameManager.createEventController();
        gameManager.getEventController().start();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //place red_box storage on the left of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a yellow box at 0 and a red box at 1
        storage.addBox(player.getSpaceship(), Box.YELLOW, 0);
        storage.addBox(player.getSpaceship(), Box.RED, 1);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(1, 2, 0);

        //place red_box storage under the central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a red box at 0
        storage.addBox(player.getSpaceship(), Box.RED, 0);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(2, 3, 0);

        //place box storage on the right of central-unit
        storage = new BoxStorage(ComponentType.BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        //it contains a green box at 0
        storage.addBox(player.getSpaceship(), Box.GREEN, 0);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(3, 2, 0);

        //place a housing unit on the top of central-unit
        buildingBoard.setHandComponent(new Component(ComponentType.HOUSING_UNIT, new int[]{3, 3, 3, 3}, "imgSrc"));
        buildingBoard.placeComponent(2, 1, 0);

        //test incorrect phase
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("PermissionDenied", message);}
        };
        SpaceshipController.removeBox(gameManager, player, 2, 1, 0, sender);

        //set correct phase
        gameManager.getEventController().setPhase(EventPhase.CHOOSE_BOX);

        //test invalid coordinates
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("InvalidCoordinates", message);
            }};
        SpaceshipController.removeBox(gameManager, player, 20, 10, 55, sender);

        //test remove from a noStorage component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotAStorageComponent", message);
            }};
        SpaceshipController.removeBox(gameManager, player, 1, 2, 0, sender);


        //test invalid idx for box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("BoxNotRemoved", message);
            }};
        SpaceshipController.removeBox(gameManager, player, 2, 1, 100, sender);

        //test valid removal of a box
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                if(!message.equals("SpaceshipUpdated"))
                    assertEquals("BoxRemoved", message);
            }};
        SpaceshipController.removeBox(gameManager, player, 2, 1, 1, sender);
    }

    @Test
    void destroyComponentAndCheckValidity() throws RemoteException, InterruptedException {
        //spaceship setup
        Sender sender = null;
        Component component = null;
        BoxStorage storage = null;
        Player player = new Player("mario", 0, 1);
        GameManager gameManager = new GameManager(0, 4, 1);
        gameManager.getGame().addPlayer(player);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //place red_box storage on the left of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(1, 2, 0);

        //place red_box storage under the central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(2, 3, 0);

        //place red_box storage on the right of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(3, 2, 0);

        //test empty component cell
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EmptyComponentCell", message);
            }};
        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, 0, 0, sender);

        //test correct removal of the component and spaceship validity
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertInstanceOf(DestroyedComponentMessage.class, message);
            }};
        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, 1, 2, sender);

        //test correct removal and spaceship not valid
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                if(!(message instanceof DestroyedComponentMessage))
                    assertEquals("AskSelectSpaceshipPart", message);
            }};
        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, 2, 2, sender);
    }

    @Test
    void startDestroyComponentAndCheckValidity() throws RemoteException, InterruptedException {
        //spaceship setup
        Sender sender = null;
        Component component = null;
        BoxStorage storage = null;
        Player player = new Player("mario", 0, 1);
        GameManager gameManager = new GameManager(0, 4, 1);
        gameManager.getGame().addPlayer(player);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //place red_box storage on the left of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(1, 2, 0);

        //place red_box storage under the central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(2, 3, 0);

        //place red_box storage on the right of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(3, 2, 0);

        gameManager.getGame().setPhase(GamePhase.ADJUSTING);

        //test empty component cell
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EmptyComponentCell", message);
            }};
        SpaceshipController.startDestroyComponent(gameManager, player, 0, 0, sender);

        //test correct removal of the component
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("ImpossibleToDestroyCorrectlyPlaced", message);
            }};
        SpaceshipController.startDestroyComponent(gameManager, player, 1, 2, sender);
    }

    @Test
    void chooseSpaceshipPartToKeep() throws RemoteException {

        //spaceship setup
        Sender sender = null;
        Component component = null;
        BoxStorage storage = null;
        Player player = new Player("mario", 0, 1);
        GameManager gameManager = new GameManager(0, 4, 1);
        gameManager.getGame().addPlayer(player);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //place red_box storage on the left of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(1, 2, 0);

        //place red_box storage under the central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(2, 3, 0);

        //place red_box storage on the right of central-unit
        storage = new BoxStorage(ComponentType.RED_BOX_STORAGE, new int[]{3, 3, 3, 3}, "imgSrc", 3);
        buildingBoard.setHandComponent(storage);
        buildingBoard.placeComponent(3, 2, 0);

        //Test invalid coordinates
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotValidCoordinates", message);
            }
        };

        try{
            SpaceshipController.chooseSpaceshipPartToKeep(gameManager, player, -1, 66, sender);
        } catch (RemoteException e) {
            sender.sendMessage(e.getMessage());
        }


        //Test valid coordinates
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {}
        };
        Sender finalSender = sender ;

        try {
            assertDoesNotThrow(() -> SpaceshipController.chooseSpaceshipPartToKeep(gameManager, player, 2, 2, finalSender));
        }catch (IllegalStateException e) {
            sender.sendMessage(e.getMessage());
        }

    }
}