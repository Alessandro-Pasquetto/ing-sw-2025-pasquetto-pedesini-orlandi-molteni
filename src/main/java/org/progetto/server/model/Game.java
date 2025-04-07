package org.progetto.server.model;

import com.google.gson.reflect.TypeToken;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.progetto.server.model.loading.ComponentDeserializer;
import org.progetto.server.model.loading.EventDeserializer;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
    private final AtomicInteger numReadyPlayers;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Game(int idGame, int maxNumPlayers, int level) {
        this.id = idGame;
        this.maxNumPlayers = maxNumPlayers;
        this.players = new ArrayList<Player>();
        this.level = level;
        this.phase = GamePhase.INIT;
        this.componentDeck = loadComponents();
        this.visibleComponentDeck = new ArrayList<>();
        this.hiddenEventDeck = new ArrayList<>();
        this.visibleEventCardDecks = loadEvents();
        this.eventDeckAvailable = new Player[] {null,null,null};
        this.board = new Board(level);
        this.activeEventCard = null;
        this.activePlayer = null;
        this.numReadyPlayers = new AtomicInteger(0);
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

    public ArrayList<Player> getPlayers() {
        synchronized (players) {
            return new ArrayList<>(players);
        }
    }

    public int getPlayersSize() {
        synchronized (players) {
            return players.size();
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
        return numReadyPlayers.get();
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

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Loading event cards from json file and initialize visibleEventCardDecks
     *
     * @author Lorenzo
     * @return event card deck (list of event cards)
     */
    private ArrayList<EventCard>[] loadEvents(){

        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(EventCard.class, new EventDeserializer());
            Gson gson = gsonBuilder.create();

            Type listType = new TypeToken<ArrayList<EventCard>>() {}.getType();

            if(level == 1) {
                ArrayList<EventCard> demoDeck;
                FileReader reader = new FileReader("src/main/resources/org.progetto.server/EventCardsL.json");
                demoDeck = gson.fromJson(reader, listType);
                reader.close();

                Collections.shuffle(demoDeck);

                hiddenEventDeck = demoDeck;

                return null;
            }

            if(level == 2) {
                ArrayList<EventCard> lv1Deck;
                ArrayList<EventCard> lv2Deck;

                ArrayList<EventCard>[] Deck = (ArrayList<EventCard>[]) new ArrayList[3];
                for (int i = 0; i < 3; i++) {
                    Deck[i] = new ArrayList<>();
                }

                FileReader reader = new FileReader("src/main/resources/org.progetto.server/EventCards1.json");
                lv1Deck = gson.fromJson(reader, listType);
                reader.close();

                reader = new FileReader("src/main/resources/org.progetto.server/EventCards2.json");
                lv2Deck = gson.fromJson(reader, listType);
                reader.close();

                Collections.shuffle(lv1Deck);
                Collections.shuffle(lv2Deck);

                hiddenEventDeck.add(lv1Deck.getFirst());
                hiddenEventDeck.addAll(lv2Deck.subList(0,2));


                for(int i = 1; i<4; i++) {
                    Deck[i-1].add(lv1Deck.get(i));
                    Deck[i-1].addAll(lv2Deck.subList(i*i+1, i*i+3));
                }

                return Deck;
            }

        } catch (IOException e) {
            e.printStackTrace();

            return null;
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

            FileReader reader = new FileReader("src/main/resources/org.progetto.server/Components.json");
            ArrayList<Component> components = gson.fromJson(reader, listType);
            reader.close();

            return components;

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Returns the list of players who have more than 0 credits (winners)
     *
     * @author Alessandro
     * @return list of the winner players
     */
    public ArrayList<Player> winnerPlayers() {

        ArrayList<Player> winners = new ArrayList<>();

        for (Player player : players) {
            if (player.getCredits() > 0) {
                winners.add(player);
            }
        }

        return winners;
    }

    /**
     * Adds a player to the list of players in the game
     *
     * @author Alessandro
     * @param player the new player joining the game
     */
    public void addPlayer(Player player) {
        synchronized (players) {
            players.add(player);
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
            int randomPos = (int) (Math.random() * componentDeck.size());
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

        discardedComponent.setHidden(false);
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
            if(hiddenEventDeck.isEmpty())
                throw new IllegalStateException("EmptyHiddenEventCardDeck");
            int randomPos = (int) (Math.random() * hiddenEventDeck.size());
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

        if (idx < 0 || idx >= eventDeckAvailable.length) {
            throw new IllegalStateException("IllegalIndexEventCardDeck");
        }

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
     * @return the hiddenDeck composed if all the visible decks where available
     */
    public ArrayList<EventCard> composeHiddenEventDeck() {
        ArrayList<EventCard> Deck = new ArrayList<>(hiddenEventDeck);

        for(int i = 0; i < 3; i++) {
            if(eventDeckAvailable[i] == null)
                Deck.addAll(visibleEventCardDecks[i]);
            else
                return null;
        }

        do Collections.shuffle(Deck);
        while (Deck.getFirst().getLevel() != level);

        return Deck;
    }

    /**
     * Changes numReadyPlayer value
     *
     * @author Alessandro
     * @param isToAdd Defines increment/decrement
     */
    public void addReadyPlayers(boolean isToAdd) {
        if(isToAdd)
            numReadyPlayers.getAndIncrement();
        else
            numReadyPlayers.getAndDecrement();
    }
}