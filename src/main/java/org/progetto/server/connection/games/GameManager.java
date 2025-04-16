package org.progetto.server.connection.games;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.controller.TimerController;
import org.progetto.server.controller.events.*;
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

    private final ArrayList<Player> notCheckedReadyPlayers = new ArrayList<>();

    GameThread gameThread;
    private final Game game;
    private EventControllerAbstract eventController;
    private final TimerController timer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameManager(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.eventController = null;
        this.timer = new TimerController(this, 5, 2);
        GameManagerMaps.addWaitingGameManager(idGame, this);

        gameThread = new GameThread(this);
        gameThread.start();
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

    public ArrayList<Player> getCheckedNotReadyPlayersCopy() {
        ArrayList<Player> checkedNotReadyPlayersCopy;

        synchronized (notCheckedReadyPlayers) {
            checkedNotReadyPlayersCopy = new ArrayList<>(notCheckedReadyPlayers);
        }

        return checkedNotReadyPlayersCopy;
    }

    public Game getGame() {
        return game;
    }

    public EventControllerAbstract getEventController() {
        return eventController;
    }

    public GameThread getGameThread() {
        return gameThread;
    }

    public TimerController getTimerController() {
        return timer;
    }

    public boolean getTimerExpired() {
        return timer.getIsTimerExpired();
    }

    public SocketWriter getSocketWriterByPlayer(Player player) {
        return playerSocketWriters.get(player);
    }

    public VirtualClient getVirtualClientByPlayer(Player player) {
        return playerRmiClients.get(player);
    }

    public Sender getSenderByPlayer(Player player) {
        SocketWriter socketWriter = this.getSocketWriterByPlayer(player);
        VirtualClient virtualClient = this.getVirtualClientByPlayer(player);

        Sender sender = null;

        if (socketWriter != null) {
            sender = socketWriter;
        } else if (virtualClient != null) {
            sender = virtualClient;
        }

        return sender;
    }

    public Player getPlayerByVirtualClient(VirtualClient virtualClient) throws IllegalStateException {

        for (Map.Entry<Player, VirtualClient> entry : playerRmiClients.entrySet()) {
            if(entry.getValue().equals(virtualClient))
                return entry.getKey();
        }
        throw new IllegalStateException("PlayerNotFound");
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

    public void addNotCheckedReadyPlayer(Player player){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.add(player);
        }
    }

    public void addAllNotCheckReadyPlayers(){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.addAll(game.getPlayersCopy());
        }
    }

    public void removeNotCheckedReadyPlayer(Player player){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.remove(player);
        }
    }

    public void removeAllNotCheckReadyPlayers(){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.clear();
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

    public void broadcastGameToNotReadyPlayersMessage(Object messageObj) {
        ArrayList<Player> playersCopy = game.getPlayersCopy();

        for (Player p : playersCopy) {
            try{
                getSenderByPlayer(p).sendMessage(messageObj);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
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

            case PLANETS:
                eventController = new PlanetsController(this);
                break;

            case BATTLEZONE:
                eventController = new BattlezoneController(this);
                break;

            case LOSTSHIP:
                eventController = new LostShipController(this);
                break;

            case LOSTSTATION:
                eventController = new LostStationController(this);
                break;

            case METEORSRAIN:
                eventController = new MeteorsRainController(this);
                break;

            case PIRATES:
                eventController = new PiratesController(this);
                break;

            case SMUGGLERS:
                eventController = new SmugglersController(this);
                break;

            case SLAVERS:
                eventController = new SlaversController(this);
                break;

            case SABOTAGE:
                eventController = new SabotageController(this);
                break;

            default:
                eventController = null;
                break;
        }
    }

    public void startTimer() {
        timer.startTimer();
    }
}