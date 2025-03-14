package org.progetto.server.model;
import org.progetto.server.model.components.*;
import java.util.ArrayList;
import java.util.Random;

// todo: check sync
public class Game {

    // =======================
    // ATTRIBUTES
    // =======================
    private final Random random;

    private final int id;
    private final int numPlayers;
    private final ArrayList<Player> players;
    private final int level;
    private GamePhase phase;
    private final ArrayList<Component> componentDeck;
    private final ArrayList<EventCard> eventCardDeck;
    private final Board board;

    // =======================
    // CONSTRUCTORS
    // =======================

    // todo: set eventCardDeck, defaultTimer, timerFlipsAllowed, imgPath
    public Game(int idGame, int numPlayers, int level) {
        this.random = new Random();
        this.id = idGame;
        this.numPlayers = numPlayers;
        this.players = new ArrayList<Player>();
        this.level = level;
        this.phase = GamePhase.INIT;
        this.componentDeck = loadComponents();
        this.eventCardDeck = loadEvents();
        this.board = new Board(elaborateSizeBoardFromLv(level), "imgPath");
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

    public ArrayList<Component> getComponentDeck() {
        return componentDeck;
    }

    public int getComponentDeckSize() {
        synchronized (componentDeck) {
            return componentDeck.size();
        }
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

    public Component pickComponent(Player player){
        Component pickedComponent = null;
        synchronized (componentDeck) {
            int randomPos = random.nextInt(componentDeck.size());
            pickedComponent = componentDeck.remove(randomPos);
        }

        player.getSpaceship().getBuildingBoard().setHandComponent(pickedComponent);

        return pickedComponent;
    }

    public ArrayList<EventCard> loadEvents(){return null;}

    public ArrayList<Component> loadComponents(){
        ArrayList<Component> components = new ArrayList<>();
        components.add(new Component(ComponentType.CANNON, new int[]{0,1,2,3}, "imgPath"));
        components.add(new Component(ComponentType.DOUBLE_CANNON, new int[]{0,1,2,3}, "imgPath"));
        components.add(new Component(ComponentType.ENGINE, new int[]{0,1,2,3}, "imgPath"));
        components.add(new Component(ComponentType.BATTERY_STORAGE, new int[]{0,1,2,3}, "imgPath"));
        components.add(new Component(ComponentType.ORANGE_HOUSING_UNIT, new int[]{0,1,2,3}, "imgPath"));
        components.add(new Component(ComponentType.SHIELD, new int[]{0,1,2,3}, "imgPath"));

        return components;
    }

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