package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.GameManager;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiServerReceiver extends UnicastRemoteObject implements VirtualServer{

    protected RmiServerReceiver() throws RemoteException {
        super();
    }

    @Override
    public void connect(VirtualView rmiClient) throws RemoteException {
        RmiServer.addRmiClient(rmiClient);
    }

    @Override
    public void createGame(VirtualView view, String name, int gameLevel, int numPlayers) throws RemoteException {

        InternalGameInfo internalGameInfo = LobbyController.createGame(name, gameLevel, numPlayers);

        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeRmiClient(view);
        gameManager.addRmiClient(view);
        RmiServer.addVirtualViewGameManager(view, gameManager);

        LobbyController.broadcastLobbyMessageToOthers(new NotifyNewGameMessage(game.getId()), null, view);
        view.sendMessage(new GameInfoMessage(board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void joinGame(VirtualView view, int idGame, String name) throws RemoteException {

        InternalGameInfo internalGameInfo = null;
        try {
            internalGameInfo = LobbyController.joinGame(idGame, name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("NotAvailableName"))
                view.sendMessage("NotAvailableName");
            return;
        }

        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeRmiClient(view);
        gameManager.addRmiClient(view);
        RmiServer.addVirtualViewGameManager(view, gameManager);

        view.sendMessage("AllowedToJoinGame");
        view.sendMessage(new GameInfoMessage(board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void startGame(VirtualView view) throws RemoteException {
        GameController.startGame(RmiServer.getVirtualViewGameManager(view));
    }

    @Override
    public void pickHiddenComponent(VirtualView view, String name) throws RemoteException{
        GameManager gameManager = RmiServer.getVirtualViewGameManager(view);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                view.sendMessage("PlayerNameNotFound");
            return;
        }
        GameController.pickHiddenComponent(gameManager.getGame(), player, null, view);
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(VirtualView view, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameManager gameManager = RmiServer.getVirtualViewGameManager(view);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                view.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.placeHandComponentAndPickHiddenComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, null, view);
    }
}