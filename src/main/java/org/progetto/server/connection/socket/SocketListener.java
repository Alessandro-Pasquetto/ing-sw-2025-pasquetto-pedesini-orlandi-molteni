package org.progetto.server.connection.socket;

import org.progetto.messages.toServer.*;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.connection.games.GameThread;
import org.progetto.server.controller.*;
import org.progetto.server.controller.events.EventControllerAbstract;
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
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // =======================
    // Methods that handle the messages by calling the necessary functions
    // =======================

    /**
     * Method that handle lobby requests
     */
    private void handlerLobbyMessages(Object messageObj) throws RemoteException {
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

            LobbyController.broadcastLobbyMessageToOthers("UpdateGameList", clientHandler.getSocketWriter());
            clientHandler.getSocketWriter().sendMessage(new GameInfoMessage(idGame, game.getLevel(), board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));

        } else if (messageObj instanceof JoinGameMessage joinGameMessage) {
            int idGame = joinGameMessage.getIdGame();
            String name = joinGameMessage.getName();

            InternalGameInfo internalGameInfo = null;
            try {
                internalGameInfo = LobbyController.joinGame(idGame, name);
            } catch (IllegalStateException e) {
                clientHandler.getSocketWriter().sendMessage(e.getMessage());
                return;
            }

            GameManager gameManager = internalGameInfo.getGameManager();
            Game game = gameManager.getGame();
            Board board = game.getBoard();
            Player player = internalGameInfo.getPlayer();
            BuildingBoard buildingBoard = player.getSpaceship().getBuildingBoard();

            clientHandler.initPlayerConnection(gameManager, player);
            clientHandler.getSocketWriter().sendMessage(new GameInfoMessage(idGame, game.getLevel(), board.getImgSrc(), buildingBoard.getImgSrc(), buildingBoard.getImgSrcCentralUnitFromColor(player.getColor())));
        }

        else if (messageObj instanceof String messageString) {
            switch (messageString){
                case "UpdateGameList":
                    LobbyController.showWaitingGames(clientHandler.getSocketWriter());
                    break;

                default:
                    System.out.println(messageString + " not allowed");
                    break;
            }
        }
    }

    /**
     * Method that handle game requests
     */
    private void handlerGameMessages(Object messageObj) throws RemoteException, InterruptedException {
        SocketWriter socketWriter = clientHandler.getSocketWriter();
        GameManager gameManager = clientHandler.getGameManager();
        Game game = gameManager.getGame();
        Player player = clientHandler.getPlayer();

        switch (game.getPhase()) {
            case INIT:
                if (messageObj instanceof String messageString){
                    switch (messageString) {
                        case "Ready":
                            GameController.ready(gameManager, player, socketWriter);
                            break;
                        default:
                            System.out.println(messageString + " not allowed");
                            break;
                    }
                }
                break;

            case BUILDING:
                if (messageObj instanceof PlaceComponentMessage placeComponentMessage) {
                    int xPlaceComponent = placeComponentMessage.getX();
                    int yPlaceComponent = placeComponentMessage.getY();
                    int rPlaceComponent = placeComponentMessage.getRotation();
                    BuildingController.placeComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, socketWriter);
                }

                else if (messageObj instanceof PlaceLastComponentMessage placeLastComponentMessage) {
                    int xPlaceComponent = placeLastComponentMessage.getX();
                    int yPlaceComponent = placeLastComponentMessage.getY();
                    int rPlaceComponent = placeLastComponentMessage.getRotation();
                    BuildingController.placeLastComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickHiddenComponentMessage placeHandComponentAndPickComponentMessage) {
                    int xPlaceComponent = placeHandComponentAndPickComponentMessage.getX();
                    int yPlaceComponent = placeHandComponentAndPickComponentMessage.getY();
                    int rPlaceComponent = placeHandComponentAndPickComponentMessage.getRotation();
                    BuildingController.placeHandComponentAndPickHiddenComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickVisibleComponentMessage placeHandComponentAndPickVisibleComponentMessage) {
                    int xPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getX();
                    int yPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getY();
                    int rPlaceComponent = placeHandComponentAndPickVisibleComponentMessage.getRotation();
                    int componentIdx = placeHandComponentAndPickVisibleComponentMessage.getComponentIdx();
                    BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, componentIdx, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickUpEventCardDeckMessage placeHandComponentAndPickUpEventCardDeckMessage) {
                    int xPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getX();
                    int yPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getY();
                    int rPlaceComponent = placeHandComponentAndPickUpEventCardDeckMessage.getRotation();
                    int deckIdx = placeHandComponentAndPickUpEventCardDeckMessage.getIdxDeck();
                    BuildingController.placeHandComponentAndPickVisibleComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, deckIdx, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndPickBookedComponentMessage placeHandComponentAndPickBookedComponentMessage) {
                    int xPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getX();
                    int yPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getY();
                    int rPlaceComponent = placeHandComponentAndPickBookedComponentMessage.getRotation();
                    int idx = placeHandComponentAndPickBookedComponentMessage.getIdx();
                    BuildingController.placeHandComponentAndPickBookedComponent(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, idx, socketWriter);
                }

                else if (messageObj instanceof PlaceHandComponentAndReadyMessage placeHandComponentAndReadyMessageMessage) {
                    int xPlaceComponent = placeHandComponentAndReadyMessageMessage.getX();
                    int yPlaceComponent = placeHandComponentAndReadyMessageMessage.getY();
                    int rPlaceComponent = placeHandComponentAndReadyMessageMessage.getRotation();
                    BuildingController.placeHandComponentAndReady(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, socketWriter);
                }

                else if (messageObj instanceof PickVisibleComponentMessage pickVisibleComponent) {
                    int componentIdx = pickVisibleComponent.getComponentIdx();
                    BuildingController.pickVisibleComponent(gameManager, player, componentIdx, socketWriter);
                }

                else if (messageObj instanceof PickUpEventCardDeckMessage pickUpEventCardDeck) {
                    int deckIdx = pickUpEventCardDeck.getDeckIdx();
                    BuildingController.pickUpEventCardDeck(gameManager, player, deckIdx, socketWriter);
                }

                else if (messageObj instanceof BookComponentMessage bookComponentMessage) {
                    int idx = bookComponentMessage.getBookIdx();
                    BuildingController.bookComponent(gameManager, player, idx, socketWriter);
                }

                else if (messageObj instanceof PickBookedComponentMessage bookedComponentMessage) {
                    int idx = bookedComponentMessage.getIdx();
                    BuildingController.pickBookedComponent(gameManager, player, idx, socketWriter);
                }

                else if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }

                else if (messageObj instanceof String messageString) {
                    switch (messageString){

                        case "ShowHandComponent":
                            BuildingController.showHandComponent(gameManager, player, socketWriter);
                            break;

                        case "PickHiddenComponent":
                            BuildingController.pickHiddenComponent(gameManager, player, socketWriter);
                            break;

                        case "DiscardComponent":
                            BuildingController.discardComponent(gameManager, player, socketWriter);
                            break;

                        case "PutDownEventCardDeck":
                            BuildingController.putDownEventCardDeck(gameManager, player, socketWriter);
                            break;

                        case "ShowVisibleComponents":
                            BuildingController.showVisibleComponents(gameManager, player, socketWriter);
                            break;

                        case "ShowBookedComponents":
                            BuildingController.showBookedComponents(gameManager, player, socketWriter);
                            break;

                        case "SpaceshipStats":
                            SpaceshipController.spaceshipStats(gameManager, player, socketWriter);
                            break;

                        case "ShowTrack":
                            GameController.showTrack(gameManager, socketWriter);
                            break;

                        case "ResetTimer":
                            BuildingController.resetTimer(gameManager, socketWriter);
                            break;

                        case "Ready":
                            BuildingController.readyBuilding(gameManager, player, socketWriter);
                            break;

                        default:
                            System.out.println(messageString + " not allowed");
                            break;
                    }
                }
                break;

            case START_ADJUSTING:
                if(messageObj instanceof DestroyComponentMessage destroyComponentMessage) {
                    int x = destroyComponentMessage.getX();
                    int y = destroyComponentMessage.getY();
                    SpaceshipController.destroyComponentWithoutAnyCheck(gameManager, player, x, y, socketWriter);
                }

                else if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }
                break;

            case POPULATING:
                if(messageObj instanceof PopulatingMessage populatingMessage){
                    String crewType = populatingMessage.getCrewType();
                    int xComponent = populatingMessage.getxComponent();
                    int yComponent = populatingMessage.getyComponent();

                    SpaceshipController.populateComponent(player, crewType, xComponent, yComponent, socketWriter);
                }

                else if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }

                else if(messageObj instanceof String messageString) {
                    switch (messageString){
                        case "Ready":
                            player.setIsReady(true, game);
                            gameManager.getGameThread().notifyThread();
                            break;
                        default:
                            System.out.println(messageString + " not allowed");
                            break;
                    }
                }
                break;

            case ADJUSTING:
                if(messageObj instanceof DestroyComponentMessage destroyComponentMessage) {
                    int x = destroyComponentMessage.getX();
                    int y = destroyComponentMessage.getY();
                    SpaceshipController.destroyComponentAndCheckValidity(gameManager, player, x, y, socketWriter);
                }

                else if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }
                break;

            case TRAVEL:
                if(messageObj instanceof String messageString) {
                    switch (messageString){

                        default:
                            break;
                    }
                }

                else if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }

                else if( messageObj instanceof ResponseContinueTravelMessage responseContinueTravelMessage ) {
                    String response = responseContinueTravelMessage.getResponse();
                    EventController.chooseToContinueTravel(gameManager, response, player, socketWriter);
                }

                break;

            case EVENT:

                if( messageObj instanceof RequestSpaceshipMessage requestSpaceshipMessage ) {
                    String owner = requestSpaceshipMessage.getOwner();
                    SpaceshipController.showSpaceship(gameManager,owner,socketWriter);
                }

                else if(messageObj instanceof ResponseHowManyDoubleCannonsMessage responseHowManyDoubleCannonsMessage) {
                    int howManyWantToUse = responseHowManyDoubleCannonsMessage.getHowManyWantToUse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveHowManyCannonsToUse(player, howManyWantToUse, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseHowManyDoubleEnginesMessage responseHowManyDoubleEnginesMessage) {
                    int howManyWantToUse = responseHowManyDoubleEnginesMessage.getHowManyWantToUse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveHowManyEnginesToUse(player, howManyWantToUse, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseBatteryToDiscardMessage responseBatteryToDiscardMessage) {
                    int xBatteryStorage = responseBatteryToDiscardMessage.getXBatteryStorage();
                    int yBatteryStorage = responseBatteryToDiscardMessage.getYBatteryStorage();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveDiscardedBatteries(player, xBatteryStorage, yBatteryStorage, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseCrewToDiscardMessage responseCrewToDiscardMessage){
                    int xHousingUnit = responseCrewToDiscardMessage.getXHousingUnit();
                    int yHousingUnit = responseCrewToDiscardMessage.getYHousingUnit();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveDiscardedCrew(player, xHousingUnit, yHousingUnit, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseBoxToDiscardMessage responseBoxToDiscardMessage){
                    int idx = responseBoxToDiscardMessage.getIdx();
                    int xBoxStorage = responseBoxToDiscardMessage.getXBoxStorage();
                    int yBoxStorage = responseBoxToDiscardMessage.getYBoxStorage();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveDiscardedBox(player, xBoxStorage, yBoxStorage, idx, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseChooseToUseShieldMessage responseChooseToUseShieldMessage){
                    String response = responseChooseToUseShieldMessage.getResponse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveProtectionDecision(player, response, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseUseDoubleCannonRequestMessage responseUseDoubleCannonRequestMessage){
                    String response = responseUseDoubleCannonRequestMessage.getResponse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveProtectionDecision(player, response, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseAcceptRewardCreditsAndPenaltiesMessage responseAcceptRewardCreditsAndPenaltiesMessage){
                    String response = responseAcceptRewardCreditsAndPenaltiesMessage.getResponse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveRewardAndPenaltiesDecision(player, response, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseLandRequestMessage responseLandRequestMessage){
                    String response = responseLandRequestMessage.getResponse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveDecisionToLand(player, response, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseAcceptRewardCreditsAndPenaltyDaysMessage responseAcceptRewardCreditsAndPenaltyDaysMessage){
                    String response = responseAcceptRewardCreditsAndPenaltyDaysMessage.getResponse();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveRewardDecision(player, response, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponsePlanetLandRequestMessage responsePlanetLandRequestMessage){
                    int planetIdx = responsePlanetLandRequestMessage.getIdx();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveDecisionToLandPlanet(player, planetIdx, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(messageObj instanceof ResponseRewardBoxMessage responseRewardBoxMessage){
                    int idxBox = responseRewardBoxMessage.getIdxBox();
                    int idx = responseRewardBoxMessage.getIdx();
                    int xBoxStorage = responseRewardBoxMessage.getXBoxStorage();
                    int yBoxStorage = responseRewardBoxMessage.getYBoxStorage();

                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    try {
                        eventController.receiveRewardBox(player, idxBox, xBoxStorage, yBoxStorage, idx, socketWriter);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                else if (messageObj instanceof String messageString) {
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