package org.progetto.server.connection.socket;

import org.progetto.messages.toServer.*;
import org.progetto.server.connection.ServerDisconnectionDetection;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.*;
import org.progetto.server.controller.events.EventControllerAbstract;
import org.progetto.server.model.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.io.UTFDataFormatException;

public class SocketListener extends Thread {

    private final ClientHandler clientHandler;
    private final ObjectInputStream in;
    private boolean running = true;

    public SocketListener(ClientHandler clientHandler, ObjectInputStream in) {
        this.clientHandler = clientHandler;
        this.in = in;
        this.setName("SocketListenerThread");
    }

    /**
     * Method that receives the messages sent by the socket clients
     *
     * @author Alessandro
     */
    @Override
    public void run() {
        try {
            while (running) {

                try {
                    Object messageObj = in.readObject();
                    if (clientHandler.getGameManager() == null)
                        handlerLobbyMessages(messageObj);
                    else
                        handlerGameMessages(messageObj);

                }catch (StreamCorruptedException | UTFDataFormatException | ClassNotFoundException e) {
                    System.err.println("Error reading the object from the stream");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {

            System.err.println("Client unreachable");
        }
    }

    // =======================
    // Methods that handle the messages by calling the necessary functions
    // =======================

    /**
     * Method that handle lobby requests
     *
     * @author Alessandro
     * @param messageObj the message object received from the client
     */
    private synchronized void handlerLobbyMessages(Object messageObj) {

        if (messageObj instanceof CreateGameMessage createGameMessage) {
            int levelGame = createGameMessage.getLevelGame();
            int numPlayers = createGameMessage.getNumPlayers();
            String name = createGameMessage.getName();

            try {
                GameManager gameManager = LobbyController.createGame(name, levelGame, numPlayers, clientHandler.getSocketWriter());
                clientHandler.setGameManager(gameManager);
                clientHandler.setPlayer(gameManager.getPlayerBySender(clientHandler.getSocketWriter()));

            } catch (IllegalStateException e) {
                clientHandler.getSocketWriter().sendMessage(e.getMessage());
            }
        }

        else if (messageObj instanceof JoinGameMessage joinGameMessage) {
            int idGame = joinGameMessage.getIdGame();
            String name = joinGameMessage.getName();

            try {
                GameManager gameManager = LobbyController.joinGame(idGame, name, clientHandler.getSocketWriter());
                clientHandler.setGameManager(gameManager);
                clientHandler.setPlayer(gameManager.getPlayerBySender(clientHandler.getSocketWriter()));
            } catch (IllegalStateException e) {
                clientHandler.getSocketWriter().sendMessage(e.getMessage());
            }
        }

        else if (messageObj instanceof ReconnectMessage reconnectMessage) {
            int idGame = reconnectMessage.getIdGame();
            String playerName = reconnectMessage.getNamePlayer();

            GameManager gameManager;
            try {
                gameManager = LobbyController.reconnectToGame(idGame, playerName, clientHandler.getSocketWriter());
            } catch (IllegalStateException e) {

                if(e.getMessage().equals("FailedToReconnect"))
                    clientHandler.getSocketWriter().sendMessage("FailedToReconnect");

                return;
            }

            clientHandler.setGameManager(gameManager);
            clientHandler.setPlayer(gameManager.getPlayerBySender(clientHandler.getSocketWriter()));
        }

        else if (messageObj instanceof String messageString) {
            switch (messageString){
                case "Pong":
                    ServerDisconnectionDetection.setPongIsArrived(clientHandler.getSocketWriter());
                    break;

                case "UpdateGameList":
                    LobbyController.showWaitingGames(clientHandler.getSocketWriter());
                    break;

                default:
                    System.out.println(messageString + " not allowed in lobby");
                    break;
            }
        }
    }

    /**
     * Method that handle game requests
     *
     * @author Alessandro, Gabriele
     * @param messageObj the message object received from the client
     */
    private void handlerGameMessages(Object messageObj) {
        SocketWriter socketWriter = clientHandler.getSocketWriter();
        GameManager gameManager = clientHandler.getGameManager();
        Player player = clientHandler.getPlayer();

        if(messageObj instanceof String messageString && messageString.equals("Pong")){
            ServerDisconnectionDetection.setPongIsArrived(socketWriter);
            return;
        }

        if(messageObj instanceof String messageString && messageString.equals("LeaveGame")){
            gameManager.leaveGame(player, socketWriter);
            clientHandler.setGameManager(null);
            clientHandler.setPlayer(null);
            return;
        }

        // Freeze game
        if(gameManager.getGame().getPlayersSize() == 1)
            return;

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

            BuildingController.placeHandComponentAndPickUpEventCardDeck(gameManager, player, xPlaceComponent, yPlaceComponent, rPlaceComponent, deckIdx, socketWriter);
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

            SpaceshipController.showSpaceship(gameManager, owner, socketWriter);
        }

        else if(messageObj instanceof BuildSpaceshipMessage buildSpaceshipMessage) {
            int idShip = buildSpaceshipMessage.getIdShip();

            BuildingController.buildShip(gameManager, player, idShip, socketWriter);
        }

        else if(messageObj instanceof DestroyComponentMessage destroyComponentMessage) {
            int x = destroyComponentMessage.getX();
            int y = destroyComponentMessage.getY();

            SpaceshipController.startDestroyComponent(gameManager, player, x, y, socketWriter);
        }

        else if (messageObj instanceof ResponsePlaceAlienMessage responsePlaceAlien) {
            String color = responsePlaceAlien.getColor();
            int x = responsePlaceAlien.getX();
            int y = responsePlaceAlien.getY();

            PopulatingController.receivePlaceAlien(gameManager, player, x, y, color);
        }

        else if (messageObj instanceof ResponseStartingPositionMessage responseStartingPositionMessage) {
            int startingPosition = responseStartingPositionMessage.getStartingPosition();

            PositioningController.receiveStartingPosition(gameManager, player, startingPosition, socketWriter);
        }

        else if(messageObj instanceof ResponseHowManyDoubleCannonsMessage responseHowManyDoubleCannonsMessage) {
            int howManyWantToUse = responseHowManyDoubleCannonsMessage.getHowManyWantToUse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveHowManyCannonsToUse(player, howManyWantToUse, socketWriter);
        }

        else if(messageObj instanceof ResponseHowManyDoubleEnginesMessage responseHowManyDoubleEnginesMessage) {
            int howManyWantToUse = responseHowManyDoubleEnginesMessage.getHowManyWantToUse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveHowManyEnginesToUse(player, howManyWantToUse, socketWriter);
        }

        else if(messageObj instanceof ResponseBatteryToDiscardMessage responseBatteryToDiscardMessage) {
            int xBatteryStorage = responseBatteryToDiscardMessage.getXBatteryStorage();
            int yBatteryStorage = responseBatteryToDiscardMessage.getYBatteryStorage();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveDiscardedBatteries(player, xBatteryStorage, yBatteryStorage, socketWriter);
        }

        else if(messageObj instanceof ResponseCrewToDiscardMessage responseCrewToDiscardMessage){
            int xHousingUnit = responseCrewToDiscardMessage.getXHousingUnit();
            int yHousingUnit = responseCrewToDiscardMessage.getYHousingUnit();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveDiscardedCrew(player, xHousingUnit, yHousingUnit, socketWriter);
        }

        else if(messageObj instanceof ResponseBoxToDiscardMessage responseBoxToDiscardMessage){
            int idx = responseBoxToDiscardMessage.getIdx();
            int xBoxStorage = responseBoxToDiscardMessage.getXBoxStorage();
            int yBoxStorage = responseBoxToDiscardMessage.getYBoxStorage();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveDiscardedBox(player, xBoxStorage, yBoxStorage, idx, socketWriter);
        }

        else if(messageObj instanceof ResponseChooseToUseShieldMessage responseChooseToUseShieldMessage){
            String response = responseChooseToUseShieldMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveProtectionDecision(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponseUseDoubleCannonRequestMessage responseUseDoubleCannonRequestMessage){
            String response = responseUseDoubleCannonRequestMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveProtectionDecision(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponseAcceptRewardCreditsAndPenaltiesMessage responseAcceptRewardCreditsAndPenaltiesMessage){
            String response = responseAcceptRewardCreditsAndPenaltiesMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveRewardAndPenaltiesDecision(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponseLandRequestMessage responseLandRequestMessage){
            String response = responseLandRequestMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveDecisionToLand(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponseAcceptRewardCreditsAndPenaltyDaysMessage responseAcceptRewardCreditsAndPenaltyDaysMessage){
            String response = responseAcceptRewardCreditsAndPenaltyDaysMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveRewardDecision(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponseAcceptRewardBoxesAndPenaltyDaysMessage responseAcceptRewardBoxesAndPenaltyDaysMessage){
            String response = responseAcceptRewardBoxesAndPenaltyDaysMessage.getResponse();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveRewardDecision(player, response, socketWriter);
        }

        else if(messageObj instanceof ResponsePlanetLandRequestMessage responsePlanetLandRequestMessage){
            int planetIdx = responsePlanetLandRequestMessage.getIdx();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveDecisionToLandPlanet(player, planetIdx, socketWriter);
        }

        else if(messageObj instanceof ResponseRewardBoxMessage responseRewardBoxMessage){
            int rewardIdxBox = responseRewardBoxMessage.getRewardIdxBox();
            int idx = responseRewardBoxMessage.getIdx();
            int xBoxStorage = responseRewardBoxMessage.getXBoxStorage();
            int yBoxStorage = responseRewardBoxMessage.getYBoxStorage();

            EventControllerAbstract eventController = gameManager.getEventController();
            if(eventController == null){
                socketWriter.sendMessage("EventControllerNull");
                return;
            }

            eventController.receiveRewardBox(player, rewardIdxBox, xBoxStorage, yBoxStorage, idx, socketWriter);
        }

        else if (messageObj instanceof MoveBoxMessage moveBoxMessage) {
            int xStart = moveBoxMessage.getxStart();
            int yStart = moveBoxMessage.getyStart();
            int idxStart = moveBoxMessage.getIdxStart();
            int xDestination = moveBoxMessage.getxDestination();
            int yDestination = moveBoxMessage.getyDestination();
            int idxDestination = moveBoxMessage.getIdxDestination();

            SpaceshipController.moveBox(gameManager, player, xStart, yStart, idxStart, xDestination, yDestination, idxDestination, socketWriter);
        }

        else if (messageObj instanceof RemoveBoxMessage removeBoxMessage) {
            int x = removeBoxMessage.getxBoxStorage();
            int y = removeBoxMessage.getyBoxStorage();
            int idx = removeBoxMessage.getIdx();

            SpaceshipController.removeBox(gameManager, player, x, y, idx, socketWriter);
        }

        else if (messageObj instanceof ResponseSelectSpaceshipPartMessage responseSelectSpaceshipPart){
            int x = responseSelectSpaceshipPart.getX();
            int y = responseSelectSpaceshipPart.getY();

            SpaceshipController.chooseSpaceshipPartToKeep(gameManager, player, x, y, socketWriter);
        }

        else if (messageObj instanceof ResponseContinueTravelMessage responseContinueTravelMessage ) {
            String response = responseContinueTravelMessage.getResponse();

            EventController.chooseToContinueTravel(gameManager, response, player, socketWriter);
        }

        else if (messageObj instanceof String messageString){

            switch (messageString) {
                case "Ready":
                    if(gameManager.getGame().getPhase().equals(GamePhase.BUILDING)) {
                        BuildingController.readyBuilding(gameManager, player, socketWriter);

                    } else if (gameManager.getGame().getPhase().equals(GamePhase.INIT) || gameManager.getGame().getPhase().equals(GamePhase.POPULATING)) {
                        GameController.ready(gameManager, player, socketWriter);
                    }
                    break;

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
                    BuildingController.showVisibleComponents(gameManager, socketWriter);
                    break;

                case "ShowBookedComponents":
                    BuildingController.showBookedComponents(gameManager, player, socketWriter);
                    break;

                case "PlayerStats":
                    GameController.playerStats(gameManager, player, socketWriter);
                    break;

                case "ShowPlayers":
                    GameController.showPlayers(gameManager, socketWriter);
                    break;

                case "ShowStartingPositions":
                    PositioningController.showStartingPositions(gameManager, socketWriter);
                    break;

                case "ShowPlayersInPositioningDecisionOrder":
                    PositioningController.showPlayersInPositioningDecisionOrder(gameManager, socketWriter);
                    break;

                case "SpaceshipStats":
                    SpaceshipController.spaceshipStats(gameManager, player, socketWriter);
                    break;

                case "ShowTrack":
                    GameController.showTrack(gameManager, socketWriter);
                    break;

                case "ResetTimer":
                    BuildingController.resetTimer(gameManager, player, socketWriter);
                    break;

                case "RollDice":
                    EventControllerAbstract eventController = gameManager.getEventController();
                    if(eventController == null){
                        socketWriter.sendMessage("EventControllerNull");
                        return;
                    }

                    eventController.rollDice(player, socketWriter);
                    break;

                default:

                    System.out.println(messageString + " not allowed in game");
                    break;
            }
        }
    }
}