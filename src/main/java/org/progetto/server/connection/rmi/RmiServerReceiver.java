package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.server.controller.GameController;
import org.progetto.server.controller.GameManager;
import org.progetto.server.controller.GameManagersMaps;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiServerReceiver extends UnicastRemoteObject implements VirtualServer{

    // =======================
    // ATTRIBUTES
    // =======================

    protected RmiServerReceiver() throws RemoteException {
        super();
    }

    // =======================
    // OTHER METHODS
    // =======================

    // Methods called by the rmi clients

    /* LobbyController methods */

    /**
     * Add the virtualClient to the list of rmiClients in the lobby
     */
    @Override
    public void connect(VirtualClient rmiClient) throws RemoteException {
        RmiServer.addLobbyRmiClient(rmiClient);
    }

    @Override
    public void createGame(VirtualClient virtualClient, String name, int gameLevel, int numPlayers) throws RemoteException {

        InternalGameInfo internalGameInfo = LobbyController.createGame(name, gameLevel, numPlayers);

        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();
        int idGame = game.getId();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeLobbyRmiClient(virtualClient);
        gameManager.addRmiClient(virtualClient);
        GameManagersMaps.addWaitingGameManager(idGame, gameManager);

        LobbyController.broadcastLobbyMessageToOthers(new NotifyNewGameMessage(idGame), null, virtualClient);
        virtualClient.sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void joinGame(VirtualClient virtualClient, int idGame, String name) throws RemoteException {

        InternalGameInfo internalGameInfo = null;
        try {
            internalGameInfo = LobbyController.joinGame(idGame, name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("NotAvailableName"))
                virtualClient.sendMessage("NotAvailableName");
            return;
        }

        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeLobbyRmiClient(virtualClient);
        gameManager.addRmiClient(virtualClient);

        virtualClient.sendMessage("AllowedToJoinGame");
        virtualClient.sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    /* GameController methods */

    @Override
    public void startGame(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameController.startGame(GameManagersMaps.getGameManager(idGame));
    }

    @Override
    public void pickHiddenComponent(VirtualClient virtualClient, int idGame, String name) throws RemoteException{
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.pickHiddenComponent(gameManager, player, null, virtualClient);
    }

    @Override
    public void pickVisibleComponent(VirtualClient virtualClient, int idGame, String name, int idx) throws RemoteException{
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.pickVisibleComponent(gameManager, player, idx, null, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(VirtualClient virtualClient, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.placeHandComponentAndPickHiddenComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, null, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(VirtualClient virtualClient, int idGame, String name, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx) throws RemoteException{
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.placeHandComponentAndPickVisibleComponent(gameManager, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, componentIdx, null, virtualClient);
    }

    /**
     * Allows client to call for discardComponent with RMI in server proxy
     *
     * @author Lorenzo
     * @param virtualClient is the interface we want to address to
     * @param idGame were we want to discard
     * @param name of the player that want to discard
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void discardComponent(VirtualClient virtualClient, int idGame, String name) throws RemoteException {
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.discardComponent(gameManager, player, null, virtualClient);
    }


    /**
     *Allows client to call for bookedComponent with RMI in server proxy
     *
     * @Author lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to discard
     * @param name of the player that want to discard
     * @param idx in the array where we want to insert the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void bookComponent(VirtualClient virtualClient, int idGame, String name, int idx) throws RemoteException {
        GameManager gameManager = GameManagersMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getGame().getPlayerByName(name);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNameNotFound"))
                virtualClient.sendMessage("PlayerNameNotFound");
            return;
        }

        GameController.bookComponent(gameManager,player,idx,null,virtualClient);
    }

}