package org.progetto.server.connection.games;

import org.progetto.messages.toClient.NewGamePhaseMessage;
import org.progetto.messages.toClient.ScoreBoardMessage;
import org.progetto.server.connection.Sender;
import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.PopulateController;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.GamePhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.events.CardType;

import java.rmi.RemoteException;

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
                switch (gameManager.getGame().getPhase()) {

                    case INIT:
                        System.out.println("Waiting for players...");

                        resetAndWaitPlayersReady();

                        gameManager.getGame().setPhase(GamePhase.BUILDING);
                        break;

                    case BUILDING:
                        System.out.println("Start building...");
                        GameController.startBuilding(gameManager);
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));

                        gameManager.getGame().resetReadyPlayers();
                        synchronized (gameThreadLock) {
                            while (game.getNumReadyPlayers() != game.getMaxNumPlayers() && !gameManager.getTimerExpired())
                                gameThreadLock.wait();
                        }

                        if(gameManager.getTimerExpired()){
                            gameManager.broadcastGameToNotReadyPlayersMessage("TimerExpired");
                            System.out.println("TimerExpired");
                        }
                        gameManager.getTimerController().stopTimer();

                        // Waiting for placing the last component
                        waitPlayersReady();

                        while(!BuildingController.checkAllNotReadyStartShipValidity(gameManager)){
                            System.out.println("Adjusting spaceships...");
                            game.setPhase(GamePhase.ADJUSTING);
                            gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));

                            // Waiting for adjusting spaceship
                            waitPlayersReady();
                        }

                        if(!BuildingController.initializeAllSpaceship(game)){
                            game.setPhase(GamePhase.POPULATING);
                            gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));
                            System.out.println("Waiting for the players to populate their ships...");

                            PopulateController.askAliens(gameManager);

                            // Waiting to populate the spaceship (don't do another phase for this, bcs custom actions after this)
                            waitPlayersReady();
                        }

                        // Preparing travelers on the track
                        game.getBoard().addTravelersInTrack(game.getLevel());

                        if(game.getLevel() != 1)
                            game.composeHiddenEventDeck();

                        System.out.println("End building phase...");
                        game.setPhase(GamePhase.EVENT);
                        break;

                    case EVENT:
                        System.out.println();
                        System.out.println("New event...");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));

                        EventController.pickEventCard(gameManager);

                        System.out.println(gameManager.getGame().getActiveEventCard().getType().toString());

                        String eventType = gameManager.getGame().getActiveEventCard().getType().toString();
                        int travelersCount = gameManager.getGame().getBoard().getNumTravelers();

                        // Checks if remains only a traveler and picked event is battlezone
                        if (eventType.equals(CardType.BATTLEZONE.toString()) && travelersCount == 1) {
                            gameManager.broadcastGameMessage("This event card had been skipped");

                            game.setPhase(GamePhase.TRAVEL);
                            break;
                        }

                        gameManager.createEventController();
                        gameManager.getEventController().start();

                        // After event
                        gameManager.getGame().setActiveEventCard(null);
                        gameManager.broadcastGameMessage("This event card is finished");

                        // Checks if there isn't any traveler remaining
                        if (gameManager.getGame().getBoard().getNumTravelers() == 0)
                            game.setPhase(GamePhase.ENDGAME);
                        else{
                            gameManager.getGame().getBoard().updateTurnOrder();
                            game.setPhase(GamePhase.TRAVEL);
                        }
                        break;

                    case TRAVEL:
                        System.out.println("Travel phase started...");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));
                        EventController.handleDefeatedPlayers(gameManager);

                        if(gameManager.getGame().getEventDeckSize() > 0){
                            // Asks for each traveler if he wants to continue travel
                            for (Player player : gameManager.getGame().getBoard().getCopyTravelers()) {
                                Sender sender = gameManager.getSenderByPlayer(player);
                                sender.sendMessage("AskContinueTravel");
                            }

                            resetAndWaitTravelersReady();

                            // Checks if there is at least a traveler remaining
                            if (gameManager.getGame().getBoard().getNumTravelers() > 0) {
                                game.setPhase(GamePhase.EVENT);
                                break;
                            }
                        }

                        game.setPhase(GamePhase.ENDGAME);
                        break;

                    case ENDGAME:
                        System.out.println("Game over");
                        gameManager.broadcastGameMessage(new NewGamePhaseMessage(gameManager.getGame().getPhase().toString()));

                        gameManager.broadcastGameMessage(new ScoreBoardMessage(gameManager.getGame().scoreBoard()));

                        for (Player player : gameManager.getGame().scoreBoard()) {
                            Sender sender = gameManager.getSenderByPlayer(player);

                            if(player.getCredits() > 0)
                                sender.sendMessage("You have won");
                            else
                                sender.sendMessage("You have lost");
                        }
                        return;
                }
            }
        } catch (InterruptedException | RemoteException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void waitFirstNotify() throws InterruptedException {
        synchronized (gameThreadLock) {
            gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until the player is ready
     *
     * @author Alessandro
     */
    public void resetAndWaitPlayerReady(Player player) throws InterruptedException {
        player.setIsReady(false, gameManager.getGame());

        synchronized (gameThreadLock) {
            while (!player.getIsReady())
                gameThreadLock.wait();
        }
    }

    /**
     * Reset players ready and pauses the game thread until all players are ready to continue
     *
     * @author Alessandro
     */
    public void resetAndWaitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();
        game.resetReadyPlayers();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until all players are ready to continue
     *
     * @author Alessandro
     */
    public void waitPlayersReady() throws InterruptedException {
        Game game = gameManager.getGame();

        synchronized (gameThreadLock) {
            while (game.getNumReadyPlayers() != game.getMaxNumPlayers())
                gameThreadLock.wait();
        }
    }

    /**
     * Pauses the game thread until all travelers are ready to continue
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