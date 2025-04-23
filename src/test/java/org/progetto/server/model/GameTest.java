package org.progetto.server.model;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.Epidemic;
import org.progetto.server.model.events.EventCard;
import org.progetto.server.model.events.Sabotage;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void getId() {
        Game game1 = new Game(1, 4, 2);
        assertEquals(1, game1.getId());

        Game game2 = new Game(2, 4, 2);
        assertEquals(2, game2.getId());
    }

    @Test
    void getLevel() {
        Game game1 = new Game(1, 4, 1);
        assertEquals(1, game1.getLevel());

        Game game2 = new Game(2, 4, 2);
        assertEquals(2, game2.getLevel());
    }

    @Test
    void getPhase() {
        Game game = new Game(1, 4, 2);
        assertEquals(GamePhase.INIT, game.getPhase());
    }

    @Test
    void getPlayerByName(){
        Game game = new Game(1, 4, 2);
        game.addPlayer(new Player("mario",1,2));
        Player alice = new Player("alice",1,2);
        game.addPlayer(alice);
        game.addPlayer(new Player("bob",1,2));

        assertEquals(alice, game.getPlayerByName("alice"));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> game.getPlayerByName("matteo"));
        assertEquals("PlayerNameNotFound", exception.getMessage());
    }

    @Test
    void getPlayersCopy() {
        Game game = new Game(1, 4, 2);

        // Test initial empty player list
        assertTrue(game.getPlayersCopy().isEmpty());

        // Add a player and test
        Player player = new Player("gino", 0, 2);
        game.addPlayer(player);

        ArrayList<Player> players = game.getPlayersCopy();
        assertEquals(1, players.size());
        assertEquals("gino", players.get(0).getName());

        // Verify that getPlayers returns a new ArrayList (defensive copy)
        ArrayList<Player> players1 = game.getPlayersCopy();
        ArrayList<Player> players2 = game.getPlayersCopy();
        assertNotSame(players1, players2);
    }

    @Test
    void getVisibleComponentDeckCopy() {
        Game game = new Game(1, 4, 2);

        Player player = new Player("gino", 0, 2);
        game.addPlayer(player);

        ArrayList<Component> visibleComponent = new ArrayList<>();

        assertEquals(visibleComponent, game.getVisibleComponentDeckCopy());

        Spaceship spaceship = player.getSpaceship();
        BuildingBoard buildingBoard = spaceship.getBuildingBoard();

        // Cannon
        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // HousingUnit
        buildingBoard.setHandComponent(new HousingUnit(ComponentType.HOUSING_UNIT, new int[]{0, 3, 3, 3}, "imgPath", 2));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // Engine
        buildingBoard.setHandComponent(new Component(ComponentType.ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // DoubleEngine
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_ENGINE, new int[]{3, 3, 0, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // Shields (x2)
        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{0, 0, 3, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // BatteryStorage
        buildingBoard.setHandComponent(new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{3, 3, 3, 3}, "imgPath", 2));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        // DoubleCannon
        buildingBoard.setHandComponent(new Component(ComponentType.DOUBLE_CANNON, new int[]{0, 3, 3, 3}, "imgPath"));
        visibleComponent.add(buildingBoard.getHandComponent());
        game.discardComponent(player);

        assertEquals(visibleComponent, game.getVisibleComponentDeckCopy());

    }

    @Test
    void getPlayersCopySize() {
        Game game = new Game(1, 4, 2);

        // Test initial size
        assertEquals(0, game.getPlayersSize());

        // Add players and test size
        game.addPlayer(new Player("gino", 0, 2));
        assertEquals(1, game.getPlayersSize());

        game.addPlayer(new Player("andrea", 1, 2));
        assertEquals(2, game.getPlayersSize());
    }

    @Test
    void getMaxNumPlayers() {
        Game game1 = new Game(1, 4, 2);
        assertEquals(4, game1.getMaxNumPlayers());

        Game game2 = new Game(2, 2, 2);
        assertEquals(2, game2.getMaxNumPlayers());

        Game game3 = new Game(3, 3, 2);
        assertEquals(3, game3.getMaxNumPlayers());
    }

    @Test
    void getBoard() {
        // Test for level 1
        Game game1 = new Game(1, 4, 1);
        assertNotNull(game1.getBoard());
        assertEquals(18, game1.getBoard().getTrack().length);

        // Test for level 2
        Game game2 = new Game(1, 4, 2);
        assertNotNull(game2.getBoard());
        assertEquals(24, game2.getBoard().getTrack().length);
    }

    @Test
    void getActiveEventCard(){
        Game game = new Game(0, 3,2);

        game.setActiveEventCard(new Epidemic(CardType.EPIDEMIC, 1, "img"));
        assertEquals(CardType.EPIDEMIC, game.getActiveEventCard().getType());

        game.setActiveEventCard(new Sabotage(CardType.SABOTAGE, 2, "img"));
        assertEquals(CardType.SABOTAGE, game.getActiveEventCard().getType());
        assertNotEquals(CardType.EPIDEMIC, game.getActiveEventCard().getType());
    }

    @Test
    void getActivePlayer(){
        Game game = new Game(0,3,2);
        Player player = new Player("tom", 1, 1);

        game.setActivePlayer(player);
    }

    @Test
    void getNumReadyPlayers(){
        Game game = new Game(0,3,2);

        game.addReadyPlayers(true);
        game.addReadyPlayers(true);
        game.addReadyPlayers(true);
        game.addReadyPlayers(false);

        assertEquals(2, game.getNumReadyPlayers());
    }

    @Test
    void setPhase() {
        Game game = new Game(1, 4, 2);

        // Test initial phase
        assertEquals(GamePhase.INIT, game.getPhase());

        // Test setting to BUILDING
        game.setPhase(GamePhase.BUILDING);
        assertEquals(GamePhase.BUILDING, game.getPhase());

        // Test setting to TRAVEL
        game.setPhase(GamePhase.TRAVEL);
        assertEquals(GamePhase.TRAVEL, game.getPhase());

        // Test setting to EVENT
        game.setPhase(GamePhase.EVENT);
        assertEquals(GamePhase.EVENT, game.getPhase());

        // Test setting to END
        game.setPhase(GamePhase.ENDGAME);
        assertEquals(GamePhase.ENDGAME, game.getPhase());
    }

    @Test
    void setActiveEventCard(){
        Game game = new Game(0, 3,2);

        game.setActiveEventCard(new Epidemic(CardType.EPIDEMIC, 1, "img"));
        assertEquals(CardType.EPIDEMIC, game.getActiveEventCard().getType());

        game.setActiveEventCard(new Sabotage(CardType.SABOTAGE, 2, "img"));
        assertEquals(CardType.SABOTAGE, game.getActiveEventCard().getType());
        assertNotEquals(CardType.EPIDEMIC, game.getActiveEventCard().getType());
    }

    @Test
    void setActivePlayer(){
        Game game = new Game(0, 3,2);
        Player player = new Player("tom", 1, 1);

        game.setActivePlayer(player);

        assertEquals(player, game.getActivePlayer());
    }

    @Test
    void loadEvents(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);

        assertNotNull(game.pickUpEventCardDeck(mario,0));
    }

    @Test
    void loadComponents(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);

        assertNotNull(game.pickHiddenComponent(mario));
    }

    @Test
    void addPlayer() {
        Player mario = new Player("mario",1,2);
        Player alice = new Player("alice",1,2);

        Game game = new Game(0, 3,2);
        game.addPlayer(mario);
        game.addPlayer(alice);

        assertTrue(game.getPlayersCopy().contains(mario));
        assertTrue(game.getPlayersCopy().contains(alice));

    }

    @Test
    void pickUpEventCardDeck(){
        Player player = new Player("mario", 1, 2);
        Player player2 = new Player("anna", 3, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(player);
        game.addPlayer(player2);

        assertNotNull(game.pickUpEventCardDeck(player,0));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            game.pickUpEventCardDeck(player, 6);
        });
        assertEquals("IllegalIndexEventCardDeck", exception.getMessage());

        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> {
            game.pickUpEventCardDeck(player, -4);
        });
        assertEquals("IllegalIndexEventCardDeck", exception1.getMessage());

        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            game.pickUpEventCardDeck(player2, 0);
        });
        assertEquals("EventCardDeckIsAlreadyTaken", exception2.getMessage());
    }

    @Test
    void putDownEventCardDeck(){
        Player player = new Player("mario", 1, 2);
        Player player2 = new Player("anna", 3, 2);
        Game game = new Game(0, 3, 2);
        game.addPlayer(player);
        game.addPlayer(player2);

        ArrayList<EventCard> eventDeck = game.pickUpEventCardDeck(player, 0);

        assertNotNull(eventDeck);

        // Success: player has a deck assigned
        int index = game.putDownEventCardDeck(player);
        assertEquals(0, index);

        // Deck not taken
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            game.putDownEventCardDeck(player2);
        });
        assertEquals("NoEventCardDeckTaken", exception.getMessage());

        //TODO: sistemarlo
    }

    @Test
    void composeHiddenEventDeck(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);
        game.addPlayer(mario);

        game.composeHiddenEventDeck();

        assertNotNull(game.pickEventCard());
    }


    @Test
    void pickHiddenComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);
        Component component = game.pickHiddenComponent(mario);
        assertEquals(component, mario.getSpaceship().getBuildingBoard().getHandComponent());

        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> game.pickHiddenComponent(mario));
        assertEquals("FullHandComponent", exception1.getMessage());

        game.discardComponent(mario);

        for (int i = 0; i < 151; i++) {
            game.pickHiddenComponent(mario);
            game.discardComponent(mario);
        }

        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> game.pickHiddenComponent(mario));
        assertEquals("EmptyComponentDeck", exception2.getMessage());

    }

    @Test
    void pickVisibleComponent() {
        Player mario = new Player("mario", 1, 2);
        Player teo = new Player("teo", 1, 2);
        Game game = new Game(0,3,2);

        game.addPlayer(mario);
        game.addPlayer(teo);

        Component nextDiscardedComponent = game.pickHiddenComponent(mario);
        String imgSrcDiscardedComponent = game.discardComponent(mario);

        game.pickVisibleComponent(0, mario);

        Component component_picked = mario.getSpaceship().getBuildingBoard().getHandComponent();

        assertEquals(component_picked, nextDiscardedComponent);

        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> game.pickVisibleComponent(0, mario));
        assertEquals("FullHandComponent", exception1.getMessage());

        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> game.pickVisibleComponent(0, teo));
        assertEquals("IllegalIndexComponent", exception2.getMessage());
    }

    @Test
    void discardComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0,3,2);

        game.addPlayer(mario);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> game.discardComponent(mario));
        assertEquals("EmptyHandComponent", exception.getMessage());

        Component pickedComponent = game.pickHiddenComponent(mario);
        String imgSrcDiscardedComponent = game.discardComponent(mario);

        assertNull(mario.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void pickEventCard() {

        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0,3,2);

        game.addPlayer(mario);

        assertNotNull(game.pickEventCard());

        assertEquals(2, game.getEventDeckSize());
    }

    @Test
    void checkAvailableName() {
        Game game = new Game(0,3,2);
        game.addPlayer(new Player("alice",1,2));
        game.addPlayer(new Player("matteo",1,2));

        assertFalse(game.checkAvailableName("alice"));
        assertFalse(game.checkAvailableName("matteo"));
        assertTrue(game.checkAvailableName("gianfranco"));
        assertTrue(game.checkAvailableName("francesco"));
    }

    @Test
    void addReadyPlayers(){
        Game game = new Game(0,3,2);

        game.addReadyPlayers(true);
        game.addReadyPlayers(true);
        game.addReadyPlayers(true);
        game.addReadyPlayers(false);

        assertEquals(2, game.getNumReadyPlayers());
    }

    @Test
    void resetReadyPlayers(){
        Game game = new Game(0,3,2);

        Player p1 = new Player("alice",1,2);
        Player p2 = new Player("matteo",1,2);
        Player p3 = new Player("gianfranco",1,2);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);

        p1.setIsReady(true, game);
        p2.setIsReady(true, game);
        p3.setIsReady(true, game);

        game.resetReadyPlayers();

        assertEquals(0, game.getNumReadyPlayers());
    }

    @Test
    void scoreBoard() {
        Game game = new Game(0,3,2);

        Player p1 = new Player("alice",0,2);
        Player p2 = new Player("matteo",1,2);
        Player p3 = new Player("gianfranco",2,2);

        game.addPlayer(p1);
        game.addPlayer(p2);
        game.addPlayer(p3);

        // Sets for each player his position
        p1.setPosition(20);
        p2.setPosition(15);
        p3.setPosition(10);

        // Sets for each player his exposed connectors
        p1.getSpaceship().setExposedConnectorsCount(3);
        p2.getSpaceship().setExposedConnectorsCount(2);
        p3.getSpaceship().setExposedConnectorsCount(1);

        // p2 left travel
        p2.setHasLeft(true);

        // Adds for each player his boxes
        p1.getSpaceship().addBoxCount(1, Box.RED);
        p1.getSpaceship().addBoxCount(1, Box.YELLOW);
        p2.getSpaceship().addBoxCount(1, Box.YELLOW);
        p3.getSpaceship().addBoxCount(1, Box.BLUE);

        // Adds for each player his destroyed components count
        p1.getSpaceship().addDestroyedCount(2);
        p2.getSpaceship().addDestroyedCount(1);
        p3.getSpaceship().addDestroyedCount(3);

        ArrayList<Player> result = game.scoreBoard();

        assertNotNull(result);

        assertEquals(p1, result.get(0));
        assertEquals(p2, result.get(2));
        assertEquals(p3, result.get(1));

        assertEquals(9, p1.getCredits());
        assertEquals(1, p2.getCredits());
        assertEquals(3, p3.getCredits());
    }
}