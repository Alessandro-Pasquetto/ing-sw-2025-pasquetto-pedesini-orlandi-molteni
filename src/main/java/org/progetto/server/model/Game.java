package org.progetto.server.model;
import org.progetto.server.model.components.*;
import java.util.ArrayList;

// todo: check sync
public class Game {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int id;
    private final int level;
    private GamePhase phase;
    private final ArrayList<Player> players;
    private final Component[] componentDeck;
    private final ArrayList<EventCard> eventCardDeck;
    private final Board board;

    // =======================
    // CONSTRUCTORS
    // =======================

    // todo: set eventCardDeck, defaultTimer, timerFlipsAllowed, imgPath
    public Game(int idGame, int level) {
        this.id = idGame;
        this.level = level;
        this.phase = GamePhase.INIT;
        this.players = new ArrayList<>();
        this.componentDeck = ComponentConfig.loadComponents();
        this.eventCardDeck = new ArrayList<>();
        this.board = new Board(elaborateSizeBoardFromLv(level), 60,3,"imgPath");
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
            return players;
        }
    }

    public int getPlayersSize() {
        synchronized (players) {
            return players.size();
        }
    }

    public ArrayList<EventCard> getEventCardDeck() {
        return eventCardDeck;
    }

    public Component[] getComponentDeck() {
        return componentDeck;
    }

    public Board getBoard() {
        return board;
    }

    // =======================
    // SETTERS
    // =======================

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public void addPlayer(Player player) {
        synchronized (players) {
            players.add(player);
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void startGame() {}

    private void shuffleComponents() {}

    private void shuffleEventCards() {}

    public void startBuilding(){}

    public void saveGame(){}

    public EventCard pickEventCard(){return null;}

    public void loadEvents(){}

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

    private int elaborateSizeBoardFromLv(int level) {
        return switch (level) {
            case 1 -> 18;
            case 2 -> 24;
            case 3 -> 34;
            default -> 0;
        };
    }
}