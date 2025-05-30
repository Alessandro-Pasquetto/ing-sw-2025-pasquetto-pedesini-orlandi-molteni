package org.progetto.server.connection.games;

import org.progetto.messages.toClient.NewGamePhaseMessage;
import org.progetto.messages.toClient.ScoreBoardMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.controller.*;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.CardType;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class GameThread extends Thread {

    // =======================
    // ATTRIBUTES
    // =======================

    GameManager gameManager;
    private boolean isRunning = true;
    private final Object gameThreadLock = new Object();

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameThread(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    // =======================
    // RUN
    // =======================

    /**
     * Manages the game phases
     *
     * @author Alessandro
     */
    @Override
    public void run() {
        try {
            Game game = gameManager.getGame();

            while (isRunning) {
                switch (game.getPhase()) {

                    case WAITING:
                        System.out.println("Waiting for players...");

                        resetAndWaitPlayersReady();

                        game.setPhase(GamePhase.INIT);
                        break;

                    case INIT:
                        System.out.println("Waiting for ready players...");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                        resetAndWaitWaitingPlayersReady();

                        if(game.getPhase().equals(GamePhase.WAITING))
                            continue;

                        gameManager.setAndSendPlayersColor();
                        game.initPlayersSpaceship();
                        game.setPhase(GamePhase.BUILDING);
                        break;

                    case BUILDING:
                        System.out.println("Start building...");
                        GameController.startBuilding(gameManager);
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                        // Updates players list and spaceships
                        gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(game.getPlayersCopy()));

                        game.resetReadyPlayers();
                        synchronized (gameThreadLock) {
                            while (game.getNumReadyPlayers() != game.getPlayersSize() && !gameManager.getTimerExpired())
                                gameThreadLock.wait();
                        }

                        if(gameManager.getTimerExpired()){
                            gameManager.broadcastGameToNotReadyPlayersMessage("TimerExpired");
                            System.out.println("TimerExpired");
                        }
                        gameManager.getTimerController().stopTimer();

                        // Waiting for placing the last component
                        waitPlayersReady();
                        BuildingController.autoReadyBuildingForDisconnectedPlayers(gameManager);

                        if(!BuildingController.checkAllNotReadyStartShipValidityAndAddToTravelers(gameManager)){
                            System.out.println("Adjusting spaceships...");
                            game.setPhase(GamePhase.ADJUSTING);
                            gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                            // Waiting for adjusting spaceship
                            waitPlayersReady();
                        }
                        BuildingController.addDisconnectedPlayersWithIllegalSpaceshipToLosingPlayers(gameManager);

                        if(!BuildingController.initializeAllSpaceship(game)){
                            game.setPhase(GamePhase.POPULATING);
                            gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));
                            System.out.println("Waiting for the players to populate their ships...");

                            PopulatingController.askAliens(gameManager);

                            // Waiting to populate the spaceship
                            waitPlayersReady();
                        }
                        PopulatingController.fillHumansDisconnectedPlayers(gameManager);

                        // Preparing travelers on the track
                        if (game.getLevel() == 1)
                            game.getBoard().addTravelersOnTrack(game.getLevel());
                        else {
                            game.setPhase(GamePhase.POSITIONING);
                            gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));
                            System.out.println("Waiting for the players to decide their starting position...");

                            PositioningController.askForStartingPosition(gameManager);

                            game.getBoard().updateTravelersBasedOnStartingPosition();
                            game.getBoard().addTravelersOnTrack(game.getLevel());
                            // TODO: handle disconnected players
                        }

                        if(game.getLevel() != 1)
                            game.composeHiddenEventDeck();

                        System.out.println("End building phase...");
                        game.setPhase(GamePhase.EVENT);
                        break;

                    case EVENT:
                        System.out.println();
                        System.out.println("New event...");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                        // Updates mini tracks and other spaceships
                        gameManager.broadcastGameMessage(new UpdateOtherTravelersShipMessage(game.getBoard().getCopyTravelers()));
                        gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getPlayersInTrackCopy(gameManager), game.getBoard().getTrack()));


                        // Updates the spaceship
                        for (Player player : game.getBoard().getCopyTravelers()) {
                            Sender sender = gameManager.getSenderByPlayer(player);

                            MessageSenderService.sendOptional(new UpdateSpaceshipMessage(player.getSpaceship(), player), sender);
                        }

                        EventController.pickEventCard(gameManager);

                        System.out.println(game.getActiveEventCard().getType().toString());

                        String eventType = game.getActiveEventCard().getType().toString();
                        int travelersCount = game.getBoard().getNumTravelers();

                        // Checks if remains only a traveler and picked event is battlezone
                        if (eventType.equals(CardType.BATTLEZONE.toString()) && travelersCount == 1) {
                            gameManager.broadcastGameMessage("This event card had been skipped");

                            game.setPhase(GamePhase.TRAVEL);
                            break;
                        }

                        gameManager.createEventController();
                        gameManager.getEventController().start();

                        // After event
                        game.setActiveEventCard(null);
                        gameManager.broadcastGameMessage("This event card is finished");

                        // Handles defeated players
                        EventController.handleDefeatedPlayers(gameManager);

                        // Sleep for a while to let players read the results of the event
                        Thread.sleep(3000);

                        game.setPhase(GamePhase.TRAVEL);
                        break;

                    case TRAVEL:
                        System.out.println("Travel phase started...");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                        gameManager.addReconnectingPlayersToTravelers();

                        // Updates the track
                        gameManager.broadcastGameMessage(new UpdateTrackMessage(GameController.getPlayersInTrackCopy(gameManager), game.getBoard().getTrack()));

                        if(game.getEventDeckSize() > 0){

                            // Asks for each traveler if he wants to continue travel
                            for (Player player : game.getBoard().getCopyTravelers()) {
                                Sender sender = gameManager.getSenderByPlayer(player);

                                MessageSenderService.sendOptional("AskContinueTravel", sender);
                            }

                            resetAndWaitTravelersReady();

                            game.getBoard().updateTurnOrder();

                            // Checks if there is at least a traveler remaining
                            if (game.getBoard().getNumTravelers() > 0) {
                                game.setPhase(GamePhase.EVENT);
                                break;
                            }
                        }

                        game.setPhase(GamePhase.ENDGAME);
                        break;

                    case ENDGAME:
                        System.out.println("Game over");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(game.getPhase().toString()));

                        ArrayList<Player> allPlayers = game.getPlayersCopy();
                        allPlayers.addAll(gameManager.getDisconnectedPlayersCopy());

                        gameManager.broadcastGameMessage(new ScoreBoardMessage(game.scoreBoard(allPlayers)));

                        for (Player player : game.scoreBoard(allPlayers)) {
                            Sender sender = gameManager.getSenderByPlayer(player);

                            if (player.getCredits() > 0)
                                MessageSenderService.sendOptional("YouWon", sender);
                            else
                                MessageSenderService.sendOptional("YouLost", sender);
                        }
                        return;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Pauses the game thread until max num players are ready to continue
     *
     * @author Alessandro
     */
    public void resetAndWaitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        game.resetReadyPlayers();

        synchronized (gameThreadLock) {
            while (game.getPlayersSize() < game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until max num players are ready to continue in waiting phase
     *
     * @author Alessandro
     */
    public void resetAndWaitWaitingPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        game.resetReadyPlayers();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() < game.getMaxNumPlayers()){
                if(game.getPhase() == GamePhase.WAITING)
                    return;

                gameThreadLock.wait();
            }
        }
    }

    /**
     * Pauses the game thread until all connected players are ready to continue
     *
     * @author Alessandro
     */
    public void waitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() < game.getPlayersSize())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until the player is ready or disconnected
     *
     * @author Alessandro
     */
    public void resetAndWaitTravelerReady(Player player) throws InterruptedException {
        player.setIsReady(false, gameManager.getGame());

        synchronized (gameThreadLock) {
            while (!player.getIsReady() && !gameManager.getDisconnectedPlayersCopy().contains(player))
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until all connected travelers are ready to continue
     *
     * @author Gabriele
     */
    public void resetAndWaitTravelersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        Board board = game.getBoard();

        for (Player player : board.getCopyTravelers()) {
            player.setIsReady(false, game);
        }

        synchronized (gameThreadLock) {
            while (!board.allTravelersReady())
                gameThreadLock.wait();
        }
    }

    /**
     * Notifies the game thread
     */
    public void notifyThread(){
        synchronized (gameThreadLock){
            gameThreadLock.notify();
        }
    }

    /**
     * Stops the game thread
     */
    public void stopThread() {
        isRunning = false;
    }
}