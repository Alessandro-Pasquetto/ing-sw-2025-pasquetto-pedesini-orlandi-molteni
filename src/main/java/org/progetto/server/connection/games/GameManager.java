package org.progetto.server.connection.games;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.controller.PopulatingController;
import org.progetto.server.controller.PositioningController;
import org.progetto.server.controller.TimerController;
import org.progetto.server.controller.events.*;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
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

    private final HashMap<Player, Sender> playerSenders = new HashMap<>();

    private final ArrayList<Player> disconnectedPlayers = new ArrayList<>();

    private final ArrayList<Player> losingPlayers = new ArrayList<>();

    // List to save playersOrder after building
    private final ArrayList<Player> notCheckedReadyPlayers = new ArrayList<>();

    GameThread gameThread;
    private final Game game;
    private EventControllerAbstract eventController;
    private final TimerController timer;

    private static int gameDisconnectionDetectionInterval;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameManager(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.eventController = null;
        this.timer = new TimerController(this, 120, 2);
        GameManagerMaps.addWaitingGameManager(idGame, this);

        gameThread = new GameThread(this);
        gameThread.start();

        startGamePinger();
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Sender> getSendersCopy() {
        ArrayList<Sender> socketWritersCopy;

        synchronized (playerSenders) {
            socketWritersCopy = new ArrayList<>(playerSenders.values());
        }

        return socketWritersCopy;
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

    public Sender getSenderByPlayer(Player player) {
        return playerSenders.get(player);
    }

    public Player getPlayerBySender(Sender sender) throws IllegalStateException {

        for (Map.Entry<Player, Sender> entry : playerSenders.entrySet()) {
            if(entry.getValue().equals(sender))
                return entry.getKey();
        }
        throw new IllegalStateException("PlayerNotFound");
    }

    public ArrayList<Player> getDisconnectedPlayersCopy() {
        synchronized (disconnectedPlayers) {
            return new ArrayList<>(disconnectedPlayers);
        }
    }

    public int getSizeDisconnectedPlayers() {
        synchronized (disconnectedPlayers) {
            return disconnectedPlayers.size();
        }
    }

    public Player getDisconnectedPlayerByName(String playerName) {
        synchronized (disconnectedPlayers) {
            for (Player player : disconnectedPlayers) {
                if(player.getName().equals(playerName))
                    return player;
            }
        }
        return null;
    }

    public ArrayList<Player> getLosingPlayersCopy() {
        synchronized (losingPlayers) {
            return new ArrayList<>(losingPlayers);
        }
    }

    // =======================
    // SETTERS
    // =======================

    public void setGameThread(GameThread gameThread) {
        this.gameThread = gameThread;
    }

    public static void setGameDisconnectionDetectionInterval(int gameDisconnectionDetectionInterval) {
        GameManager.gameDisconnectionDetectionInterval = gameDisconnectionDetectionInterval;
    }

    // =======================
    // OTHER METHODS
    // =======================

    private void startGamePinger() {

        Thread pingThread = new Thread(() -> {
            while (true) {
                try {
                    gamePinger();
                    Thread.sleep(gameDisconnectionDetectionInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

    private void gamePinger() {

        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender sender : sendersCopy) {
            if (sender instanceof VirtualClient vc) {
                try{
                    vc.ping();
                } catch (RemoteException e) {
                    disconnectPlayer(getPlayerBySender(vc));
                }
            }
        }
    }

    public void setAndSendPlayersColor(){
        ArrayList<Player> players = game.getPlayersCopy();

        for (int i = 0; i < game.getMaxNumPlayers(); i++) {
            Player player = players.get(i);
            player.setColor(i);

            Sender sender = getSenderByPlayer(player);
            try {
                sender.sendMessage(new PlayerColorMessage(player.getColor()));
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }
    }

    public void addLosingPlayer(Player player) {
        synchronized (losingPlayers) {
            losingPlayers.add(player);
        }
    }

    public void removeLosingPlayer(Player player) {
        synchronized (losingPlayers) {
            losingPlayers.remove(player);
        }
    }

    public boolean isLosingPlayer(Player player) {
        synchronized (losingPlayers) {
            return losingPlayers.contains(player);
        }
    }

    public void disconnectPlayer(Player player) {
        System.out.println(player.getName() + " has disconnected");

        Game game = getGame();

        game.removePlayer(player);
        removeSender(player);

        if(game.getPhase().equals(GamePhase.INIT)){
            game.setPhase(GamePhase.WAITING);
            broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

            gameThread.notifyThread();

            GameManagerMaps.addWaitingGameManager(game.getId(), this);
        }

        if(game.getPhase().equals(GamePhase.WAITING)){
            if(game.getPlayersSize() == 0)
                GameManagerMaps.removeGameManager(game.getId());
            else
                broadcastGameMessage(new WaitingPlayersMessage(game.getPlayersCopy()));

            LobbyController.broadcastLobbyMessage("UpdateGameList");

            return;
        }

        addDisconnectedPlayers(player);
        // todo notificare la disconnessione del player agli altri

        if(game.getPhase().equals(GamePhase.POSITIONING)) {

            if(game.getActivePlayer().equals(player))
                PositioningController.insertAtFurthestStartPosition(this, player);
        }

        if(game.getPhase().equals(GamePhase.EVENT)){

            //todo

        }

        //todo gestire il resto

        gameThread.notifyThread();
    }

    public void reconnectPlayer(String namePlayer, Sender sender) {
        try {

            Player player = getDisconnectedPlayerByName(namePlayer);

            if(player == null)
                throw new IllegalStateException("FailedToReconnect");

            removeDisconnectedPlayer(player);
            addSender(player, sender);

            System.out.println(player.getName() + " has reconnected");

            Game game = getGame();

            Player activePlayer = game.getActivePlayer();
            String nameActivePlayer = "";

            if(activePlayer != null)
                nameActivePlayer = activePlayer.getName();

            sender.sendMessage(new ReconnectionGameData(game.getLevel(), game.getPhase().toString(), player.getColor(), nameActivePlayer));

            if(isLosingPlayer(player)){
                sender.sendMessage("GameOver");
                return;
            }

            // todo: va bene aggiungere subito o all'evento successivo?
            //if(!game.getPhase().equals(GamePhase.EVENT))

            game.addPlayer(player);

            if(game.getPhase().equals(GamePhase.POPULATING))
                PopulatingController.askAliensToSinglePlayer(this, player);

            if(game.getPhase().equals(GamePhase.EVENT)){
                sender.sendMessage(new UpdateTrackMessage(game.getBoard().getCopyTravelers(), game.getBoard().getTrack()));
                sender.sendMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
                sender.sendMessage(new UpdatePlayersMessage(game.getBoard().getCopyTravelers()));


            }

            if(game.getPhase().equals(GamePhase.TRAVEL)){
                sender.sendMessage("AskContinueTravel");
            }


        }catch (RemoteException e) {
            System.err.println("RMI client unreachable");
        }
    }

    public void kickOutDisconnectedPlayer(Player player) {
        removeLosingPlayer(player);
        removeDisconnectedPlayer(player);
        removeSender(player);
    }

    public void addSender(Player player, Sender socketWriter){
        synchronized (playerSenders){
            playerSenders.put(player, socketWriter);
        }
    }

    public void removeSender(Player player){
        synchronized (playerSenders){
            playerSenders.remove(player);
        }
    }

    public void addDisconnectedPlayers(Player player){
        synchronized (disconnectedPlayers){
            disconnectedPlayers.add(player);
        }
    }

    public void removeDisconnectedPlayer(Player player){
        synchronized (disconnectedPlayers){
            disconnectedPlayers.remove(player);
        }
    }

    public void addNotCheckedReadyPlayer(Player player){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.add(player);
        }
    }

    public void removeNotCheckedReadyPlayer(Player player){
        synchronized (notCheckedReadyPlayers){
            notCheckedReadyPlayers.remove(player);
        }
    }

    public synchronized void broadcastGameMessage(Object messageObj) {
        ArrayList<Sender> socketWritersCopy = getSendersCopy();

        for (Sender sender : socketWritersCopy) {
            try{
                sender.sendMessage(messageObj);
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }
    }

    public synchronized void broadcastGameToNotReadyPlayersMessage(Object messageObj) {
        ArrayList<Player> playersCopy = game.getPlayersCopy();

        for (Player p : playersCopy) {
            try{
                getSenderByPlayer(p).sendMessage(messageObj);
            } catch (RemoteException e) {
                System.err.println("RMI client unreachable");
            }
        }
    }

    public synchronized void broadcastGameMessageToOthers(Object messageObj, Sender sender) {
        ArrayList<Sender> socketWritersCopy = getSendersCopy();

        for (Sender s : socketWritersCopy) {
            if (!s.equals(sender)) {
                try {
                    s.sendMessage(messageObj);
                } catch (RemoteException e) {
                    System.err.println("RMI client unreachable");
                }
            }
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

    public void leaveGame(Player player, Sender sender) {
        game.removePlayer(player);
        removeSender(player);

        if(game.getPlayersSize() == 0)
            GameManagerMaps.removeGameManager(game.getId());

        LobbyController.addSender(sender);
    }
}