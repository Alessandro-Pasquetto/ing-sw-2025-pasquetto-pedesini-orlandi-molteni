package org.progetto.server.model;

import com.google.gson.reflect.TypeToken;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.progetto.server.model.loadClasses.ComponentDeserializer;
import org.progetto.server.model.loadClasses.EventDeserializer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;


public class Game {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int id;
    private final int maxNumPlayers;
    private final ArrayList<Player> players;
    private final int level;
    private GamePhase phase;

    private ArrayList<Component> componentDeck;
    private ArrayList<Component> visibleComponentDeck;

    private ArrayList<EventCard> hiddenEventDeck;
    private final ArrayList<EventCard>[] visibleEventCardDecks;    //array of 3 visible event decks: [left, centre, right]
    private  boolean[] eventDeckAvailable;                           //direct relation to visibleEventCardDecks, true means that deck is on a player hand

    private final Board board;
    private EventCard activeEventCard;



    // =======================
    // CONSTRUCTORS
    // =======================

    // todo: set visibleEventCardDecks, defaultTimer, timerFlipsAllowed, imgPath
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
        this.eventDeckAvailable = new boolean[] {true,true,true};
        this.board = new Board(level);
        this.activeEventCard = null;
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


    // =======================
    // SETTERS
    // =======================

    public void setPhase(GamePhase phase) {
        synchronized (this) {
            this.phase = phase;
        }
    }

    private synchronized void setActiveEventCard(EventCard eventCard) {
        this.activeEventCard = eventCard;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // todo: saveGame()
    public void saveGame(){}

    /**
     * Returns the list of players who have more than 0 credits (winners)
     *
     * @return list of the winner players
     */
    public ArrayList<Player> endGame() {

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
     * @param player the player who is picking
     * @return the randomly picked component
     */
    public Component pickHiddenComponent(Player player) throws IllegalStateException{
        Component pickedComponent = null;

        if(player.getSpaceship().getBuildingBoard().getHandComponent() != null)
            throw new IllegalStateException("HandComponent already set");

        synchronized (componentDeck) {
            if(componentDeck.isEmpty()) throw new IllegalStateException("Empty componentDeck");
            int randomPos = (int) (Math.random() * componentDeck.size());
            pickedComponent = componentDeck.remove(randomPos);
        }

        player.getSpaceship().getBuildingBoard().setHandComponent(pickedComponent);

        return pickedComponent;
    }

    /**
     * Takes a component from the discarded/visible ones and assigns it to hand Component
     *
     * @param indexComponent is the index of the visible component picked
     * @param player is the player who is picking
     */
    public void pickVisibleComponent(int indexComponent, Player player) throws IllegalStateException{
        Component pickedComponent = null;

        synchronized (visibleComponentDeck) {
            if(indexComponent >= visibleComponentDeck.size()) throw new IllegalStateException("Wrong indexComponent");
            pickedComponent = visibleComponentDeck.remove(indexComponent);
        }

        player.getSpaceship().getBuildingBoard().setHandComponent(pickedComponent);
    }

    /**
     * Draws a random event card
     * @return the randomly picked card
     */
    public EventCard pickEventCard() throws IllegalStateException {
        EventCard pickedEventCard = null;
        synchronized (hiddenEventDeck) {
            if(hiddenEventDeck.isEmpty()) throw new IllegalStateException("Empty visibleEventCardDecks");
            int randomPos = (int) (Math.random() * hiddenEventDeck.size());
            pickedEventCard = hiddenEventDeck.remove(randomPos);
        }

        setActiveEventCard(pickedEventCard);

        return pickedEventCard;
    }

    /**
     * Loading event cards from json file and initialize visibleEventCardDecks
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
     * Loading all components saved on json file in to the componentDeck
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
     * Check if the name of the player who wants to join is available
     *
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
     * pick-up a visible event deck
     * @author Lorenzo
     * @param idx of the deck that the player wants to pick-up
     * @return the deck picked if available
     */
    public ArrayList<EventCard> pickUpEventCardDeck(int idx){

        if(eventDeckAvailable[idx]){
            eventDeckAvailable[idx] = false;
            return visibleEventCardDecks[idx];
        }
        else
            return null;
    }

    /**
     * put-down the visible event-deck
     * @author Lorenzo
     * @param idx
     * @return true if the event-deck can be return to the board
     */
    public boolean putDownEventDeck(int idx){
        if(!eventDeckAvailable[idx]) {
            eventDeckAvailable[idx] = true;
            return true;
        }
        else
            return false;
    }

    /**
     * Compose the hidden deck after the building phase
     * @author Lorenzo
     * @return the hiddenDeck composed if all the visible decks where available
     */
    public ArrayList<EventCard> composeHiddenEventDeck() {

        ArrayList<EventCard> Deck = new ArrayList<>();

        Deck.addAll(hiddenEventDeck);

        for(int i = 0; i < 3; i++) {
            if(eventDeckAvailable[i])
                Deck.addAll(visibleEventCardDecks[i]);

            else
                return null;
        }

        Collections.shuffle(Deck);
        while(Deck.getFirst().getLevel() != level)
            Collections.shuffle(Deck);

        return Deck;
    }
}