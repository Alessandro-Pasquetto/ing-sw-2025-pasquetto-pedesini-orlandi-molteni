package org.progetto.server.connection.socket;

import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.messages.toClient.PickedComponentMessage;
import org.progetto.messages.toServer.*;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.server.controller.*;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.*;
import org.progetto.server.model.components.Component;

import java.io.IOException;
import java.io.ObjectInputStream;

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

            LobbyController.broadcastLobbyMessageToOthers(new NotifyNewGameMessage(gameManager.getGame().getId()), clientHandler.getSocketWriter(), null);
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

    private void handlerGameMessages(Object messageObj) {
        SocketWriter socketWriter = clientHandler.getSocketWriter();
        GameManager gameManager = clientHandler.getGameManager();
        Game game = gameManager.getGame();
        Player player = clientHandler.getPlayer();

        switch (game.getPhase()) {
            case INIT:
                GameController.startGame(gameManager);
                break;

            case BUILDING:
                if (messageObj instanceof PlaceHandComponentAndPickHiddenComponentMessage placeHandComponentAndPickComponentMessage) {
                    int yPlaceComponent = placeHandComponentAndPickComponentMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickComponentMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickComponentMessage.getRotation();
                    GameController.placeHandComponentAndPickHiddenComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, socketWriter, null);

                } else if (messageObj instanceof PlaceHandComponentAndPickVisibleComponentMessage placeHandComponentAndPickVisibleComponentMessage) {
                }
                else if (messageObj instanceof PickVisibleComponent pickVisibleComponent) {
                    int componentIdx = pickVisibleComponent.getComponentIdx();
                    GameController.pickVisibleComponent(gameManager, player, componentIdx, socketWriter, null);

                }
                else if (messageObj instanceof PlaceHandComponentAndPickVisibleComponentMessage placeHandComponentAndPickVisibleComponentMessage) {
                    int yPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getRotation();
                    int componentIdx = placeHandComponentAndPickVisibleComponentMessage.getComponentIdx();
                    GameController.placeHandComponentAndPickVisibleComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, componentIdx, socketWriter, null);

                } else if (messageObj instanceof PlaceHandComponentAndPickUpEventCardDeckMessage placeHandComponentAndPickUpEventCardDeckMessage) {
                    int yPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getY();
                    int xPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getX();
                    int rPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getRotation();
                    int deckIdx = placeHandComponentAndPickUpEventCardDeckMessage.getIdxDeck();
                    GameController.placeHandComponentAndPickVisibleComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, deckIdx, socketWriter, null);

                } else if (messageObj instanceof PickVisibleComponent pickVisibleComponent) {
                    int componentIdx = pickVisibleComponent.getComponentIdx();
                    GameController.pickVisibleComponent(gameManager, player, componentIdx, socketWriter, null);

                } else if (messageObj instanceof PickUpEventCardDeck pickUpEventCardDeck) {
                    int deckIdx = pickUpEventCardDeck.getDeckIdx();
                    GameController.pickUpEventCardDeck(gameManager, player, deckIdx, socketWriter, null);

                } else if (messageObj instanceof String messageString) {
                    switch (messageString){
                        case "PickHiddenComponent":
                            GameController.pickHiddenComponent(gameManager, player, socketWriter, null);
                            break;
                }
                else if (messageObj instanceof BookComponentMessage bookComponentMessage) {      //handle incoming book message
                    int idx = bookComponentMessage.getBookIdx();
                    GameController.bookComponent(gameManager, player, idx, socketWriter, null);
                }

                else if (messageObj instanceof String messageString) {
                            switch (messageString){
                                case "PickHiddenComponent":
                                    GameController.pickHiddenComponent(gameManager, player, socketWriter, null);
                                    break;

                                case "DiscardComponent":
                                    GameController.discardComponent(gameManager,player,socketWriter,null);
                                    break;

                                default:
                                    break;
                            }
                }
                break;

            case TRAVEL:

                break;

            case EVENT:

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