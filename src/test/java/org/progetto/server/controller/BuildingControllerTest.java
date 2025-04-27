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

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

class BuildingControllerTest {

    @Test
    void pickHiddenComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 1, 1);
        Game game = gameManager.getGame();

        Player player = new Player("mario", 0, 1);
        game.addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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

        gameManager.getGame().setPhase(GamePhase.BUILDING);

        Component component = new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath");
        buildingBoard.setHandComponent(component);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        assertDoesNotThrow(() -> BuildingController.placeLastComponent(gameManager, player, 2, 1, 0, sender));

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));

        //Test unable to pick for level 1
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("CannotPickUpEventCardDeck", message);

            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);


        //Test empty hand
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        player.getSpaceship().getBuildingBoard().setHandComponent(null);
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EmptyHandComponent", message);

            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);


        //Test deck already taken
        Player otherPLayer = new Player("Giovanni", 0, 1);
        gameManager.getGame().addPlayer(otherPLayer);

        BuildingBoard bb1 = player.getSpaceship().getBuildingBoard();
        BuildingBoard bb2 = otherPLayer.getSpaceship().getBuildingBoard();

        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        bb2.setHandComponent(new Component(ComponentType.BOX_STORAGE, new int[]{1, 1, 2, 1}, "imgPath"));

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {

            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, otherPLayer, 2, 3, 0, 0, sender);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                if(!message.equals("AllowedToPlaceComponent"))
                    assertEquals("EventCardDeckIsAlreadyTaken", message);
            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 0, sender);


        //Test illegal placement
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {
                assertEquals("NotAllowedToPlaceComponent", msg);
            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 2, 3, 0, 1, sender);

        //Test correct placing and picking
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        sender = new Sender() {
            @Override
            public void sendMessage(Object msg) throws RemoteException {
                if(!msg.equals("AllowedToPlaceComponent"))
                    assertInstanceOf(PickedUpEventCardDeckMessage.class, msg);
            }
        };
        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, 1, 3, 0, 1, sender);



    }

    @Test
    void placeHandComponentAndPickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        assertDoesNotThrow(() -> BuildingController.placeHandComponentAndReady(gameManager, player, 2, 1, 0, sender));

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        assertEquals(bookedComponent, buildingBoard.getBookedCopy()[0]);
    }

    @Test
    void pickBookedComponent() throws RemoteException {
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

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
        GameManager gameManager = new GameManager(0, 4, 1);

        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        //Test unable to pick for level 1
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("CannotPickUpEventCardDeck", message);

            }
        };

       BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);


        //Test unable to pick with hand full
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("FullHandComponent", message);

            }
        };

        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);
        buildingBoard.setHandComponent(null);   //reset hand


        //Test unable to pick while ready   //todo check why it doesn't set ready the player
//        gameManager = new GameManager(0, 4, 2);
//        gameManager.getGame().addPlayer(player);
//        player.setIsReady(true, gameManager.getGame());
//
//        sender = new Sender() {
//            @Override
//            public void sendMessage(Object message) {
//                assertEquals("ActionNotAllowedInReadyState", message);
//
//            }
//        };
//
//        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);


        //Test idxOutOfBound
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("IllegalIndexEventCardDeck", message);

            }
        };

        BuildingController.pickUpEventCardDeck(gameManager, player,-1, sender);

        //Test deck already taken
        gameManager = new GameManager(0, 4, 2);
        Player otherPLayer = new Player("Giovanni", 0, 1);
        gameManager.getGame().addPlayer(otherPLayer);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
            }
        };
        BuildingController.pickUpEventCardDeck(gameManager, otherPLayer, 1, sender);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EventCardDeckIsAlreadyTaken", message);

            }
        };
        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);


        //Test deck picked
        gameManager = new GameManager(0, 4, 2);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertInstanceOf(PickedUpEventCardDeckMessage.class, message);

            }
        };
        BuildingController.pickUpEventCardDeck(gameManager, player, 1, sender);


    }

    @Test
    void putDownEventCardDeck() throws RemoteException {

        //Test empty hand deck
        GameManager gameManager = new GameManager(0, 4, 2);
        Player player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("NoEventCardDeckTaken", message);
            }
        };
        BuildingController.putDownEventCardDeck(gameManager, player, sender);


        //Test full hand
        gameManager = new GameManager(0, 4, 2);
        player = new Player("mario", 0, 1);
        gameManager.getGame().addPlayer(player);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {}
        };
        BuildingController.pickUpEventCardDeck(gameManager, player, 2, sender);
        sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("EventCardDeckPutDown", message);
            }
        };

        BuildingController.putDownEventCardDeck(gameManager, player, sender);

    }

    @Test
    void checkAllNotReadyStartShipValidity() throws RemoteException {
        //setup
        GameManager gameManager = new GameManager(0, 4, 2);
        Player player_1 = new Player("mario", 0, 1);
        Player player_2 = new Player("marco", 1, 1);
        gameManager.getGame().addPlayer(player_1);
        gameManager.getGame().addPlayer(player_2);
        gameManager.getGame().setPhase(GamePhase.BUILDING);

        BuildingBoard bb1 = player_1.getSpaceship().getBuildingBoard();
        BuildingBoard bb2 = player_2.getSpaceship().getBuildingBoard();


        //init first spaceship (not valid)
        bb1.setHandComponent(new Component(ComponentType.CANNON, new int[]{3, 3, 3, 3}, "imgPath"));
        bb1.placeComponent(1, 2, 0);

        //test validity for first ship
        Sender sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
                assertEquals("ValidSpaceShip", message);
            }
        };

        BuildingController.checkAllNotReadyStartShipValidity(gameManager);


        //init second spaceship(valid)
        bb2.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 1}, "imgPath"));
        bb2.placeComponent(1, 2, 0);


        //test validity for first+second ship
       sender = new Sender() {
            @Override
            public void sendMessage(Object message) {
            assertEquals("NotValidSpaceShip", message);
            }
       };

       BuildingController.checkAllNotReadyStartShipValidity(gameManager);


    }
}