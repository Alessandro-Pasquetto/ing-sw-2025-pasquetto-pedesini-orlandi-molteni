package org.progetto.server.connection.rmi;

import org.progetto.client.connection.rmi.VirtualClient;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.server.controller.*;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameManagerMaps;
import org.progetto.server.controller.events.EventControllerAbstract;
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
    public void showWaitingGames(VirtualClient virtualClient) throws RemoteException {
        LobbyController.showWaitingGames(virtualClient);
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

        virtualClient.sendMessage(new GameInfoMessage(idGame, game.getLevel(), board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void joinGame(VirtualClient virtualClient, int idGame, String name) throws RemoteException {
        InternalGameInfo internalGameInfo = null;
        try {
            internalGameInfo = LobbyController.joinGame(idGame, name);
        } catch (IllegalStateException e) {
            virtualClient.sendMessage(e.getMessage());
            return;
        }

        GameManager gameManager = internalGameInfo.getGameManager();
        Game game = gameManager.getGame();
        Board board = game.getBoard();
        Player player = internalGameInfo.getPlayer();
        BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

        RmiServer.removeLobbyRmiClient(virtualClient);
        gameManager.addRmiClient(player, virtualClient);

        virtualClient.sendMessage(new GameInfoMessage(idGame, game.getLevel(), board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
    }

    @Override
    public void showHandComponent(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.showHandComponent(gameManager, player, virtualClient);
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
    public void buildShip(VirtualClient virtualClient, int idGame, int idShip) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.buildShip(gameManager, player, idShip, virtualClient);
    }

    @Override
    public void showVisibleComponents(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.showVisibleComponents(gameManager, player, virtualClient);
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
    public void placeComponent(VirtualClient virtualClient, int idGame, int xPlaceComponent, int yPlaceComponent, int rPlaceComponent) throws RemoteException{
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try {
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.placeComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, virtualClient);
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
     * @author Lorenzo
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

    @Override
    public void showBookedComponents(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        } catch (IllegalStateException e) {
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        BuildingController.showBookedComponents(gameManager, player, virtualClient);
    }

    /**
     * Allows client to call for pickBookedComponent with RMI in server proxy
     *
     * @author Lorenzo
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
    public void destroyComponent(VirtualClient virtualClient, int idGame, int xComponent, int yComponent) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        SpaceshipController.startDestroyComponent(gameManager, player, xComponent, yComponent, virtualClient);
    }

    @Override
    public void populateComponent (VirtualClient virtualClient, int idGame, String crewType, int xComponent, int yComponent) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        SpaceshipController.populateComponent(gameManager, player, crewType, xComponent, yComponent, virtualClient);
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

        if(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) {
            BuildingController.readyBuilding(gameManager, player, virtualClient);

        } else if (gameManager.getGame().getPhase().equals(GamePhase.INIT) || gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) {
            GameController.ready(gameManager, player, virtualClient);
        }
    }

    @Override
    public void resetTimer(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);

        BuildingController.resetTimer(gameManager, virtualClient);
    }

    @Override
    public void playerStats(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        GameController.playerStats(gameManager, player, virtualClient);
    }

    @Override
    public void showSpaceship(VirtualClient virtualClient, int idGame, String owner) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        SpaceshipController.showSpaceship(gameManager, owner, virtualClient);
    }

    @Override
    public void spaceshipStats(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        SpaceshipController.spaceshipStats(gameManager, player, virtualClient);
    }

    @Override
    public void showTrack(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        GameController.showTrack(gameManager, virtualClient);
    }

    @Override
    public void rollDice(VirtualClient virtualClient, int idGame) throws RemoteException, InterruptedException {
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
    public void responseHowManyDoubleCannons(VirtualClient virtualClient, int idGame, int howManyWantToUse) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveHowManyCannonsToUse(player, howManyWantToUse, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseHowManyDoubleEngines(VirtualClient virtualClient, int idGame, int howManyWantToUse) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveHowManyEnginesToUse(player, howManyWantToUse, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseBatteryToDiscard(VirtualClient virtualClient, int idGame, int xBatteryStorage, int yBatteryStorage) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveDiscardedBatteries(player, xBatteryStorage, yBatteryStorage, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseCrewToDiscard(VirtualClient virtualClient, int idGame, int xHousingUnit, int yHousingUnit) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveDiscardedCrew(player, xHousingUnit, yHousingUnit, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseBoxToDiscard(VirtualClient virtualClient, int idGame, int xBoxStorage, int yBoxStorage, int idx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveDiscardedBox(player, xBoxStorage, yBoxStorage, idx, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseChooseToUseShield(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveProtectionDecision(player, response, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseUseDoubleCannonRequest(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveProtectionDecision(player, response, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenalties(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveRewardAndPenaltiesDecision(player, response, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseLandRequest(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveDecisionToLand(player, response, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseAcceptRewardCreditsAndPenaltyDays(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveRewardAndPenaltiesDecision(player, response, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responsePlanetLandRequest(VirtualClient virtualClient, int idGame, int planetIdx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveDecisionToLandPlanet(player, planetIdx, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseRewardBox(VirtualClient virtualClient, int idGame, int idxBox, int xBoxStorage, int yBoxStorage, int idx) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.receiveRewardBox(player, idxBox, xBoxStorage, yBoxStorage, idx, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseContinueTravel(VirtualClient virtualClient, int idGame, String response) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventController.chooseToContinueTravel(gameManager, response, player, virtualClient);
    }

    @Override
    public void responseRollDice(VirtualClient virtualClient, int idGame) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        EventControllerAbstract eventController = gameManager.getEventController();
        if(eventController == null){
            virtualClient.sendMessage("EventControllerNull");
            return;
        }

        try {
            eventController.rollDice(player, virtualClient);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseSelectSpaceshipPart(VirtualClient virtualClient, int idGame, int x, int y) throws RemoteException {
        GameManager gameManager = GameManagerMaps.getGameManager(idGame);
        Player player = null;
        try{
            player = gameManager.getPlayerByVirtualClient(virtualClient);
        }catch (IllegalStateException e){
            if(e.getMessage().equals("PlayerNotFound"))
                virtualClient.sendMessage("PlayerNotFound");
            return;
        }

        SpaceshipController.chooseSpaceshipPartToKeep(gameManager, player, x, y, virtualClient);
    }
}