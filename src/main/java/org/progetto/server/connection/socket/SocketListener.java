package org.progetto.server.connection.socket;

import org.progetto.messages.CreateGameMessage;
import org.progetto.messages.InitGameMessage;
import org.progetto.messages.JoinGameMessage;
import org.progetto.messages.NotifyNewGameMessage;
import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.GameControllersQueue;
import org.progetto.server.controller.InitController;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

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

    @Override
    public void run() {
        try {
            while (running) {
                Object messageObj = in.readObject();
                if(clientHandler.getGameController() == null)
                    handlerLobbyMessages(messageObj);
                else
                    handlerGameMessages(messageObj);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    private void handlerLobbyMessages(Object messageObj) {
        if (messageObj instanceof CreateGameMessage createGameMessage) {

            int idGame = SocketServer.getCurrentIdGameAndIncrement();
            int levelGame = createGameMessage.getLevelGame();
            int numPlayers = createGameMessage.getNumPlayers();
            String name = createGameMessage.getName();

            clientHandler.setGameControllerAndJoin(new GameController(idGame, numPlayers, levelGame), new Player(name, 0, levelGame));

            Game game = clientHandler.getGameController().getGame();
            Player player = clientHandler.getPlayer();
            BuildingBoard buildingBoard =  player.getSpaceship().getBuildingBoard();
            clientHandler.getSocketWriter().sendMessage(new InitGameMessage(game.getBoard().getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
            SocketServer.broadcastMessage(new NotifyNewGameMessage(idGame));

        } else if (messageObj instanceof JoinGameMessage joinGameMessage) {

            int idGame = joinGameMessage.getIdGame();
            String name = joinGameMessage.getName();


            GameController gameController = GameControllersQueue.getGameController(idGame);
            Game game = gameController.getGame();

            if(game.checkAvailableName(name)){

                Player player = new Player(name, game.getPlayersSize(), game.getLevel());
                BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

                clientHandler.setGameControllerAndJoin(gameController, player);
                clientHandler.getSocketWriter().sendMessage("AllowedToJoinGame");
                clientHandler.getSocketWriter().sendMessage(new InitGameMessage(game.getBoard().getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
            }else{
                clientHandler.getSocketWriter().sendMessage("NotAllowedToJoinGame");
            }
        }
    }

    private void handlerGameMessages(Object messageObj) {
        GameController gameController = clientHandler.getGameController();
        Game game = gameController.getGame();
        Player player = clientHandler.getPlayer();

        switch (game.getPhase()) {
            case INIT:
                InitController.handle(gameController::startTimer, gameController::broadcastMessage, game, player, messageObj);
                break;

            case BUILDING:
                if(gameController.getTimerObj().getTimerInt() > 0)
                    BuildingController.handle(gameController::broadcastMessage, clientHandler.getSocketWriter(), game, player, messageObj);
                else
                    clientHandler.getSocketWriter().sendMessage("TimerExpired");
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