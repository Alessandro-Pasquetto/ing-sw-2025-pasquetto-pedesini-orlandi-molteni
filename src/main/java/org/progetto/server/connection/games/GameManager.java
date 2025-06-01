package org.progetto.server.connection.games;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.controller.*;
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

    private final HashMap<Player, Sender> playerSenders;

    private final ArrayList<Player> disconnectedPlayers;

    private final ArrayList<Player> losingPlayers;

    // List to save playersOrder after building
    private final ArrayList<Player> notCheckedReadyPlayers;

    GameThread gameThread;
    private final Game game;
    private EventControllerAbstract eventController;
    private final TimerController timer;

    private static int gameDisconnectionDetectionInterval;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameManager(int idGame, int numPlayers, int level) {

        this.playerSenders = new HashMap<>();
        this.disconnectedPlayers = new ArrayList<>();
        this.losingPlayers = new ArrayList<>();
        this.notCheckedReadyPlayers = new ArrayList<>();

        this.game = new Game(idGame, numPlayers, level);
        this.eventController = null;
        this.timer = new TimerController(this, 90, 2);
        GameManagerMaps.addWaitingGameManager(idGame, this);

        gameThread = new GameThread(this);
        gameThread.start();

        startGamePinger();
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Sender> getSendersCopy() {
        ArrayList<Sender> sendersCopy;

        synchronized (playerSenders) {
            sendersCopy = new ArrayList<>(playerSenders.values());
        }

        return sendersCopy;
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
        pingThread.setName("GamePingerThread");
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

            MessageSenderService.sendOptional(new PlayerColorMessage(player.getColor()), sender);
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

    public synchronized void disconnectPlayer(Player player) {

        Game game = getGame();

        game.removePlayer(player);
        removeSender(player);

        if(game.getPlayersSize() == 0){
            GameManagerMaps.removeGameManager(game.getId());
            LobbyController.broadcastLobbyMessage("UpdateGameList");
            return;
        }

        broadcastGameMessage(new AnotherPlayerDisconnectMessage(player.getName()));

        if(game.getPhase().equals(GamePhase.WAITING)){
            broadcastGameMessage(new WaitingPlayersMessage(game.getPlayersCopy()));

            LobbyController.broadcastLobbyMessage("UpdateGameList");
            return;
        }

        if(game.getPhase().equals(GamePhase.INIT)){
            game.setPhase(GamePhase.WAITING);
            broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));
            broadcastGameMessage(new WaitingPlayersMessage(game.getPlayersCopy()));

            gameThread.notifyThread();

            GameManagerMaps.addWaitingGameManager(game.getId(), this);

            LobbyController.broadcastLobbyMessage("UpdateGameList");

            return;
        }

        addDisconnectedPlayers(player);

        if(game.getPhase().equals(GamePhase.EVENT)){
            /*
            game.getBoard().removeTraveler(player);

            broadcastGameMessage(new UpdateOtherTravelersShipMessage(game.getBoard().getCopyTravelers()));
             */
            broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
        }

        if(game.getPhase().equals(GamePhase.TRAVEL)){

            game.getBoard().removeTraveler(player);

        }

        player.setIsReady(false, game);
        gameThread.notifyThread();
    }

    public synchronized void reconnectPlayer(String namePlayer, Sender sender) {
        Player player = getDisconnectedPlayerByName(namePlayer);

        if(player == null)
            throw new IllegalStateException("FailedToReconnect");

        player.setIsReady(false, game);

        removeDisconnectedPlayer(player);
        addSender(player, sender);
        game.addPlayer(player);

        broadcastGameMessageToOthers(new AnotherPlayerReconnectMessage(player.getName()), sender);

        Game game = getGame();

        Player activePlayer = game.getActivePlayer();
        String nameActivePlayer = "";

        if(activePlayer != null)
            nameActivePlayer = activePlayer.getName();

        MessageSenderService.sendOptional(new ReconnectionGameData(game.getLevel(), game.getPhase().toString(), player.getColor(), nameActivePlayer), sender);

        if(isLosingPlayer(player)){
            MessageSenderService.sendOptional("GameOver", sender);
            return;
        }

        if(game.getPhase().equals(GamePhase.POPULATING))
            PopulatingController.askAliensToSinglePlayer(this, player); // No need to set the player as not ready, the initialization method already did

        else if (game.getPhase().equals(GamePhase.POSITIONING)) {
            PositioningController.showPlayersInPositioningDecisionOrder(this, sender);
            PositioningController.showStartingPositions(this, sender);
        }

        else if(game.getPhase().equals(GamePhase.EVENT)){
            MessageSenderService.sendOptional(new UpdateSpaceshipMessage(player.getSpaceship(), player), sender);
            MessageSenderService.sendOptional(new UpdateOtherTravelersShipMessage(game.getBoard().getCopyTravelers()), sender);
            MessageSenderService.sendOptional(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(this), game.getBoard().getTrack()), sender);
            MessageSenderService.sendOptional(new PickedEventCardMessage(game.getActiveEventCard()), sender);

            if(!player.getHasLeft())
                eventController.reconnectPlayer(player, sender);
        }

        else if(game.getPhase().equals(GamePhase.TRAVEL)){
            MessageSenderService.sendOptional(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(this), game.getBoard().getTrack()), sender);

            if(!player.getHasLeft()){
                game.getBoard().addTraveler(player);
                MessageSenderService.sendOptional("AskContinueTravel", sender);
            }
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
        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender sender : sendersCopy) {
            MessageSenderService.sendOptional(messageObj, sender);
        }
    }

    public synchronized void broadcastGameToNotReadyPlayersMessage(Object messageObj) {
        ArrayList<Player> playersCopy = game.getPlayersCopy();

        for (Player p : playersCopy) {
            MessageSenderService.sendOptional(messageObj, getSenderByPlayer(p));
        }
    }

    public synchronized void broadcastGameMessageToOthers(Object messageObj, Sender sender) {
        ArrayList<Sender> sendersCopy = getSendersCopy();

        for (Sender s : sendersCopy) {
            if (!s.equals(sender)) {
                MessageSenderService.sendOptional(messageObj, s);
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

    public void losePlayer(Player player) {
        game.getBoard().leaveTravel(player);
        addLosingPlayer(player);

        broadcastGameMessage(new UpdateTrackMessage(GameController.getAllPlayersInTrackCopy(this),game.getBoard().getTrack()));
    }
}