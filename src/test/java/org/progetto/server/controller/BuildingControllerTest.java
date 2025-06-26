package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.messages.toClient.Building.PickedUpEventCardDeckMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingControllerTest {

    @Test
    void pickHiddenComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Game game = gameManager.getGame();

        Player player = new Player("mario");
        game.addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());

        assertDoesNotThrow(() -> BuildingController.pickHiddenComponent(gameManager, player, sender));

        assertNotNull(player.getSpaceship().getBuildingBoard().getHandComponent());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.pickHiddenComponent(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.pickHiddenComponent(gameManager, player, sender);
    }

    @Test
    void pickVisibleComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        Player p2 = new Player("chiara");

        gameManager.getGame().addPlayer(player);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
            }

            public void sendPing() {}
        };

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();
        gameManager.getGame().discardComponent(player);

        // Call pickVisibleComponent with the component index and ensure no exceptions are thrown
        assertDoesNotThrow(() -> BuildingController.pickVisibleComponent(gameManager, player, 0, sender));

        // Check if the player’s hand now contains the picked visible component
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), component);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.pickVisibleComponent(gameManager, player, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.pickVisibleComponent(gameManager, player, 0, sender);

        p2.getSpaceship().getBuildingBoard().setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "img", 2));
        BuildingController.pickVisibleComponent(gameManager, p2, 0, sender);

        gameManager.getGame().discardComponent(p2);
        BuildingController.pickVisibleComponent(gameManager, p2, 3 , sender);
    }

    @Test
    void placeComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        final String[] lastMessage = new String[1];

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){
                lastMessage[0] = msg.toString();
            }

            public void sendPing() {}
        };

        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingController.placeComponent(gameManager, player, 2, 1, 0, sender);

        Component component = new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath");
        buildingBoard.setHandComponent(component);

        BuildingController.placeComponent(gameManager, player, 4, 4, 0, sender);
        assertDoesNotThrow(() -> BuildingController.placeComponent(gameManager, player, 2, 1, 0, sender));

        assertEquals("AllowedToPlaceComponent", lastMessage[0]);

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeComponent(gameManager, player, 2, 1, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeComponent(gameManager, player, 2, 1, 0, sender);
    }

    @Test
    void placeLastComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                // Mock implementation
            }

            public void sendPing() {}
        };

        BuildingController.placeLastComponent(gameManager, player, 2, 1, 0, sender);

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        BuildingController.placeLastComponent(gameManager, player, 4, 4, 0, sender);
        assertDoesNotThrow(() -> BuildingController.placeLastComponent(gameManager, player, 2, 1, 0, sender));

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        // Check that the component was placed and the player's hand component is now null
        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeLastComponent(gameManager, player, 2, 1, 0, sender);

    }

    @Test
    void placeHandComponentAndPickHiddenComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 2, 1, 0, sender);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 4, 4, 0, sender);

        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 2, 1, 0, sender));

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component2 = buildingBoard.getHandComponent();

        assertNotNull(component2);
        assertNotSame(component, component2);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 2, 1, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 2, 1, 0, sender);
    }

    @Test
    void placeHandComponentAndPickVisibleComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");

        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        // Set up a visible component in the game for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component visibleComponent = buildingBoard.getHandComponent();
        gameManager.getGame().discardComponent(player);

        BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 2, 1, 0, 0, sender);

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 4, 4, 0, 0, sender);

        // Place the hand component and pick a visible component
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 2, 1, 0, 0, sender));

        // Check that the component was placed and the player picked up the visible component
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), visibleComponent);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 2, 1, 0, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 2, 1, 0, 0, sender);
    }

    @Test
    void placeHandComponentAndPickUpEventCardDeck() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));

        //Test unable to pick for level 1
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("CannotPickUpEventCardDeck", message);

            }

            public void sendPing() {}
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);


        //Test empty hand
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        player.getSpaceship().getBuildingBoard().setHandComponent(null);
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EmptyHandComponent", message);

            }

            public void sendPing() {}
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);


        //Test deck already taken
        Player otherPLayer = new Player("Giovanni");
        gameManager.getGame().addPlayer(otherPLayer);
        gameManager.getGame().initPlayersSpaceship();

        BuildingBoard bb1 = player.getSpaceship().getBuildingBoard();
        BuildingBoard bb2 = otherPLayer.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        bb2.setHandComponent(new Component(ComponentType.BOX_STORAGE, new int[]{1, 1, 2, 1}, "imgPath"));

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, otherPLayer, 2, 3, 0, 0, sender);

        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);

        //Test illegal placement
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 1, sender);

        //Test correct placing and picking
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 1, 0, 1, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 1, 0, 1, sender);
    }

    @Test
    void placeHandComponentAndPickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 2, 1, 0, 0, sender);

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();
        buildingBoard.setAsBooked(0);

        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));

        BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 4, 4, 0, 0, sender);

        // Simulate the action of placing the hand component and picking up the booked component
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 2, 1, 0, 0, sender));

        // Assert that the booked component has been placed
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), bookedComponent);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 2, 1, 0, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 2, 1, 0, 0, sender);
    }

    @Test
    void placeHandComponentAndReady() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        BuildingController.placeHandComponentAndReady(gameManager, player, 2, 1, 0, sender);

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        BuildingController.placeHandComponentAndReady(gameManager, player, 4, 4, 0, sender);

        // Simulate the action of placing the hand component and marking it as ready
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndReady(gameManager, player, 2, 1, 0, sender));

        // Placed component
        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        // Assert that the player is ready
        assertTrue(player.getIsReady());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.placeHandComponentAndReady(gameManager, player, 2, 1, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.placeHandComponentAndReady(gameManager, player, 2, 1, 0, sender);
    }

    @Test
    void readyBuilding() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        // Simulate the action of readying the building
        assertDoesNotThrow(() -> BuildingController.readyBuilding(gameManager, player, sender));

        // Assert that the building is ready
        assertTrue(player.getIsReady());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.readyBuilding(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.readyBuilding(gameManager, player, sender);
    }

    @Test
    void discardComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");

        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        BuildingController.discardComponent(gameManager, player, sender);

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));

        // Discard the hand component
        gameManager.getGame().discardComponent(player);

        // Check that the component has been discarded (null)
        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.discardComponent(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.discardComponent(gameManager, player, sender);
    }

    @Test
    void bookComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        BuildingController.bookComponent(gameManager, player, 0, sender);

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();

        BuildingController.bookComponent(gameManager, player, 4, sender);

        assertDoesNotThrow(() -> BuildingController.bookComponent(gameManager, player, 0, sender));

        // Check if the component was booked correctly
        assertEquals(bookedComponent, buildingBoard.getBookedCopy()[0]);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        BuildingController.bookComponent(gameManager, player, 0, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.bookComponent(gameManager, player, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.bookComponent(gameManager, player, 0, sender);
    }

    @Test
    void pickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();

        BuildingController.pickBookedComponent(gameManager, player, 0, sender);

        buildingBoard.setAsBooked(0);

        BuildingController.pickBookedComponent(gameManager, player, 4, sender);
        BuildingController.pickBookedComponent(gameManager, player, -3, sender);

        // Simulate the action of picking up the booked component
        assertDoesNotThrow(() -> BuildingController.pickBookedComponent(gameManager, player, 0, sender));

        // Assert that the booked component has been placed
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), bookedComponent);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.pickBookedComponent(gameManager, player, 0, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.pickBookedComponent(gameManager, player, 0, sender);
    }

    @Test
    void pickUpEventCardDeck() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }

            public void sendPing() {}
        };

        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);

        //Test unable to pick with hand full
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));

        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);
        buildingBoard.setHandComponent(null);   //reset hand

        //Test idxOutOfBound
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingController.pickUpEventCardDeck(gameManager, player,-1, sender);

        //Test deck already taken
        gameManager = new GameManager(0, 4, 2);
        Player otherPLayer = new Player("Giovanni");
        gameManager.getGame().addPlayer(otherPLayer);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
            }

            public void sendPing() {}
        };
        BuildingController.pickUpEventCardDeck(gameManager, otherPLayer, 1, sender);

        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);


        //Test deck picked
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);
    }

    @Test
    void putDownEventCardDeck() throws RemoteException {

        //Test empty hand deck
        GameManager gameManager = new GameManager(0, 4, 2);
        Player player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NoEventCardDeckTaken", message);
            }

            public void sendPing() {}
        };
        BuildingController.putDownEventCardDeck(gameManager, player, sender);


        //Test full hand
        gameManager = new GameManager(0, 4, 2);
        player = new Player("mario");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {}

            public void sendPing() {}
        };

        BuildingController.pickUpEventCardDeck(gameManager, player, 2, sender);

        BuildingController.putDownEventCardDeck(gameManager, player, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.putDownEventCardDeck(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.putDownEventCardDeck(gameManager, player, sender);
    }

    @Test
    void checkAllNotReadyStartShipValidityAndAddToTravelers() throws RemoteException {
        //setup
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player1 = new Player("mario");
        Player player2 = new Player("marco");
        gameManager.getGame().addPlayer(player1);
        gameManager.getGame().addPlayer(player2);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard bb1 = player1.getSpaceship().getBuildingBoard();
        BuildingBoard bb2 = player2.getSpaceship().getBuildingBoard();


        //init first spaceship (not valid)
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(1, 2, 0);

        //test validity for first ship
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("ValidSpaceShip", message);
            }

            public void sendPing() {}
        };

        BuildingController.checkAllNotReadyStartShipValidityAndAddToTravelers(gameManager);


        //init second spaceship(valid)
        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 1}, "imgPath"));
        bb2.placeComponent(1, 2, 0);


        //test validity for first + second ship
       sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NotValidSpaceShip", message);
            }

           public void sendPing() {}

       };

       BuildingController.checkAllNotReadyStartShipValidityAndAddToTravelers(gameManager);
    }

    @Test
    void buildShip(){
        GameManager gameManager = new GameManager(0, 2, 2);
        Player p1 = new Player("mario");
        Player p2 = new Player("marco");
        gameManager.getGame().addPlayer(p1);
        gameManager.getGame().addPlayer(p2);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        BuildingController.buildShip(gameManager, p1, 1, sender);
        BuildingController.buildShip(gameManager, p2, 2, sender);

        GameManager gameManager2 = new GameManager(0, 2, 1);
        Player p3 = new Player("mario");
        Player p4 = new Player("marco");
        gameManager2.getGame().addPlayer(p3);
        gameManager2.getGame().addPlayer(p4);
        gameManager2.getGame().initPlayersSpaceship();
        gameManager2.getGame().setPhase(GamePhase.BUILDING);
        Sender sender2 = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };

        BuildingController.buildShip(gameManager2, p3, 1, sender2);
        BuildingController.buildShip(gameManager2, p4, 2, sender2);
    }

    @Test
    void showHandComponent(){
        GameManager gameManager = new GameManager(0, 3, 1);
        Player player = new Player("gabriele");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };
        gameManager.addSender(player, sender);

        BuildingBoard buildingBoard = new BuildingBoard(player.getSpaceship(), player.getColor());
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 0, 1, 1}, "imgSrc"));
        BuildingController.showHandComponent(gameManager, player, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.showHandComponent(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.showHandComponent(gameManager, player, sender);

        // null component
        player.getSpaceship().getBuildingBoard().setHandComponent(null);
        BuildingController.showHandComponent(gameManager, player, sender);
    }

    @Test
    void showVisibleComponent(){
        GameManager gameManager = new GameManager(0, 3, 1);
        Player player = new Player("gabriele");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };
        gameManager.addSender(player, sender);

        BuildingBoard buildingBoard = new BuildingBoard(player.getSpaceship(), player.getColor());
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 0, 1, 1}, "imgSrc"));
        BuildingController.showVisibleComponents(gameManager, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.showVisibleComponents(gameManager, sender);
    }

    @Test
    void showBookedComponents(){
        GameManager gameManager = new GameManager(0, 3, 1);
        Player player = new Player("gabriele");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };
        gameManager.addSender(player, sender);

        BuildingBoard buildingBoard = new BuildingBoard(player.getSpaceship(), player.getColor());
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 0, 1, 1}, "imgSrc"));
        buildingBoard.setAsBooked(0);
        BuildingController.showBookedComponents(gameManager, player, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.showBookedComponents(gameManager, player, sender);

        // ready player
        gameManager.getGame().setPhase(GamePhase.BUILDING);
        player.setIsReady(true);
        BuildingController.showBookedComponents(gameManager, player, sender);
    }

    @Test
    void resetTimer(){
        GameManager gameManager = new GameManager(0, 3, 1);
        Player player = new Player("gabriele");
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().initPlayersSpaceship();
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg){

            }

            public void sendPing() {}
        };
        gameManager.addSender(player, sender);

        BuildingController.resetTimer(gameManager, player, sender);

        // wrong phase
        gameManager.getGame().setPhase(GamePhase.POPULATING);
        BuildingController.resetTimer(gameManager, player, sender);
    }
}