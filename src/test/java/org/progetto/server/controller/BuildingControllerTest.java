package org.progetto.server.controller;

import org.junit.jupiter.api.Test;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class BuildingControllerTest {

    @Test
    void pickHiddenComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Game game = gameManager.getGame();

        Player player = new Player("mario", 0, 1);
        game.addPlayer(player);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {

            }
        };

        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());

        assertDoesNotThrow(() -> BuildingController.pickHiddenComponent(gameManager, player, sender));

        assertNotNull(player.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void pickVisibleComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        gameManager.getGame().addPlayer(player);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
            }
        };

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();
        gameManager.getGame().discardComponent(player);

        // Call pickVisibleComponent with the component index and ensure no exceptions are thrown
        assertDoesNotThrow(() -> BuildingController.pickVisibleComponent(gameManager, player, 0, sender));

        // Check if the player’s hand now contains the picked visible component
        assertTrue(player.getSpaceship().getBuildingBoard().getHandComponent().equals(component));
    }

    @Test
    void placeComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        final String[] lastMessage = new String[1];

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {
                lastMessage[0] = msg.toString();
            }
        };

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        assertDoesNotThrow(() -> BuildingController.placeComponent(gameManager, player, 2, 1, 0, sender));

        assertEquals("AllowedToPlaceComponent", lastMessage[0]);

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());
    }

    @Test
    void placeLastComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                // Mock implementation
            }
        };

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        // Place the last component
        assertDoesNotThrow(() -> BuildingController.placeLastComponent(gameManager, player, 0, 0, 0, sender));

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        // Check that the component was placed and the player's hand component is now null
        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void placeHandComponentAndPickHiddenComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);
        Player player = new Player("mario", 0, 1);
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
        gameManager.getGame().addPlayer(player);

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {

            }
        };

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, 2, 1, 0, sender));

        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component2 = buildingBoard.getHandComponent();

        assertNotNull(component2);
        assertNotSame(component, component2);
    }

    @Test
    void placeHandComponentAndPickVisibleComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);

        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a visible component in the game for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component visibleComponent = buildingBoard.getHandComponent();
        gameManager.getGame().discardComponent(player);

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        // Place the hand component and pick a visible component
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, 2, 1, 0, 0, sender));

        // Check that the component was placed and the player picked up the visible component
        assertTrue(player.getSpaceship().getBuildingBoard().getHandComponent().equals(visibleComponent));
    }

    @Test
    void placeHandComponentAndPickUpEventCardDeck() throws RemoteException {
    }

    @Test
    void placeHandComponentAndPickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();
        buildingBoard.setAsBooked(0);

        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{1, 1, 2, 1}, "imgPath"));

        // Simulate the action of placing the hand component and picking up the booked component
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, 2, 1, 0, 0, sender));

        // Assert that the booked component has been placed
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), bookedComponent);
    }

    @Test
    void placeHandComponentAndReady() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component component = buildingBoard.getHandComponent();

        // Simulate the action of placing the hand component and marking it as ready
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndReady(gameManager, player,2,1, 0, sender));

        // Placed component
        assertEquals(2, component.getX());
        assertEquals(1, component.getY());
        assertEquals(0, component.getRotation());

        // Assert that the player is ready
        assertTrue(player.getIsReady());
    }

    @Test
    void readyBuilding() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Simulate the action of readying the building
        assertDoesNotThrow(() -> BuildingController.readyBuilding(gameManager, player, sender));

        // Assert that the building is ready
        assertTrue(player.getIsReady());
    }

    @Test
    void discardComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);

        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a hand component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component handComponent = buildingBoard.getHandComponent();

        // Discard the hand component
        gameManager.getGame().discardComponent(player);

        // Check that the component has been discarded (null)
        assertNull(player.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void bookComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();

        assertDoesNotThrow(() -> BuildingController.bookComponent(gameManager, player, 0, sender));

        // Check if the component was booked correctly
        assertEquals(bookedComponent, buildingBoard.getBooked()[0]);
    }

    @Test
    void pickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };

        // Set up a booked component for the player
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        Component bookedComponent = buildingBoard.getHandComponent();
        buildingBoard.setAsBooked(0);

        // Simulate the action of picking up the booked component
        assertDoesNotThrow(() -> BuildingController.pickBookedComponent(gameManager, player, 0, sender));

        // Assert that the booked component has been placed
        assertEquals(player.getSpaceship().getBuildingBoard().getHandComponent(), bookedComponent);
    }

    @Test
    void pickUpEventCardDeck() throws RemoteException {
    }

    @Test
    void putDownEventCardDeck() throws RemoteException {
    }

    @Test
    void checkAllShipValidity() throws RemoteException {
    }
}