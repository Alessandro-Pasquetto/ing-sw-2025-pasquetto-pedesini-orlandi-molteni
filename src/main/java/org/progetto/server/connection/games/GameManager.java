package org.progetto.server.connection.games;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.TimerController;
import org.progetto.server.controller.events.EpidemicController;
import org.progetto.server.controller.events.OpenSpaceController;
import org.progetto.server.controller.events.StardustController;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.EventCard;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * All game communication data to handle multiple clients (Socket/RMI)
 */
public class GameManager {

    // =======================
    // ATTRIBUTES
    // =======================

    private final HashMap<Player, SocketWriter> playerSocketWriters = new HashMap<>();
    private final HashMap<Player, VirtualClient> playerRmiClients = new HashMap<>();

    private final Game game;
    private EventController eventController;
    private final TimerController timer;

    private int diceResult;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameManager(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.eventController = null;
        this.timer = new TimerController(this, 10, 2);
        GameManagerMaps.addWaitingGameManager(idGame, this);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<SocketWriter> getSocketWritersCopy() {
        ArrayList<SocketWriter> socketWritersCopy;

        synchronized (playerSocketWriters) {
            socketWritersCopy = new ArrayList<>(playerSocketWriters.values());
        }

        return socketWritersCopy;
    }

    public ArrayList<VirtualClient> getRmiClientsCopy() {
        ArrayList<VirtualClient> rmiClientsCopy;

        synchronized (playerRmiClients) {
            rmiClientsCopy = new ArrayList<>(playerRmiClients.values());
        }

        return rmiClientsCopy;
    }

    public Game getGame() {
        return game;
    }

    public EventController getEventController() {
        return eventController;
    }

    public TimerController getTimerController() {
        return timer;
    }

    public boolean timerExpired() {
        return timer.isTimerExpired();
    }

    public SocketWriter getSocketWriterByPlayer(Player player) {
        return playerSocketWriters.get(player);
    }

    public VirtualClient getVirtualClientByPlayer(Player player) {
        return playerRmiClients.get(player);
    }

    public Player getPlayerByVirtualClient(VirtualClient virtualClient) throws IllegalStateException {

        for (Map.Entry<Player, VirtualClient> entry : playerRmiClients.entrySet()) {
            if(entry.getValue().equals(virtualClient))
                return entry.getKey();
        }
        throw new IllegalStateException("PlayerNotFound");
    }

    public int getDiceResult() {
        return diceResult;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void addSocketWriter(Player player, SocketWriter socketWriter){
        synchronized (playerSocketWriters){
            playerSocketWriters.put(player, socketWriter);
        }
    }

    public void removeSocketWriter(Player player){
        synchronized (playerSocketWriters){
            playerSocketWriters.remove(player);
        }
    }

    public void addRmiClient(Player player, VirtualClient rmiClient){
        synchronized (playerRmiClients){
            playerRmiClients.put(player, rmiClient);
        }
    }

    public void removeRmiClient(Player player){
        synchronized (playerRmiClients){
            playerRmiClients.remove(player);
        }
    }

    public void broadcastGameMessage(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            sw.sendMessage(messageObj);
        }

        ArrayList<VirtualClient> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualClient vc : rmiClientsCopy) {
                vc.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastGameMessageToOthers(Object messageObj, Sender sender) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            if(!sw.equals(sender))
                sw.sendMessage(messageObj);
        }

        ArrayList<VirtualClient> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualClient vc : rmiClientsCopy) {
                if(!vc.equals(sender))
                    vc.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void createEventController() {
        EventCard eventCard = game.getActiveEventCard();

        switch (eventCard.getType()) {

            case EPIDEMIC:
                eventController = new EpidemicController(this);
                break;

            case STARDUST:
                eventController = new StardustController(this);
                break;

            case OPENSPACE:
                eventController = new OpenSpaceController(this);
                break;

            default:
                eventController = null;
                break;
        }
    }

    public void startTimer() {
        timer.startTimer();
    }

    public void setDiceResult(int diceResult) {
        this.diceResult = diceResult;
    }
}