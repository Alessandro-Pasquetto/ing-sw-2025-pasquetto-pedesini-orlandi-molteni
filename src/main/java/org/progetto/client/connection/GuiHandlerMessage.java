package org.progetto.client.connection;

import org.progetto.client.gui.Alerts;
import org.progetto.client.gui.DragAndDrop;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.messages.toClient.Travel.PlayerIsContinuingMessage;
import org.progetto.messages.toClient.Travel.PlayerLeftMessage;
import org.progetto.messages.toClient.WaitingGameInfoMessage;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                        PageController.switchScene("newEventPage.fxml", "Event");
                        break;

                    case "TRAVEL":
                        PageController.initTravel(GameData.getLevelGame());
                        PageController.switchScene("travelPage.fxml", "Travel");

                        GameData.getSender().showTrack();
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

            Sender sender = GameData.getSender();

            try{
                if(GameData.getPhaseGame().equalsIgnoreCase("WAITING"))
                    PageController.getWaitingRoomView().disableReadyBtn(true);

                if(GameData.getPhaseGame().equalsIgnoreCase("INIT"))
                    PageController.getWaitingRoomView().disableReadyBtn(false);

                else if(GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                    GameData.saveGameData();

                    PageController.initBuilding(GameData.getLevelGame(), GameData.getColor());
                    PageController.switchScene("buildingPage.fxml", "Building");
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("ADJUSTING")) {
                    PageController.initAdjusting(GameData.getLevelGame());
                    PageController.switchScene("adjustingPage.fxml", "Adjusting");

                    sender.showSpaceship(GameData.getNamePlayer());
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("POPULATING")) {
                    PageController.initPopulating(GameData.getLevelGame());
                    PageController.switchScene("populatingPage.fxml", "Populating");
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("POSITIONING")) {
                    PageController.initPositioning(GameData.getLevelGame());
                    PageController.switchScene("positioningPage.fxml", "Positioning");
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("EVENT")) {
                    PageController.initEvent(GameData.getLevelGame());
                    PageController.switchScene("newEventPage.fxml", "Event");
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("TRAVEL")){
                    PageController.initTravel(GameData.getLevelGame());
                    PageController.switchScene("travelPage.fxml", "Travel");
                }

                else if(GameData.getPhaseGame().equalsIgnoreCase("ENDGAME")){
                    PageController.initEndGame(GameData.getLevelGame());
                    PageController.switchScene("gameOverPage.fxml","EndGame");
                }


            } catch (IOException e) {
                Alerts.showWarning("Error loading the page");
                System.err.println("Error loading the page");
            }
        }

        else if (messageObj instanceof UpdateSpaceshipMessage updateSpaceshipMessage) {
            GameData.setSpaceship(updateSpaceshipMessage.getSpaceship());
            GameData.setCredits(updateSpaceshipMessage.getOwner().getCredits());
            PageController.getEventView().updateSpaceship(updateSpaceshipMessage.getSpaceship());
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    if (responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer()))
                        PageController.getBuildingView().updateSpaceship(responseSpaceshipMessage.getSpaceship()); // This is used only for reconnection
                    else
                        PageController.getBuildingView().updateOtherPlayerSpaceship(responseSpaceshipMessage.getOwner(), responseSpaceshipMessage.getSpaceship()); // This is used only for reconnection and getCentralUnit
                    break;

                case "ADJUSTING":
                    if(responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer()))
                        PageController.getAdjustingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    break;

                case "POPULATING":
                    PageController.getPopulatingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    break;

                case "EVENT":
                    if (responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer())){
                        GameData.setSpaceship(responseSpaceshipMessage.getSpaceship());
                        GameData.setCredits(responseSpaceshipMessage.getOwner().getCredits());
                        PageController.getEventView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
                    }
                    else
                        PageController.getEventView().updateOtherPlayerSpaceship(responseSpaceshipMessage.getOwner().getName(), responseSpaceshipMessage.getSpaceship());
                    break;
            }
        }

        else if (messageObj instanceof ShowHandComponentMessage showHandComponentMessage) {
            PageController.getBuildingView().generateHandComponent(showHandComponentMessage.getHandComponent());
        }

        else if (messageObj instanceof ShowBookedComponentsMessage pickedBookedComponentsMessage) {
            PageController.getBuildingView().updateBookedComponents(pickedBookedComponentsMessage.getBookedComponents());
        }

        else if(messageObj instanceof ResponsePlayersMessage playersMessage) {

            ArrayList<Player> players = playersMessage.getPlayers();
            players.removeIf(player -> player.getName().equals(GameData.getNamePlayer()));

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    PageController.getBuildingView().initPlayersSpaceshipList(playersMessage.getPlayers());
                    break;

                //todo: serve? (nella gui non dovrebbe essere mai chiamato)
                case "EVENT":
                    PageController.getEventView().initTravelersSpaceshipList(playersMessage.getPlayers());

                    Map<String, Spaceship> otherSpaceships = new HashMap<>();
                    for (Player player : playersMessage.getPlayers()) {
                        otherSpaceships.put(player.getName(), player.getSpaceship());
                    }
                    GameData.setOtherSpaceships(otherSpaceships);
                    break;
            }
        }

        else if (messageObj instanceof UpdateTravelersMessage updateTravelersMessage) {

            ArrayList<Player> travelers = updateTravelersMessage.getTravelers();
            travelers.removeIf(player -> player.getName().equals(GameData.getNamePlayer()));

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    PageController.getBuildingView().initPlayersSpaceshipList(updateTravelersMessage.getTravelers());
                    break;

                case "EVENT":
                    PageController.getEventView().initTravelersSpaceshipList(updateTravelersMessage.getTravelers());

                    Map<String, Spaceship> otherSpaceships = new HashMap<>();
                    for (Player player : updateTravelersMessage.getTravelers()) {
                        otherSpaceships.put(player.getName(), player.getSpaceship());
                    }
                    GameData.setOtherSpaceships(otherSpaceships);
                    break;
            }
        }

        else if (messageObj instanceof UpdateTrackMessage updateTrackMessage) {
            GameData.setTrack(updateTrackMessage.getTrack());

            switch (GameData.getPhaseGame()) {

                case "EVENT":
                    PageController.getEventView().updateMiniTrack(updateTrackMessage.getTrack());
                    break;

                case "TRAVEL":
                    PageController.getTravelView().updateTrack(updateTrackMessage.getTrack());
                    PageController.getTravelView().updatePlayersInTrackList(updateTrackMessage.getPlayersInTrack());
                    break;
            }
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

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            String playerName = anotherPlayerPlacedComponentMessage.getNamePlayer();
            Component component = anotherPlayerPlacedComponentMessage.getComponent();
            int x = anotherPlayerPlacedComponentMessage.getX();
            int y = anotherPlayerPlacedComponentMessage.getY();

            PageController.getBuildingView().updateOtherPlayerPlacedComponent(playerName, component, x, y);
        }

        else if (messageObj instanceof AnotherPlayerBookedComponentMessage anotherPlayerBookedComponentMessage) {
            String playerName = anotherPlayerBookedComponentMessage.getPlayerName();
            Component component = anotherPlayerBookedComponentMessage.getComponent();
            int idx = anotherPlayerBookedComponentMessage.getIdx();

            PageController.getBuildingView().updateOtherPlayerBookedComponent(playerName, component, idx);
        }

        else if (messageObj instanceof AnotherPlayerPickedBookedComponentMessage anotherPlayerPickedBookedComponentMessage) {
            String playerName = anotherPlayerPickedBookedComponentMessage.getPlayerName();
            int idx = anotherPlayerPickedBookedComponentMessage.getIdx();

            PageController.getBuildingView().updateOtherPlayerBookedComponent(playerName, null, idx);
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

                case "EVENT":
                    PageController.getEventView().updateActivePlayer(activePlayerMessage.getPlayerName());
                    PageController.getEventView().addChatMessage(activePlayerMessage.getPlayerName() + " is the active player now", "INFO");
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

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            GameData.setActiveCard(pickedEventCardMessage.getEventCard());
            PageController.getEventView().initEventCard(pickedEventCardMessage.getEventCard());
        }

//        else if(messageObj instanceof HowManyDoubleCannonsMessage howManyDoubleCannonsMessage) {
//           PageController.getEventView().responseHowManyDoubleCannons(
//                   howManyDoubleCannonsMessage.getFirePowerRequired(),
//                   howManyDoubleCannonsMessage.getMaxUsable(),
//                   howManyDoubleCannonsMessage.getShootingPower(),
//                   false
//           );
//        }

        else if (messageObj instanceof PlayerMovedAheadMessage playerMovedAheadMessage) {
            int steps = playerMovedAheadMessage.getStepsCount();
            GameData.movePlayerByDistance(GameData.getNamePlayer(), steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());
        }

        else if (messageObj instanceof PlayerMovedBackwardMessage playerMovedBackwardMessage) {
            int steps = playerMovedBackwardMessage.getStepsCount();
            GameData.movePlayerByDistance(GameData.getNamePlayer(), steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());
        }

        else if (messageObj instanceof AnotherPlayerMovedAheadMessage anotherPlayerMovedAheadMessage) {
            String playerName = anotherPlayerMovedAheadMessage.getNamePlayer();
            int steps = anotherPlayerMovedAheadMessage.getStepsCount();
            GameData.movePlayerByDistance(playerName, steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());
        }

        else if (messageObj instanceof AnotherPlayerMovedBackwardMessage anotherPlayerMovedBackwardMessage) {
            String playerName = anotherPlayerMovedBackwardMessage.getNamePlayer();
            int steps = anotherPlayerMovedBackwardMessage.getStepsCount();
            GameData.movePlayerByDistance(playerName, steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());
        }

        else if(messageObj instanceof HowManyDoubleEnginesMessage howManyDoubleEnginesMessage) {
            PageController.getEventView().askForQuantity(
                    "DoubleEngines",
                    "How many double engines do you want to use?",
                    "Select number of double engines to use, you have " + howManyDoubleEnginesMessage.getMaxUsable() + " double engines available...",
                    howManyDoubleEnginesMessage.getMaxUsable(),
                    count -> GameData.getSender().responseHowManyDoubleEngines(count)
            );
        }

        else if (messageObj instanceof AcceptRewardCreditsAndPenaltiesMessage acceptRewardCreditsAndPenaltiesMessage) {
            PageController.getEventView().askYesNo(
                    "DO YOU WANT TO ACCEPT CREDITS?",
                    "You will get " + acceptRewardCreditsAndPenaltiesMessage.getRewardCredits() + " credits, but you will lose " + acceptRewardCreditsAndPenaltiesMessage.getPenaltyDays() + " days and " + acceptRewardCreditsAndPenaltiesMessage.getPenaltyCrew() + " crew members...",
                    response -> {
                        Sender sender = GameData.getSender();
                        sender.responseAcceptRewardCreditsAndPenalties(response ? "YES" : "NO");
                    }
            );
        }

        else if (messageObj instanceof PlayerGetsCreditsMessage playerGetsCreditsMessage) {
            int credits = playerGetsCreditsMessage.getCredits();
            GameData.setCredits(GameData.getCredits() + credits);
            PageController.getEventView().updateSpaceship(GameData.getSpaceship());
        }

        else if (messageObj instanceof BatteriesToDiscardMessage batteriesToDiscardMessage) {
            PageController.getEventView().askToSelectShipComponent(
                    "You need to discard " + batteriesToDiscardMessage.getBatteriesToDiscard() + " batteries",
                    "Select battery to discard...",
                    "BATTERY",
                    (x, y) -> GameData.getSender().responseBatteryToDiscard(x, y)
            );
        }

        else if (messageObj instanceof BatteryDiscardedMessage batteryDiscardedMessage) {
            Spaceship spaceship = GameData.getSpaceship();
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            BatteryStorage bs = (BatteryStorage) spaceshipMatrix[batteryDiscardedMessage.getYBatteryStorage()][batteryDiscardedMessage.getXBatteryStorage()];
            bs.decrementItemsCount(spaceship, 1);

            PageController.getEventView().updateSpaceship(spaceship);
        }

        else if (messageObj instanceof AnotherPlayerBatteryDiscardedMessage anotherPlayerBatteryDiscardedMessage) {
            Spaceship spaceship = GameData.getOtherSpaceships().get(anotherPlayerBatteryDiscardedMessage.getNamePlayer());
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            BatteryStorage bs = (BatteryStorage) spaceshipMatrix[anotherPlayerBatteryDiscardedMessage.getYBatteryStorage()][anotherPlayerBatteryDiscardedMessage.getXBatteryStorage()];
            bs.decrementItemsCount(spaceship, 1);

            PageController.getEventView().updateOtherPlayerSpaceship(anotherPlayerBatteryDiscardedMessage.getNamePlayer(), spaceship);
        }

        else if (messageObj instanceof CrewToDiscardMessage crewToDiscardMessage) {
            PageController.getEventView().askToSelectShipComponent(
                    "You need to discard " + crewToDiscardMessage.getCrewToDiscard() + " crew members",
                    "Select crew to discard...",
                    "CREW",
                    (x, y) -> GameData.getSender().responseCrewToDiscard(x, y)
            );
        }

        else if (messageObj instanceof CrewDiscardedMessage crewDiscardedMessage) {
            Spaceship spaceship = GameData.getSpaceship();
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            HousingUnit hu = (HousingUnit) spaceshipMatrix[crewDiscardedMessage.getYHousingUnit()][crewDiscardedMessage.getXHousingUnit()];
            hu.decrementCrewCount(spaceship, 1);
            hu.setAlienOrange(false);
            hu.setAlienPurple(false);

            PageController.getEventView().updateSpaceship(spaceship);
        }

        else if (messageObj instanceof AnotherPlayerCrewDiscardedMessage anotherPlayerCrewDiscardedMessage) {
            Spaceship spaceship = GameData.getOtherSpaceships().get(anotherPlayerCrewDiscardedMessage.getPlayerName());
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            HousingUnit hu = (HousingUnit) spaceshipMatrix[anotherPlayerCrewDiscardedMessage.getYHousingUnit()][anotherPlayerCrewDiscardedMessage.getXHousingUnit()];
            hu.decrementCrewCount(spaceship, 1);
            hu.setAlienOrange(false);
            hu.setAlienPurple(false);

            PageController.getEventView().updateOtherPlayerSpaceship(anotherPlayerCrewDiscardedMessage.getPlayerName(), spaceship);
        }

//        else if(messageObj instanceof CrewToDiscardMessage crewToDiscardMessage) {
//            PageController.getEventView().responseCrewToDiscard(crewToDiscardMessage.getCrewToDiscard());
//        }
//
//        else if(messageObj instanceof BoxToDiscardMessage boxToDiscardMessage) {
//            PageController.getEventView().responseBoxToDiscard(boxToDiscardMessage.getBoxToDiscard());
//        }
//
//        else if(messageObj instanceof AcceptRewardCreditsAndPenaltiesMessage acceptRewardCreditsAndPenaltiesMessage) {
//            PageController.getEventView().responseAcceptRewardCreditsAndPenalties(
//                    acceptRewardCreditsAndPenaltiesMessage.getRewardCredits(),
//                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyDays(),
//                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyCrew(),
//                    false
//            );
//        }
//
//        else if(messageObj instanceof AcceptRewardCreditsAndPenaltyDaysMessage acceptRewardCreditsAndPenaltyDaysMessage) {
//            PageController.getEventView().responseAcceptRewardCreditsAndPenaltyDays(
//                    acceptRewardCreditsAndPenaltyDaysMessage.getRewardCredits(),
//                    acceptRewardCreditsAndPenaltyDaysMessage.getPenaltyDays(),
//                    false
//            );
//        }
//
//        else if(messageObj instanceof AcceptRewardBoxesAndPenaltyDaysMessage acceptRewardBoxesAndPenaltyDaysMessage) {
//            PageController.getEventView().responseAcceptRewardBoxesAndPenaltyDays(
//                    acceptRewardBoxesAndPenaltyDaysMessage.getRewardBoxes(),
//                    acceptRewardBoxesAndPenaltyDaysMessage.getPenaltyDays(),
//                    false
//            );
//        }
//
//        else if(messageObj instanceof AvailableBoxesMessage availableBoxesMessage) {
//            PageController.getEventView().renderBoxes(availableBoxesMessage.getBoxes());
//            PageController.getEventView().responseRewardBox(availableBoxesMessage.getBoxes());
//        }
//
//        else if(messageObj instanceof AvailablePlanetsMessage availablePlanetsMessage) {
//           PageController.getEventView().responsePlanetLandRequest(
//                   availablePlanetsMessage.getRewardsForPlanets(),
//                   availablePlanetsMessage.getPlanetsTaken(),
//                   false
//           );
//        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getBuildingView().updateTimer(timer);
        }

        else if (messageObj instanceof DestroyedComponentMessage) {
            GameData.getSender().showSpaceship(GameData.getNamePlayer());
        }

        else if (messageObj instanceof PlayerIsContinuingMessage playerIsContinuingMessage) {
            PageController.getTravelView().setPlayerStatus(playerIsContinuingMessage.getPlayerName(), false);
        }

        else if (messageObj instanceof PlayerLeftMessage playerLeftMessage) {
            PageController.getTravelView().setPlayerStatus(playerLeftMessage.getPlayerName(), true);
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

//                case "AskToUseShield":
//                    PageController.getEventView().responseChooseToUseShield(false);
//                    break;
//
//                case "LandRequest":
//                    PageController.getEventView().responseLandRequest(false);
//                    break;

                case "AskContinueTravel":
                    PageController.getTravelView().askYesNo(
                            "DO YOU WANT TO CONTINUE THE TRAVEL?",
                            "Select your choice...",
                            response -> {
                                Sender sender = GameData.getSender();
                                sender.responseContinueTravel(response ? "YES" : "NO");
                            }
                    );
                    break;

                case "YouAreContinuingTravel":
                    PageController.getTravelView().setPlayerStatus(GameData.getNamePlayer(), false);
                    break;

                case "YouLeftTravel":
                    PageController.getTravelView().setPlayerStatus(GameData.getNamePlayer(), true);
                    GameData.setHasLeft(true);
                    break;

                case "NoEnginePower":
                    PageController.getEventView().setEventLabels("YOU HAVE NO ENGINE POWER", "You cannot continue the travel, wait for other players to finish their turn...");
                    GameData.setHasLeft(true);
                    break;

                case "YouLostBattle":
                    System.out.println("You lost");
                    break;

                case "YouWonBattle":
                    System.out.println("You win");
                    break;

                case "YouDrewBattle":
                    System.out.println("You drew");
                    break;

                case "YouLost":
                    PageController.getGameOverView().initGameOver(1);
                    break;

                case "YouWon":
                    PageController.getGameOverView().initGameOver(0);
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