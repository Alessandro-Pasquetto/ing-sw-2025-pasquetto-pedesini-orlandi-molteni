package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.GameController;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.controller.SpaceshipController;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Handles model's methods invocation made by RMI clients
 */
public class RmiServerReceiver extends UnicastRemoteObject implements VirtualServer{

    // =======================
    // CONSTRUCTORS
    // =======================

    protected RmiServerReceiver() throws RemoteException {
        super();
    }

    // =======================
    // OTHER METHODS
    // =======================

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
        gameManager.addRmiClient(player, virtualClient);
        GameManagerMaps.addWaitingGameManager(idGame, gameManager);

        LobbyController.broadcastLobbyMessageToOthers(new NotifyNewGameMessage(idGame), virtualClient);
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
        gameManager.addRmiClient(player, virtualClient);

        virtualClient.sendMessage("AllowedToJoinGame");
        virtualClient.sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void pickHiddenComponent(VirtualClient virtualClient, int idGame) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickHiddenComponent(gameManager, player, virtualClient);
    }

    @Override
    public void pickVisibleComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickVisibleComponent(gameManager, player, idx, virtualClient);
    }

    @Override
    public void placeLastComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeLastComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int componentIdx) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, componentIdx, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int deckIdx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, deckIdx, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent, int idx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, idx, virtualClient);
    }

    @Override
    public void placeHandComponentAndReady(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndReady(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, virtualClient);
    }

    /**
     * Allows client to call for discardComponent with RMI in server proxy
     *
     * @author Lorenzo
     * @param virtualClient is the interface we want to address to
     * @param idGame were we want to discard
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void discardComponent(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.discardComponent(gameManager, player, virtualClient);
    }

    /**
     * Allows client to call for bookedComponent with RMI in server proxy
     *
     * @author lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to discard
     * @param idx in the array where we want to insert the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void bookComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.bookComponent(gameManager, player, idx, virtualClient);
    }

    /**
     * Allows client to call for pickBookedComponent with RMI in server proxy
     *
     * @author lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to pick
     * @param idx in the array where we want to pick the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void pickBookedComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickBookedComponent(gameManager, player, idx, virtualClient);
    }

    @Override
    public void pickUpEventCardDeck(VirtualClient virtualClient, int idGame, int deckIdx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickUpEventCardDeck(gameManager, player, deckIdx, virtualClient);
    }

    @Override
    public void putDownEventCardDeck(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.putDownEventCardDeck(gameManager, player, virtualClient);
    }

    /**
     * Allows client to call for destroyComponent with RMI in server proxy
     *
     * @author Lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to remove
     * @param yComponent coordinate of the component
     * @param xComponent coordinate of the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void destroyComponent(VirtualClient virtualClient, int idGame, int yComponent, int xComponent) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, xComponent, yComponent, virtualClient);
    }

    @Override
    public void playerReady(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        if(gameManager.getGame().getPhase().equals(GamePhase.BUILDING))
            BuildingController.readyBuilding(gameManager, player, virtualClient);
        else
            GameController.ready(gameManager, player, virtualClient);
    }

    @Override
    public void resetTimer(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);

        BuildingController.resetTimer(gameManager, virtualClient);
    }

    @Override
    public void rollDice(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        gameManager.getEventController().rollDice(player, virtualClient);
    }

    @Override
    public void showSpaceship(VirtualClient virtualClient,int idGame, String owner) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        SpaceshipController.showSpaceship(gameManager, owner, virtualClient);

    }

}