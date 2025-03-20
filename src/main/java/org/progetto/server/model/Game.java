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
import java.util.Collection;
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
    private final ArrayList<EventCard> eventCardDeck;
    private final Board board;
    private EventCard activeEventCard;

    // =======================
    // CONSTRUCTORS
    // =======================

    // todo: set eventCardDeck, defaultTimer, timerFlipsAllowed, imgPath
    public Game(int idGame, int maxNumPlayers, int level) {
        this.id = idGame;
        this.maxNumPlayers = maxNumPlayers;
        this.players = new ArrayList<Player>();
        this.level = level;
        this.phase = GamePhase.INIT;
        this.componentDeck = loadComponents();
        this.visibleComponentDeck = new ArrayList<>();
        this.eventCardDeck = loadEvents();
        this.board = new Board(elaborateSizeBoardFromLv(), "imgPath");
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
     * @param player the new player joining the game
     */
    public void addPlayer(Player player) {
        synchronized (players) {
            players.add(player);
        }
    }

    /**
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
     * @return the randomly picked card
     */
    public EventCard pickEventCard() throws IllegalStateException {
        EventCard pickedEventCard = null;
        synchronized (eventCardDeck) {
            if(eventCardDeck.isEmpty()) throw new IllegalStateException("Empty eventCardDeck");
            int randomPos = (int) (Math.random() * eventCardDeck.size());
            pickedEventCard = eventCardDeck.remove(randomPos);
        }

        setActiveEventCard(pickedEventCard);

        return pickedEventCard;
    }

    /**
     * Loading event cards from json file and initialize eventCardDeck
     * @author Lorenzo
     * @return event card deck (list of event cards)
     */
    private ArrayList<EventCard> loadEvents(){


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

                return demoDeck;

            }

            if(level == 2) {
                ArrayList<EventCard> lv1Deck;
                ArrayList<EventCard> lv2Deck;
                ArrayList<EventCard> Deck = new ArrayList<>();

                FileReader reader = new FileReader("src/main/resources/org.progetto.server/EventCards1.json");
                lv1Deck = gson.fromJson(reader, listType);
                reader.close();

                reader = new FileReader("src/main/resources/org.progetto.server/EventCards2.json");
                lv2Deck = gson.fromJson(reader, listType);
                reader.close();

                Collections.shuffle(lv1Deck);
                Collections.shuffle(lv2Deck);

                Deck.addAll(lv1Deck.subList(0,4));
                Deck.addAll(lv2Deck.subList(0,8));

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
     * @param name is the name of the player who wants to join the game
     * @return true if it is available, false otherwise
     */
    public boolean tryAddPlayer(String name) {
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
     * @return the board size
     */
    private int elaborateSizeBoardFromLv() {
        return switch (level) {
            case 1 -> 18;
            case 2 -> 24;
            case 3 -> 34;
            default -> 0;
        };
    }
}