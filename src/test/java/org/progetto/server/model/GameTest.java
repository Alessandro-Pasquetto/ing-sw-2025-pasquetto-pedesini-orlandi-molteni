package org.progetto.server.model;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.Epidemic;
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
    }

    @Test
    void getPlayers() {
        Game game = new Game(1, 4, 2);

        // Test initial empty player list
        assertTrue(game.getPlayers().isEmpty());

        // Add a player and test
        Player player = new Player("gino", 0, 2);
        game.addPlayer(player);

        ArrayList<Player> players = game.getPlayers();
        assertEquals(1, players.size());
        assertEquals("gino", players.get(0).getName());

        // Verify that getPlayers returns a new ArrayList (defensive copy)
        ArrayList<Player> players1 = game.getPlayers();
        ArrayList<Player> players2 = game.getPlayers();
        assertNotSame(players1, players2);
    }

    @Test
    void getPlayersSize() {
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
    void saveGame() {
    }

    @Test
    void endGame() {
        Player mario = new Player("mario",1,2);
        Player alice = new Player("alice",1,2);
        Player bob = new Player("bob",1,2);

        Game game = new Game(0, 3,2);
        game.addPlayer(mario);
        game.addPlayer(alice);
        game.addPlayer(bob);

        mario.addCredits(2);
        alice.addCredits(2);

        ArrayList<Player> winners = game.endGame();

        assertTrue(winners.contains(mario));
        assertTrue(winners.contains(alice));
        assertFalse(winners.contains(bob));
    }

    @Test
    void addPlayer() {
        Player mario = new Player("mario",1,2);
        Player alice = new Player("alice",1,2);

        Game game = new Game(0, 3,2);
        game.addPlayer(mario);
        game.addPlayer(alice);

        assertTrue(game.getPlayers().contains(mario));
        assertTrue(game.getPlayers().contains(alice));

    }

    @Test
    void pickUpEventCardDeck(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);

        assertNotNull(game.pickUpEventCardDeck(mario,0));
    }

    @Test
    void putDownEventCardDeck(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);

        game.pickUpEventCardDeck(mario,0);
        assertTrue(game.putDownEventCardDeck(mario,0));
        assertFalse(game.putDownEventCardDeck(mario,1));
    }

    @Test
    void composeHiddenEventDeck(){
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);

        assertNotNull(game.pickEventCard());
    }


    @Test
    void pickHiddenComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0, 3, 2);

        game.addPlayer(mario);
        Component component = game.pickHiddenComponent(mario);
        assertEquals(component, mario.getSpaceship().getBuildingBoard().getHandComponent());
    }

    @Test
    void pickVisibleComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0,3,2);

        game.addPlayer(mario);

        Component nextDiscardedComponent = game.pickHiddenComponent(mario);
        String imgSrcDiscardedComponent = game.discardComponent(mario);

        game.pickVisibleComponent(0, mario);

        Component component_picked = mario.getSpaceship().getBuildingBoard().getHandComponent();

        assertEquals(component_picked, nextDiscardedComponent);
    }

    @Test
    void discardComponent() {
        Player mario = new Player("mario", 1, 2);
        Game game = new Game(0,3,2);

        game.addPlayer(mario);

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
}