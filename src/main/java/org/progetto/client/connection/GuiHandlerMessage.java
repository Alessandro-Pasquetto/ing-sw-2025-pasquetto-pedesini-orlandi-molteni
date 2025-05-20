package org.progetto.client.connection;

import org.progetto.client.gui.Alerts;
import org.progetto.client.gui.DragAndDrop;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventCommon.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.messages.toClient.Smugglers.AcceptRewardBoxesAndPenaltyDaysMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.WaitingGameInfoMessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles messages coming from server
 */
public class GuiHandlerMessage {

    /**
     * Method that handles the messages coming from the server updating the GUI
     *
     * @param messageObj the message that has arrived
     */
    public static void handleMessage(Object messageObj) {

        if (messageObj instanceof WaitingGamesMessage waitingGamesMessage) {
            ArrayList<WaitingGameInfoMessage> gamesInfo = waitingGamesMessage.getWaitingGames();
            PageController.getChooseGameView().generateGameRecordList(gamesInfo);
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {

            System.out.println("You joined a game");

            int gameId = initGameMessage.getIdGame();
            int levelGame = initGameMessage.getLevelGame();
            int numMaxPlayer = initGameMessage.getNumMaxPlayers();

            try {
                PageController.switchScene("waitingRoom.fxml", "WaitingRoom");
                PageController.getWaitingRoomView().populateGameInformation(gameId, levelGame, numMaxPlayer);

            } catch (IOException e) {
                Alerts.showWarning("Error loading the page");
                System.out.println("Error loading the page");
            }

            GameData.setIdGame(gameId);
            GameData.setLevelGame(levelGame);

        }

        else if (messageObj instanceof ReconnectionGameData reconnectionGameData) {
            try {

                Sender sender = GameData.getSender();

                int levelGame = reconnectionGameData.getLevelGame();
                String gamePhase = reconnectionGameData.getGamePhase();
                int playerColor = reconnectionGameData.getPlayerColor();

                GameData.setActivePlayer(reconnectionGameData.getNameActivePlayer());

                GameData.setLevelGame(levelGame);
                GameData.setPhaseGame(gamePhase);
                GameData.setColor(playerColor);

                switch (gamePhase) {
                    case "BUILDING":
                        PageController.initBuilding(GameData.getLevelGame(), GameData.getColor());
                        PageController.switchScene("buildingPage.fxml", "Building");

                        sender.showHandComponent();
                        sender.showBookedComponents();
                        sender.showSpaceship(GameData.getNamePlayer());
                        sender.showPlayers();
                        sender.showVisibleComponents();
                        break;

                    case "ADJUSTING":
                        PageController.initAdjusting(GameData.getLevelGame());
                        PageController.switchScene("adjustingPage.fxml", "Adjusting");

                        sender.showSpaceship(GameData.getNamePlayer());
                        break;

                    case "POPULATING":
                        PageController.initPopulating(GameData.getLevelGame());
                        PageController.switchScene("populatingPage.fxml", "Populating");
                        break;

                    case "POSITIONING":
                        PageController.initPositioning(GameData.getLevelGame());
                        PageController.switchScene("positioningPage.fxml", "Positioning");

                        sender.showPlayersInPositioningDecisionOrder();
                        sender.showStartingPositions();
                        break;

                    case "EVENT":
                        PageController.initEvent(GameData.getLevelGame());
                        PageController.switchScene("gamePage.fxml", "Game");
                        break;
                }

            } catch (IOException e) {
                Alerts.showWarning("Error loading the page");
                System.out.println("Error loading the page");
            }
        }

        else if (messageObj instanceof PlayerColorMessage playerColorMessage) {
            GameData.setColor(playerColorMessage.getColor());
        }

        else if (messageObj instanceof WaitingPlayersMessage waitingPlayersMessage) {
            PageController.getWaitingRoomView().updatePlayersList(waitingPlayersMessage.getPlayers());
        }

        else if (messageObj instanceof AnotherPlayerIsReadyMessage anotherPlayerIsReadyMessage) {

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    PageController.getBuildingView().updateOtherPlayerReadyState(anotherPlayerIsReadyMessage.getNamePlayer());
                    break;
            }
        }

        else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());

            if(GameData.getPhaseGame().equalsIgnoreCase("WAITING"))
                PageController.getWaitingRoomView().disableReadyBtn(true);

            if(GameData.getPhaseGame().equalsIgnoreCase("INIT"))
                PageController.getWaitingRoomView().disableReadyBtn(false);

            else if(GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                try {
                    GameData.saveGameData();

                    PageController.initBuilding(GameData.getLevelGame(), GameData.getColor());
                    PageController.switchScene("buildingPage.fxml", "Building");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("ADJUSTING")) {

                try {
                    PageController.initAdjusting(GameData.getLevelGame());
                    PageController.switchScene("adjustingPage.fxml", "Adjusting");

                    GameData.getSender().showSpaceship(GameData.getNamePlayer());

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("POPULATING")) {

                try {
                    PageController.initPopulating(GameData.getLevelGame());
                    PageController.switchScene("populatingPage.fxml", "Populating");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("POSITIONING")) {

                try {
                    PageController.initPositioning(GameData.getLevelGame());
                    PageController.switchScene("positioningPage.fxml", "Positioning");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("EVENT")) {
                try {
                    PageController.initEvent(GameData.getLevelGame());
                    PageController.switchScene("gamePage.fxml", "Game");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    if (!responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer()))
                        PageController.getBuildingView().updateOtherPlayerSpaceship(responseSpaceshipMessage.getOwner(), responseSpaceshipMessage.getSpaceship());
                    else
                        PageController.getBuildingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    break;

                case "ADJUSTING":
                    PageController.getAdjustingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    break;

                case "POPULATING":
                    PageController.getPopulatingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    break;

                case "EVENT":
                    PageController.getEventView().showPlayerShip(responseSpaceshipMessage.getOwner());
                    break;
            }
        }

        else if (messageObj instanceof ShowHandComponentMessage showHandComponentMessage) {
            PageController.getBuildingView().generateHandComponent(showHandComponentMessage.getHandComponent());
        }

        else if (messageObj instanceof ShowBookedComponentsMessage pickedBookedComponentsMessage) {
            PageController.getBuildingView().updateBookedComponents(pickedBookedComponentsMessage.getBookedComponents());
        }

        else if(messageObj instanceof PlayersMessage playersMessage) {
            PageController.getBuildingView().updatePlayersList(playersMessage.getPlayers());
        }

        else if(messageObj instanceof PlayersInPositioningDecisionOrderMessage playersInPositioningDecisionOrderMessage) {
            PageController.getPositioningView().initPlayersList(playersInPositioningDecisionOrderMessage.getPlayers());
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            PageController.getBuildingView().generateHandComponent(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof ShowVisibleComponentsMessage pickedVisibleComponentsMessage) {
            PageController.getBuildingView().loadVisibleComponents(pickedVisibleComponentsMessage.getVisibleComponentDeck());
        }

        else if(messageObj instanceof PlayerLeftMessage playerLeftMessage) {
            System.out.println(playerLeftMessage.getPlayerName() + " left travel");
            GameData.getSender().showPlayers();
        }

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            GameData.getSender().showSpaceship(anotherPlayerPlacedComponentMessage.getNamePlayer());
        }

        else if (messageObj instanceof AnotherPlayerDiscardComponentMessage) {
            GameData.getSender().showVisibleComponents();
        }

        else if (messageObj instanceof AnotherPlayerPickedVisibleComponentMessage) {
            GameData.getSender().showVisibleComponents();
        }

        else if(messageObj instanceof PickedUpEventCardDeckMessage pickedUpEventCardDeckMessage) {
            PageController.getBuildingView().showEventDeck(pickedUpEventCardDeckMessage.getEventCardsDeck());
            PageController.getBuildingView().updateEventDecksAvailability(pickedUpEventCardDeckMessage.getDeckIdx());
            BuildingData.setCurrentDeckIdx(pickedUpEventCardDeckMessage.getDeckIdx());
        }

        else if(messageObj instanceof AnotherPlayerPickedUpEventCardDeck anotherPlayerPickedUpEventCardDeck) {
            PageController.getBuildingView().updateEventDecksAvailability(anotherPlayerPickedUpEventCardDeck.getDeckIdx());
        }

        else if(messageObj instanceof AnotherPlayerPutDownEventCardDeckMessage anotherPlayerPutDownEventCardDeckMessage) {
            PageController.getBuildingView().updateEventDecksAvailability(anotherPlayerPutDownEventCardDeckMessage.getDeckIdx());
        }

        else if (messageObj instanceof ActivePlayerMessage activePlayerMessage) {
            GameData.setActivePlayer(activePlayerMessage.getPlayerName());

            switch (GameData.getPhaseGame()) {

                case "POSITIONING":
                    PageController.getPositioningView().highlightsActivePlayer(activePlayerMessage.getPlayerName());
                    break;

                case "TRAVEL":
                    PageController.getTravelView().highlightsActivePlayer(activePlayerMessage.getPlayerName());
                    break;
            }
        }

        else if (messageObj instanceof AskStartingPositionMessage) {
            PageController.getPositioningView().updateLabels(true);
        }

        else if (messageObj instanceof StartingPositionsMessage startingPositionsMessage) {
            PageController.getPositioningView().updateTrack(startingPositionsMessage.getStartingPositions());
        }

        else if (messageObj instanceof AskAlienMessage askAlienMessage) {
            PageController.getPopulatingView().askForAlien(askAlienMessage.getColor(), askAlienMessage.getSpaceship());
        }

        else if (messageObj instanceof TrackMessage trackMessage) {
            PageController.getTravelView().updateTrack(trackMessage.getTrack());
            PageController.getTravelView().initPlayersList(trackMessage.getTravelers());
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            GameData.setActiveCard(pickedEventCardMessage.getEventCard());

            PageController.getEventView().initEventCard(pickedEventCardMessage.getEventCard().getImgSrc());
        }

        else if(messageObj instanceof HowManyDoubleCannonsMessage howManyDoubleCannonsMessage) {
           PageController.getEventView().responseHowManyDoubleCannons(
                   howManyDoubleCannonsMessage.getFirePowerRequired(),
                   howManyDoubleCannonsMessage.getMaxUsable(),
                   howManyDoubleCannonsMessage.getShootingPower(),
                   false
           );
        }

        else if(messageObj instanceof HowManyDoubleEnginesMessage howManyDoubleEnginesMessage) {
            PageController.getEventView().responseHowManyDoubleEngines(
                    howManyDoubleEnginesMessage.getMaxUsable(),
                    howManyDoubleEnginesMessage.getEnginePower(),
                    false
            );
        }

        else if(messageObj instanceof BatteriesToDiscardMessage batteriesToDiscardMessage) {
            PageController.getEventView().responseBatteryToDiscard(batteriesToDiscardMessage.getBatteriesToDiscard());
        }

        else if(messageObj instanceof CrewToDiscardMessage crewToDiscardMessage) {
            PageController.getEventView().responseCrewToDiscard(crewToDiscardMessage.getCrewToDiscard());
        }

        else if(messageObj instanceof BoxToDiscardMessage boxToDiscardMessage) {
            PageController.getEventView().responseBoxToDiscard(boxToDiscardMessage.getBoxToDiscard());
        }

        else if(messageObj instanceof AcceptRewardCreditsAndPenaltiesMessage acceptRewardCreditsAndPenaltiesMessage) {
            PageController.getEventView().responseAcceptRewardCreditsAndPenalties(
                    acceptRewardCreditsAndPenaltiesMessage.getRewardCredits(),
                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyDays(),
                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyCrew(),
                    false
            );
        }

        else if(messageObj instanceof AcceptRewardCreditsAndPenaltyDaysMessage acceptRewardCreditsAndPenaltyDaysMessage) {
            PageController.getEventView().responseAcceptRewardCreditsAndPenaltyDays(
                    acceptRewardCreditsAndPenaltyDaysMessage.getRewardCredits(),
                    acceptRewardCreditsAndPenaltyDaysMessage.getPenaltyDays(),
                    false
            );
        }

        else if(messageObj instanceof AcceptRewardBoxesAndPenaltyDaysMessage acceptRewardBoxesAndPenaltyDaysMessage) {
            PageController.getEventView().responseAcceptRewardBoxesAndPenaltyDays(
                    acceptRewardBoxesAndPenaltyDaysMessage.getRewardBoxes(),
                    acceptRewardBoxesAndPenaltyDaysMessage.getPenaltyDays(),
                    false
            );
        }

        else if(messageObj instanceof AvailableBoxesMessage availableBoxesMessage) {
            PageController.getEventView().renderBoxes(availableBoxesMessage.getBoxes());
            PageController.getEventView().responseRewardBox(availableBoxesMessage.getBoxes());
        }

        else if(messageObj instanceof AvailablePlanetsMessage availablePlanetsMessage) {
           PageController.getEventView().responsePlanetLandRequest(
                   availablePlanetsMessage.getRewardsForPlanets(),
                   availablePlanetsMessage.getPlanetsTaken(),
                   false
           );
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getBuildingView().updateTimer(timer);
        }

        else if (messageObj instanceof DestroyedComponentMessage) {
            GameData.getSender().showSpaceship(GameData.getNamePlayer());
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Current card: " + pickedEventCardMessage.getImgSrc());
        }

        else if (messageObj instanceof String messageString) {

            switch (messageString) {

                case "FailedToReconnect":
                    GameData.clearSaveFile();
                    try {
                        PageController.switchScene("chooseGame.fxml", "ChooseGame");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    GameData.getSender().updateGameList();
                    break;

                case "UpdateGameList":
                    GameData.getSender().updateGameList();
                    break;

                case "NotValidGameId":
                    Alerts.showError("That game does not exist", false);
                    break;

                case "NotAvailableName":
                    Alerts.showError("Username already taken for this game", false);
                    break;

                case "AllowedToPlaceComponent":
                    BuildingData.resetHandComponent();
                    break;

                case "NotAllowedToPlaceComponent":
                    if(BuildingData.getIsTimerExpired())
                        PageController.getBuildingView().removeHandComponent();
                    Alerts.showError("You are not allowed to place this component", false);
                    break;

                case "ComponentBooked":
                    DragAndDrop.setOnMousePressedForBookedComponent(BuildingData.getHandComponent());
                    BuildingData.resetHandComponent();
                    break;

                case "HandComponentDiscarded":
                    PageController.getBuildingView().removeHandComponent();
                    break;

                case "HasBeenBooked":
                    System.out.println("You cannot discard a booked component");
                    Alerts.showError("You cannot discard a booked component", false);
                    break;

                case "PickedBookedComponent":
                    BuildingData.setNewHandComponent(BuildingData.getTempPickingBookedComponent());
                    BuildingData.setXHandComponent(BuildingData.getTempXPickingBooked());
                    BuildingData.setYHandComponent(-1);
                    break;

                case "RequirePlacedComponent":
                    Alerts.showError("Its required to place a component before picking up a deck!", true);
                    break;

                case "EventCardDeckPutDown":
                    PageController.getBuildingView().hideEventDeck();
                    PageController.getBuildingView().updateEventDecksAvailability(BuildingData.getCurrentDeckIdx());
                    BuildingData.setCurrentDeckIdx(-1);
                    break;

                case "ImpossibleToResetTimer":
                    Alerts.showError("Impossible to reset timer!", true);
                    break;

                case "FinalResetNotAllowed":
                    Alerts.showError("Final reset not allowed: player not ready!", true);
                    break;

                case "TimerExpired":
                    Alerts.showError("Timer expired!", false);
                    BuildingData.setIsTimerExpired(true);
                    PageController.getBuildingView().disableDraggableBookedComponents();
                    PageController.getBuildingView().placeLastComponent();
                    break;

                case "YouAreReady":
                    System.out.println("You are ready");

                    if (GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                        PageController.getBuildingView().setReadyButtonDisabled();
                    }
                    break;

                case "ActionNotAllowedInReadyState":
                    Alerts.showError("Action not allowed in ready state!", true);
                    break;

                case "StartingPositionAlreadyTaken":
                    Alerts.showError("Position already taken!", true);
                    break;

                case "ValidStartingPosition":
                    PageController.getPositioningView().updateLabels(false);
                    break;

                case "InvalidStartingPosition":
                    Alerts.showError("Invalid starting position!", true);
                    break;

                case "PlayerAlreadyHasAStartingPosition":
                    Alerts.showError("You already have a starting position!", true);
                    break;

                case "ComponentAlreadyOccupied":
                    Alerts.showError("Component already occupied!", true);
                    break;

                case "CannotContainOrangeAlien":
                    Alerts.showError("Cannot contain orange alien!", true);
                    break;

                case "CannotContainPurpleAlien":
                    Alerts.showError("Cannot contain purple alien!", true);
                    break;

                case "PopulatingComplete":
                    PageController.getPopulatingView().clearBtnContainer();
                    PageController.getPopulatingView().updateLabels();
                    GameData.getSender().showSpaceship(GameData.getNamePlayer());
                    break;

                case "NotEnoughBatteries":
                    Alerts.showWarning("Not enough batteries!");
                    break;

                case "AskToUseShield":
                    PageController.getEventView().responseChooseToUseShield(false);
                    break;

                case "LandRequest":
                    PageController.getEventView().responseLandRequest(false);
                    break;

                default:
                    System.out.println(messageString);
                    break;
            }
        }

        else
            System.out.println("A message was received but is not handled: " + messageObj.toString());
    }
}