package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.server.controller.BuildingController;
import org.progetto.server.controller.EventController;
import org.progetto.server.controller.GameController;
import org.progetto.server.connection.games.GameCommunicationHandler;
import org.progetto.server.connection.games.GameCommunicationHandlerMaps;
import org.progetto.server.controller.LobbyController;
import org.progetto.server.internalMessages.InternalGameInfo;
import org.progetto.server.model.Board;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Handles model's methods invocation made by RMI clients
 */
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

        GameCommunicationHandler gameCommunicationHandler = internalGameInfo.getGameManager();
        Game game = gameCommunicationHandler.getGame();
        int idGame = game.getId();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeLobbyRmiClient(virtualClient);
        gameCommunicationHandler.addRmiClient(player, virtualClient);
        GameCommunicationHandlerMaps.addWaitingGameManager(idGame, gameCommunicationHandler);

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

        GameCommunicationHandler gameCommunicationHandler = internalGameInfo.getGameManager();
        Game game = gameCommunicationHandler.getGame();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeLobbyRmiClient(virtualClient);
        gameCommunicationHandler.addRmiClient(player, virtualClient);

        virtualClient.sendMessage("AllowedToJoinGame");
        virtualClient.sendMessage(new GameInfoMessage(idGame, board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void startGame(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameController.startGame(GameCommunicationHandlerMaps.getGameManager(idGame), virtualClient);
    }

    @Override
    public void pickHiddenComponent(VirtualClient virtualClient, int idGame) throws RemoteException{
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickHiddenComponent(gameCommunicationHandler, player, virtualClient);
    }

    @Override
    public void pickVisibleComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException{
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickVisibleComponent(gameCommunicationHandler, player, idx, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(VirtualClient virtualClient, int idGame, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickHiddenComponent(gameCommunicationHandler, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(VirtualClient virtualClient, int idGame, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx) throws RemoteException{
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickVisibleComponent(gameCommunicationHandler, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, componentIdx, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(VirtualClient virtualClient, int idGame, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickUpEventCardDeck(gameCommunicationHandler, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, deckIdx, virtualClient);
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(VirtualClient virtualClient, int idGame, int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int idx) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeHandComponentAndPickBookedComponent(gameCommunicationHandler, player, yPlaceComponent, xPlaceComponent, rPlaceComponent, idx, virtualClient);
    }

    /**
     * Allows client to call for discardComponent with RMI in server proxy
     * @author Lorenzo
     * @param virtualClient is the interface we want to address to
     * @param idGame were we want to discard
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void discardComponent(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.discardComponent(gameCommunicationHandler, player, virtualClient);
    }

    /**
     * Allows client to call for bookedComponent with RMI in server proxy
     * @author lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to discard
     * @param idx in the array where we want to insert the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void bookComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.bookComponent(gameCommunicationHandler, player, idx, virtualClient);
    }

    /**
     * Allows client to call for pickBookedComponent with RMI in server proxy
     * @author lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to pick
     * @param idx in the array where we want to pick the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void pickBookedComponent(VirtualClient virtualClient, int idGame, int idx) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickBookedComponent(gameCommunicationHandler, player, idx, virtualClient);
    }

    @Override
    public void pickUpEventCardDeck(VirtualClient virtualClient, int idGame, int deckIdx) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.pickUpEventCardDeck(gameCommunicationHandler, player, deckIdx, virtualClient);
    }

    @Override
    public void putDownEventCardDeck(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.putDownEventCardDeck(gameCommunicationHandler, player, virtualClient);
    }

    /**
     * Allows client to call for destroyComponent with RMI in server proxy
     * @author Lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame were we want to remove
     * @param yComponent coordinate of the component
     * @param xComponent coordinate of the component
     * @throws RemoteException if a player with name in idGame was not found
     */
    @Override
    public void destroyComponent(VirtualClient virtualClient, int idGame, int yComponent, int xComponent) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.destroyComponent(gameCommunicationHandler, player, yComponent, xComponent, virtualClient);
    }

    @Override
    public void playerReady(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.playerReady(gameCommunicationHandler, player, virtualClient);
    }

    @Override
    public void resetTimer(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);

        BuildingController.resetTimer(gameCommunicationHandler, virtualClient);
    }

    /**
     * Allows client to call for pickEventCard with RMI in server proxy
     * @author Lorenzo
     * @param virtualClient is the interface we want to address
     * @param idGame is the game instances were the card will be picked
     * @throws RemoteException if an exception is called in the inner methods
     */
    @Override
    public void pickEventCard(VirtualClient virtualClient, int idGame) throws RemoteException {

        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        EventController.pickEventCard(gameCommunicationHandler, virtualClient);

    }

    @Override
    public void rollDice(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameCommunicationHandler gameCommunicationHandler = GameCommunicationHandlerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameCommunicationHandler.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventController.rollDice(gameCommunicationHandler, player, virtualClient);
    }
}