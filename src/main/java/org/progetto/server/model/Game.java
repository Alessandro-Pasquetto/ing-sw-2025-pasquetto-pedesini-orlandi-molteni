package org.progetto.server.model;

import com.google.gson.reflect.TypeToken;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.progetto.server.model.loading.ComponentDeserializer;
import org.progetto.server.model.loading.EventDeserializer;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class Game {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int id;
    private final int maxNumPlayers;
    private final ArrayList<Player> players;
    private final int level;
    private GamePhase phase;

    private final ArrayList<Component> componentDeck;
    private final ArrayList<Component> visibleComponentDeck;

    private ArrayList<EventCard> hiddenEventDeck;
    private final ArrayList<EventCard>[] visibleEventCardDecks;    // array of 3 visible event decks: [left, centre, right]
    private final Player[] eventDeckAvailable;                     // direct relation to visibleEventCardDecks, if a player is present than he's using it

    private final Board board;
    private EventCard activeEventCard;
    private Player activePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Game(int idGame, int maxNumPlayers, int level) {
        this.id = idGame;
        this.maxNumPlayers = maxNumPlayers;
        this.players = new ArrayList<>();
        this.level = level;
        this.phase = GamePhase.WAITING;
        this.componentDeck = loadComponents();
        this.visibleComponentDeck = new ArrayList<>();
        this.hiddenEventDeck = new ArrayList<>();
        this.visibleEventCardDecks = loadEvents();
        this.eventDeckAvailable = new Player[] {null, null, null};
        this.board = new Board(level, maxNumPlayers);
        this.activeEventCard = null;
        this.activePlayer = null;
    }

    // =======================
    // GETTERS
    // =======================

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Player getPlayerByName(String name) throws IllegalStateException {
        synchronized (players) {
            for (Player player : players) {
                if (player.getName().equals(name)) {
                    return player;
                }
            }
        }
        throw new IllegalStateException("PlayerNameNotFound");
    }

    public ArrayList<Player> getPlayersCopy() {
        synchronized (players) {
            return new ArrayList<>(players);
        }
    }

    public ArrayList<Component> getVisibleComponentDeckCopy() {
        synchronized (visibleComponentDeck) {
            return new ArrayList<>(visibleComponentDeck);
        }
    }

    public int getPlayersSize() {
        synchronized (players) {
            return players.size();
        }
    }

    public int getEventDeckSize() {
        synchronized (hiddenEventDeck) {
            return hiddenEventDeck.size();
        }
    }

    public int getMaxNumPlayers() {
        return maxNumPlayers;
    }

    public Board getBoard() {
        return board;
    }

    public EventCard getActiveEventCard() {
        return activeEventCard;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public int getNumReadyPlayers() {
        int readyPlayers = 0;

        synchronized (players) {
            for (Player player : players) {
                if(player.getIsReady())
                    readyPlayers++;
            }
        }

        return readyPlayers;
    }

    // =======================
    // SETTERS
    // =======================

    public void setPhase(GamePhase phase) {
        synchronized (this) {
            this.phase = phase;
        }
    }

    public void setActiveEventCard(EventCard eventCard) {
        synchronized (this) {
            this.activeEventCard = eventCard;
        }
    }

    public void setActivePlayer(Player player) {
        synchronized (this) {
            this.activePlayer = player;
        }
    }

    public synchronized Player[] getEventDeckAvailableCopy() {
        synchronized (eventDeckAvailable) {
            Player[] eventDeckAvailableCopy = new Player[eventDeckAvailable.length];

            for (int i = 0; i < eventDeckAvailable.length; i++) {
                eventDeckAvailableCopy[i] = eventDeckAvailable[i];
            }

            return eventDeckAvailableCopy;
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void initPlayersSpaceship(){
        ArrayList<Player> players = getPlayersCopy();

        for (Player player : players) {
            player.initSpaceship(level);
        }
    }

    /**
     * Loading event cards from json file and initialize visibleEventCardDecks
     *
     * @author Lorenzo
     * @return visible event card decks (list of event cards)
     */
    private ArrayList<EventCard>[] loadEvents(){
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(EventCard.class, new EventDeserializer());
            Gson gson = gsonBuilder.create();

            Type listType = new TypeToken<ArrayList<EventCard>>() {}.getType();

            if(level == 1) {
                ArrayList<EventCard> demoDeck;
                FileReader reader = new FileReader("src/main/resources/org/progetto/server/EventCardsL.json");
                demoDeck = gson.fromJson(reader, listType);
                reader.close();

                Collections.shuffle(demoDeck);

                hiddenEventDeck = demoDeck;

                return null;
            }

            if(level == 2) {
                ArrayList<EventCard> lv1Deck;
                ArrayList<EventCard> lv2Deck;

                ArrayList<EventCard>[] decks = (ArrayList<EventCard>[]) new ArrayList[3];
                for (int i = 0; i < 3; i++) {
                    decks[i] = new ArrayList<>();
                }

                FileReader reader = new FileReader("src/main/resources/org/progetto/server/EventCards1.json");
                lv1Deck = gson.fromJson(reader, listType);
                reader.close();

                reader = new FileReader("src/main/resources/org/progetto/server/EventCards2.json");
                lv2Deck = gson.fromJson(reader, listType);
                reader.close();

                Collections.shuffle(lv1Deck);
                Collections.shuffle(lv2Deck);

                // forzare uscita carta evento, todo da rimuovere
                while(!lv2Deck.getFirst().getType().equals(CardType.PIRATES) && !lv2Deck.get(1).getType().equals(CardType.PIRATES))
                   Collections.shuffle(lv2Deck);

                hiddenEventDeck.add(lv1Deck.getFirst());
                hiddenEventDeck.addAll(lv2Deck.subList(0, 2));

                for(int i = 1; i < 4; i++) {
                    decks[i - 1].add(lv1Deck.get(i));   //Add 1 lv1 card
                    decks[i - 1].addAll(lv2Deck.subList(i * 2, i * 2 + 2)); //Adds 2 lv2 cards
                }

                return decks;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Loads all components saved in json file in to the componentDeck
     *
     * @author Lorenzo
     * @return component deck (list of components)
     */
    private ArrayList<Component> loadComponents(){
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Component.class, new ComponentDeserializer());
            Gson gson = gsonBuilder.create();

            Type listType = new TypeToken<ArrayList<Component>>() {}.getType();

            FileReader reader = new FileReader("src/main/resources/org/progetto/server/Components.json");
            ArrayList<Component> components = gson.fromJson(reader, listType);
            reader.close();

            return components;

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Elaborates score board calculating final credits for each player
     *
     * @author Gabriele
     * @param arrivalOrderPlayers
     * @return list of players sorted in order of credit number
     */
    public ArrayList<Player> scoreBoard(ArrayList<Player> arrivalOrderPlayers) {
        arrivalOrderPlayers.sort((p1, p2) -> Integer.compare(p2.getPosition(), p1.getPosition()));

        int minExposedConnectorsCount = Integer.MAX_VALUE;

        // Finds min amount of exposed connector among player's ships
        for (Player player : arrivalOrderPlayers) {
            if (player.getSpaceship().getExposedConnectorsCount() < minExposedConnectorsCount) {
                minExposedConnectorsCount = player.getSpaceship().getExposedConnectorsCount();
            }
        }

        int rewardForPosition = 4;

        // Loop to update player's final score
        for (Player player : arrivalOrderPlayers) {

            boolean hasLeft = player.getHasLeft();

            // Reward for arrival order
            if (!hasLeft) {
                player.addCredits(rewardForPosition);
                rewardForPosition--;
            }

            // Reward for most beautiful ship
            if (!hasLeft && player.getSpaceship().getExposedConnectorsCount() == minExposedConnectorsCount) {
                player.addCredits(2);
            }

            // Reward for boxes
            int boxValueDivider = hasLeft ? 2 : 1;
            int boxesValue = player.getSpaceship().getBoxesValue();
            player.addCredits((int) Math.ceil((double) boxesValue / boxValueDivider));

            // Looses a credit for each destroyed component
            int destroyedComponents = player.getSpaceship().getDestroyedCount();
            player.addCredits(-destroyedComponents);
        }

        ArrayList<Player> creditsOrderPlayers = new ArrayList<>(arrivalOrderPlayers);
        creditsOrderPlayers.sort((p1, p2) -> Integer.compare(p2.getCredits(), p1.getCredits()));
        return creditsOrderPlayers;
    }

    /**
     * Adds a player to the list of players in the game
     *
     * @author Alessandro
     * @param player the new player joining the game
     */
    public void addPlayer(Player player) throws IllegalStateException {
        synchronized (players) {
            if(players.size() == maxNumPlayers)
                throw new IllegalStateException("GameFull");

            players.add(player);

            if(players.size() == maxNumPlayers){
                GameManagerMaps.removeWaitingGameManager(id);
                LobbyController.broadcastLobbyMessage("UpdateGameList");
            }
        }
    }

    public void removePlayer(Player player) throws IllegalStateException {
        synchronized (players) {
            players.remove(player);
        }
    }

    /**
     * Randomly draws a component from the covered componentsDeck and assigns it to handComponent
     *
     * @author Alessandro
     * @param player the player who is picking
     * @return the randomly picked component
     */
    public Component pickHiddenComponent(Player player) throws IllegalStateException{
        Component pickedComponent = null;

        if(player.getSpaceship().getBuildingBoard().getHandComponent() != null)
            throw new IllegalStateException("FullHandComponent");

        synchronized (componentDeck) {
            if(componentDeck.isEmpty())
                throw new IllegalStateException("EmptyComponentDeck");

//            int randomPos = (int) (Math.random() * componentDeck.size());
//            pickedComponent = componentDeck.remove(randomPos);

            // forzare componente, todo da rimuovere
            int randomPos = 0;
            ComponentType type;
            do{
                randomPos = (int) (Math.random() * componentDeck.size());

                type = componentDeck.get(randomPos).getType();

            } while (!type.equals(ComponentType.BOX_STORAGE) && !type.equals(ComponentType.RED_BOX_STORAGE));

            pickedComponent = componentDeck.remove(randomPos);
        }

        player.getSpaceship().getBuildingBoard().setHandComponent(pickedComponent);

        return pickedComponent;
    }

    /**
     * Takes the handComponent and adds it to the visibleComponentDeck
     *
     * @author Alessandro
     * @param player is the player who is discarding a component
     * @return the imgSrc of the discarded component
     */
    public String discardComponent(Player player) throws IllegalStateException{

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();
        Component discardedComponent = buildingBoard.getHandComponent();

        if(discardedComponent == null)
            throw new IllegalStateException("EmptyHandComponent");

        if(discardedComponent.getHasBeenBooked())
            throw new IllegalStateException("HasBeenBooked");

        buildingBoard.setHandComponent(null);

        synchronized (visibleComponentDeck) {
            visibleComponentDeck.add(discardedComponent);
        }
        return discardedComponent.getImgSrc();
    }

    /**
     * Takes a component from the discarded/visible ones and assigns it to handComponent
     *
     * @author Alessandro
     * @param idxVisibleComponent Is the index of the visible component picked
     * @param player Is the player who is picking
     */
    public void pickVisibleComponent(int idxVisibleComponent, Player player) throws IllegalStateException{
        Component pickedComponent = null;

        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        if(buildingBoard.getHandComponent() != null)
            throw new IllegalStateException("FullHandComponent");

        synchronized (visibleComponentDeck) {
            if(idxVisibleComponent >= visibleComponentDeck.size())
                throw new IllegalStateException("IllegalIndexComponent");
            pickedComponent = visibleComponentDeck.remove(idxVisibleComponent);
        }

        buildingBoard.setHandComponent(pickedComponent);
    }

    /**
     * Draws a random event card and set as active
     *
     * @author Alessandro
     * @return the randomly picked card
     */
    public EventCard pickEventCard() throws IllegalStateException {
        EventCard pickedEventCard = null;

        synchronized (hiddenEventDeck) {

            if (hiddenEventDeck.isEmpty())
                throw new IllegalStateException("EmptyHiddenEventCardDeck");

//            int randomPos = (int) (Math.random() * hiddenEventDeck.size());
//            pickedEventCard = hiddenEventDeck.remove(randomPos);

            //forzare eventCard, todo da rimuovere
            int randomPos = 0;

            do {
                randomPos = (int) (Math.random() * hiddenEventDeck.size());
            } while (!hiddenEventDeck.get(randomPos).getType().equals(CardType.PLANETS));

            pickedEventCard = hiddenEventDeck.remove(randomPos);
       }

        setActiveEventCard(pickedEventCard);
        return pickedEventCard;
    }

    /**
     * Check if the name of the player who wants to join is available
     *
     * @author Alessandro
     * @param name is the name of the player who wants to join the game
     * @return true if it is available, false otherwise
     */
    public boolean checkAvailableName(String name) {
        synchronized (players) {
            for (Player p : players) {
                if (p.getName().equals(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Pick-up a visible event deck
     *
     * @author Lorenzo
     * @param idx Index of the deck that the player wants to pick-up
     * @param player Is the player that wants to pick up the deck
     * @return the deck picked if available
     */
    public ArrayList<EventCard> pickUpEventCardDeck(Player player, int idx) throws IllegalStateException{

        if (idx < 0 || idx >= eventDeckAvailable.length)
            throw new IllegalStateException("IllegalIndexEventCardDeck");

        synchronized (eventDeckAvailable) {
            if (eventDeckAvailable[idx] == null && !Arrays.asList(eventDeckAvailable).contains(player)) {
                eventDeckAvailable[idx] = player;
                return visibleEventCardDecks[idx];
            } else
                throw new IllegalStateException("EventCardDeckIsAlreadyTaken");
        }
    }

    /**
     * Put-down the visible event-deck
     *
     * @author Gabriele
     * @param player is the player that wants to put-down the deck
     * @return the idx of the deck put down
     */
    public int putDownEventCardDeck(Player player) throws IllegalStateException {
        synchronized (eventDeckAvailable) {
            for (int i = 0; i < eventDeckAvailable.length; i++) {
                if (eventDeckAvailable[i] != null && eventDeckAvailable[i].equals(player)) {
                    eventDeckAvailable[i] = null;
                    return i;
                }
            }
        }

        throw new IllegalStateException("NoEventCardDeckTaken");
    }

    /**
     * Composes the hidden deck after the building phase
     *
     * @author Lorenzo
     */
    public void composeHiddenEventDeck() {
        ArrayList<EventCard> deck = new ArrayList<>(hiddenEventDeck);

        for(int i = 0; i < 3; i++) {
            if(eventDeckAvailable[i] == null)
                deck.addAll(visibleEventCardDecks[i]);
            else
                return;
        }

        do Collections.shuffle(deck);
        while (deck.getFirst().getLevel() != level);

        hiddenEventDeck = deck;
    }

    /**
     * Set all players not ready
     *
     * @author Alessandro
     */
    public void resetReadyPlayers(){
        synchronized (players){
            for (Player p : players) {
                p.setIsReady(false, this);
            }
        }
    }
}