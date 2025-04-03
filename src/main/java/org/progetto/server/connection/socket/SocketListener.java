package org.progetto.server.connection.socket;

import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.messages.toServer.*;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.*;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

/**
 * Socket message listener for messages coming from a single client
 */
public class SocketListener extends Thread {

    private final ClientHandler clientHandler;
    private final ObjectInputStream in;
    private boolean running = true;

    public SocketListener(ClientHandler clientHandler, ObjectInputStream in) {
        this.clientHandler = clientHandler;
        this.in = in;
    }

    /**
     * Method that receives the messages sent by the socket clients
     */
    @Override
    public void run() {
        try {
            while (running) {
                Object messageObj = in.readObject();
                if(clientHandler.getGameManager() == null)
                    handlerLobbyMessages(messageObj);
                else
                    handlerGameMessages(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // Methods that handle the messages by calling the necessary functions
    // =======================

    private void handlerLobbyMessages(Object messageObj) {
        if (messageObj instanceof CreateGameMessage createGameMessage) {
            int levelGame = createGameMessage.getLevelGame();
            int numPlayers = createGameMessage.getNumPlayers();
            String name = createGameMessage.getName();

            InternalGameInfo internalGameInfo = LobbyController.createGame(name, levelGame, numPlayers);

            GameManager gameManager = internalGameInfo.getGameManager();
            Game game = gameManager.getGame();
            int idGame = game.getId();
            Board board = game.getBoard();
            Player player = internalGameInfo.getPlayer();
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            clientHandler.initPlayerConnection(gameManager, player);

            LobbyController.broadcastLobbyMessageToOthers(new NotifyNewGameMessage(gameManager.getGame().getId()), clientHandler.getSocketWriter());
            clientHandler.getSocketWriter().sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));

        } else if (messageObj instanceof JoinGameMessage joinGameMessage) {
            int idGame = joinGameMessage.getIdGame();
            String name = joinGameMessage.getName();

            InternalGameInfo internalGameInfo = null;
            try {
                internalGameInfo = LobbyController.joinGame(idGame, name);
            } catch (IllegalStateException e) {
                if(e.getMessage().equals("NotAvailableName"))
                    clientHandler.getSocketWriter().sendMessage("NotAvailableName");
                return;
            }

            GameManager gameManager = internalGameInfo.getGameManager();
            Game game = gameManager.getGame();
            Board board = game.getBoard();
            Player player = internalGameInfo.getPlayer();
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            clientHandler.initPlayerConnection(gameManager, player);
            clientHandler.getSocketWriter().sendMessage("AllowedToJoinGame");
            clientHandler.getSocketWriter().sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
        }
    }

    private void handlerGameMessages(Object messageObj) throws RemoteException {
        SocketWriter socketWriter = clientHandler.getSocketWriter();
        GameManager gameManager = clientHandler.getGameManager();
        Game game = gameManager.getGame();
        Player player = clientHandler.getPlayer();

        switch (game.getPhase()) {
            case INIT:
                GameController.startGame(gameManager, socketWriter);
                break;

            case BUILDING:
                if (messageObj instanceof PlaceHandComponentAndPickHiddenComponentMessage placeHandComponentAndPickComponentMessage) {
                    int yPlaceComponent = placeHandComponentAndPickComponentMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickComponentMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickComponentMessage.getRotation();
                    BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickVisibleComponentMessage placeHandComponentAndPickVisibleComponentMessage) {
                    int yPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getRotation();
                    int componentIdx = placeHandComponentAndPickVisibleComponentMessage.getComponentIdx();
                    BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, componentIdx, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickUpEventCardDeckMessage placeHandComponentAndPickUpEventCardDeckMessage) {
                    int yPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getRotation();
                    int deckIdx = placeHandComponentAndPickUpEventCardDeckMessage.getIdxDeck();
                    BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, deckIdx, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickBookedComponentMessage placeHandComponentAndPickBookedComponentMessage) {
                    int yPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getRotation();
                    int idx = placeHandComponentAndPickBookedComponentMessage.getIdx();
                    BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, idx, socketWriter);
                }

                else if (messageObj instanceof PickVisibleComponentMessage pickVisibleComponent) {
                    int componentIdx = pickVisibleComponent.getComponentIdx();
                    BuildingController.pickVisibleComponent(gameManager, player, componentIdx, socketWriter);
                }

                else if (messageObj instanceof PickUpEventCardDeckMessage pickUpEventCardDeck) {
                    int deckIdx = pickUpEventCardDeck.getDeckIdx();
                    BuildingController.pickUpEventCardDeck(gameManager, player, deckIdx, socketWriter);
                }

                else if (messageObj instanceof BookComponentMessage bookComponentMessage) {      //handle incoming book message
                    int idx = bookComponentMessage.getBookIdx();
                    BuildingController.bookComponent(gameManager, player, idx, socketWriter);

                } else if (messageObj instanceof PickBookedComponentMessage bookedComponentMessage) {
                    int idx = bookedComponentMessage.getIdx();
                    BuildingController.pickBookedComponent(gameManager, player, idx, socketWriter);

                } else if (messageObj instanceof DestroyComponentMessage destroyComponentMessage ) {        //handle incoming destroy message
                    int y = destroyComponentMessage.getY();
                    int x = destroyComponentMessage.getX();
                    BuildingController.destroyComponent(gameManager, player, y, x, socketWriter);

                }

                else if (messageObj instanceof String messageString) {
                    switch (messageString){
                        case "PickHiddenComponent":
                            BuildingController.pickHiddenComponent(gameManager, player, socketWriter);
                            break;

                        case "DiscardComponent":
                            BuildingController.discardComponent(gameManager, player, socketWriter);
                            break;

                        case "PutDownEventCardDeck":
                            BuildingController.putDownEventCardDeck(gameManager, player, socketWriter);
                            break;

                        case "Ready":
                            BuildingController.playerReady(gameManager, player, socketWriter);
                            break;

                        case "ResetTimer":
                            BuildingController.resetTimer(gameManager, socketWriter);
                            break;

                        default:
                            break;
                    }
                }
                break;

            case TRAVEL:
                if(messageObj instanceof String messageString) {
                    switch (messageString){

                        default:
                            break;
                    }
                }

                break;

            case EVENT:
                if (messageObj instanceof String messageString) {
                    switch (messageString){
                        case "RollDice":
                            gameManager.getEventController().rollDice(player, socketWriter);
                            break;

                        default:
                            break;
                    }
                }
                break;

            case ENDGAME:

                break;

            default:
                System.out.println("InvalidGamePhase");
                break;
        }
    }

    public void stopListener() {
        running = false;
        try {
            if (in != null)
                in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}