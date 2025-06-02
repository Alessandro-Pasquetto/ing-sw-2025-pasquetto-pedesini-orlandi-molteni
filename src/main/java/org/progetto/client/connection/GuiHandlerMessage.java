package org.progetto.client.connection;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.progetto.client.gui.Alerts;
import org.progetto.client.gui.DragAndDrop;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Battlezone.AnotherPlayerGotPenalizedMessage;
import org.progetto.messages.toClient.Battlezone.EvaluatingConditionMessage;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.Epidemic.CrewInfectedAmountMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateOtherTravelersShipMessage;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.messages.toClient.Stardust.ExposedConnectorsMessage;
import org.progetto.messages.toClient.Track.UpdateTrackMessage;
import org.progetto.messages.toClient.Travel.PlayerIsContinuingMessage;
import org.progetto.messages.toClient.Travel.PlayerLeftMessage;
import org.progetto.messages.toClient.WaitingGameInfoMessage;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.Projectile;
import org.progetto.server.model.events.ProjectileSize;

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
                        break;

                    case "EVENT":
                        PageController.initEvent(GameData.getLevelGame());
                        PageController.switchScene("eventPage.fxml", "Event");
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
                    PageController.switchScene("eventPage.fxml", "Event");
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

            if (updateSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer())){
                GameData.setSpaceship(updateSpaceshipMessage.getSpaceship());
                GameData.setCredits(updateSpaceshipMessage.getOwner().getCredits());
                PageController.getEventView().updateSpaceship(updateSpaceshipMessage.getSpaceship());
            }
            else {
                GameData.getOtherSpaceships().put(updateSpaceshipMessage.getOwner().getName(), updateSpaceshipMessage.getSpaceship());
                PageController.getEventView().updateOtherPlayerSpaceship(updateSpaceshipMessage.getOwner().getName(), updateSpaceshipMessage.getSpaceship());
            }
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

        else if (messageObj instanceof UpdateOtherTravelersShipMessage updateOtherTravelersShipMessage) {

            ArrayList<Player> travelers = updateOtherTravelersShipMessage.getTravelers();
            travelers.removeIf(player -> player.getName().equals(GameData.getNamePlayer()));

            switch (GameData.getPhaseGame()) {
                case "BUILDING":
                    PageController.getBuildingView().initPlayersSpaceshipList(updateOtherTravelersShipMessage.getTravelers());
                    break;

                case "EVENT":
                    PageController.getEventView().initTravelersSpaceshipList(updateOtherTravelersShipMessage.getTravelers());

                    Map<String, Spaceship> otherSpaceships = new HashMap<>();
                    for (Player player : updateOtherTravelersShipMessage.getTravelers()) {
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

        else if (messageObj instanceof PlayerMovedAheadMessage playerMovedAheadMessage) {
            int steps = playerMovedAheadMessage.getStepsCount();
            GameData.movePlayerByDistance(GameData.getNamePlayer(), steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());

            PageController.getEventView().addChatMessage("You have moved ahead of " + playerMovedAheadMessage.getStepsCount() + " positions", "INFO");
        }

        else if (messageObj instanceof PlayerMovedBackwardMessage playerMovedBackwardMessage) {
            int steps = playerMovedBackwardMessage.getStepsCount();
            GameData.movePlayerByDistance(GameData.getNamePlayer(), steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());

            PageController.getEventView().addChatMessage("You have moved backward of " + playerMovedBackwardMessage.getStepsCount() + " positions", "INFO");
        }

        else if (messageObj instanceof AnotherPlayerMovedAheadMessage anotherPlayerMovedAheadMessage) {
            String playerName = anotherPlayerMovedAheadMessage.getNamePlayer();
            int steps = anotherPlayerMovedAheadMessage.getStepsCount();
            GameData.movePlayerByDistance(playerName, steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());

            PageController.getEventView().addChatMessage(anotherPlayerMovedAheadMessage.getNamePlayer() + " has moved ahead of " + anotherPlayerMovedAheadMessage.getStepsCount() + "positions", "INFO");
        }

        else if (messageObj instanceof AnotherPlayerMovedBackwardMessage anotherPlayerMovedBackwardMessage) {
            String playerName = anotherPlayerMovedBackwardMessage.getNamePlayer();
            int steps = anotherPlayerMovedBackwardMessage.getStepsCount();
            GameData.movePlayerByDistance(playerName, steps);

            PageController.getEventView().updateMiniTrack(GameData.getTrack());

            PageController.getEventView().addChatMessage(anotherPlayerMovedBackwardMessage.getNamePlayer() + " has moved backward of " + anotherPlayerMovedBackwardMessage.getStepsCount() + " positions", "INFO");
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

        else if (messageObj instanceof HowManyDoubleCannonsMessage howManyDoubleCannonsMessage) {
            PageController.getEventView().askForQuantity(
                    "DoubleCannons",
                    "How many double cannons do you want to use?",
                    "Select number of double cannons to use, you have " + howManyDoubleCannonsMessage.getMaxUsable() + " double cannons available...",
                    howManyDoubleCannonsMessage.getMaxUsable(),
                    count -> GameData.getSender().responseHowManyDoubleCannons(count)
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

        else if (messageObj instanceof AcceptRewardCreditsAndPenaltyDaysMessage acceptRewardCreditsAndPenaltyDaysMessage) {
            PageController.getEventView().askYesNo(
                    "DO YOU WANT TO ACCEPT CREDITS?",
                    "You will get " + acceptRewardCreditsAndPenaltyDaysMessage.getRewardCredits() + " credits, but you will lose " + acceptRewardCreditsAndPenaltyDaysMessage.getPenaltyDays() + " days...",
                    response -> {
                        Sender sender = GameData.getSender();
                        sender.responseAcceptRewardCreditsAndPenaltyDays(response ? "YES" : "NO");
                    }
            );
        }

        else if (messageObj instanceof PlayerGetsCreditsMessage playerGetsCreditsMessage) {
            int credits = playerGetsCreditsMessage.getCredits();
            GameData.setCredits(GameData.getCredits() + credits);
            PageController.getEventView().updateSpaceship(GameData.getSpaceship());

            PageController.getEventView().addChatMessage("You received " + playerGetsCreditsMessage.getCredits() + " credits", "INFO");
        }

        else if(messageObj instanceof AnotherPlayerGetsCreditsMessage anotherplayerGetsCreditsMessage) {
            PageController.getEventView().addChatMessage(anotherplayerGetsCreditsMessage.getNamePlayer() + " received " + anotherplayerGetsCreditsMessage.getCredits() + " credits", "INFO");
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

            if (hu.getHasOrangeAlien()) spaceship.addNormalEnginePower(-2);
            if (hu.getHasPurpleAlien()) spaceship.addNormalShootingPower(-2);

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

        else if (messageObj instanceof BoxToDiscardMessage boxToDiscardMessage) {
            PageController.getEventView().askToSelectBoxToDiscard(
                    "You need to discard " + boxToDiscardMessage.getBoxToDiscard() + " boxes",
                    "Select box to discard starting from the most valuable...",
                    (int[] params) -> GameData.getSender().responseBoxToDiscard(params[0], params[1], params[2])
            );
        }

        else if (messageObj instanceof BoxDiscardedMessage boxDiscardedMessage) {
            Spaceship spaceship = GameData.getSpaceship();
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            int xBox = boxDiscardedMessage.getXBoxStorage();
            int yBox = boxDiscardedMessage.getYBoxStorage();
            int idxBox = boxDiscardedMessage.getBoxIdx();

            BoxStorage bs = (BoxStorage) spaceshipMatrix[yBox][xBox];
            bs.removeBox(spaceship, idxBox);

            PageController.getEventView().updateSpaceship(spaceship);
        }

        else if (messageObj instanceof AnotherPlayerBoxDiscardedMessage anotherPlayerBoxDiscardedMessage) {
            Spaceship spaceship = GameData.getOtherSpaceships().get(anotherPlayerBoxDiscardedMessage.getPlayerName());
            Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

            int xBox = anotherPlayerBoxDiscardedMessage.getXBoxStorage();
            int yBox = anotherPlayerBoxDiscardedMessage.getYBoxStorage();
            int idxBox = anotherPlayerBoxDiscardedMessage.getBoxIdx();

            BoxStorage bs = (BoxStorage) spaceshipMatrix[yBox][xBox];
            bs.removeBox(spaceship, idxBox);

            PageController.getEventView().updateOtherPlayerSpaceship(anotherPlayerBoxDiscardedMessage.getPlayerName(), spaceship);
        }

        else if (messageObj instanceof EvaluatingConditionMessage evaluatingConditionMessage) {
            String condition = switch (evaluatingConditionMessage.getCondition()) {
                case "Crew" -> "less crew members";
                case "Engine" -> "fewer engine power";
                case "Cannon" -> "fewer shooting power";
                default -> "";
            };
            PageController.getEventView().addChatMessage("Finding player with " + condition, "INFO");
        }

        else if (messageObj instanceof AnotherPlayerGotPenalizedMessage anotherPlayerGotPenalizedMessage) {
            GameData.setActivePlayer(GameData.getNamePlayer());
            PageController.getEventView().updateActivePlayer(GameData.getNamePlayer());

            PageController.getEventView().setEventLabels("ANOTHER PLAYER GOT PENALIZED", "Wait for that player to finish his turn...");
            PageController.getEventView().addChatMessage(anotherPlayerGotPenalizedMessage.getPlayerName() + " got penalized for current condition", "INFO");
        }

        else if (messageObj instanceof IncomingProjectileMessage incomingProjectileMessage) {
            Projectile projectile = incomingProjectileMessage.getProjectile();

            String dimension = projectile.getSize() == ProjectileSize.BIG ? "BIG" : "SMALL";
            String from = switch (projectile.getFrom()) {
                case 0 -> "TOP";
                case 1 -> "RIGHT";
                case 2 -> "BOTTOM";
                case 3 -> "LEFT";
                default -> "UNKNOWN";
            };

            PageController.getEventView().setEventLabels("A " + dimension + " projectile is incoming from " + from + "!", "Wait for the first player to roll dice to decide where will it hit...");
        }

        else if (messageObj instanceof DiceResultMessage diceResultMessage) {
            PageController.getEventView().updateDiceResult(diceResultMessage.getDiceResult(), null);
        }

        else if (messageObj instanceof AnotherPlayerDiceResultMessage anotherPlayerDiceResultMessage) {
            PageController.getEventView().updateDiceResult(anotherPlayerDiceResultMessage.getDiceResult(), anotherPlayerDiceResultMessage.getNamePlayer());
        }

        else if (messageObj instanceof AffectedComponentMessage affectedComponentMessage) {
            Pane affectedComponent = PageController.getEventView().getCellFromSpaceshipMatrix(affectedComponentMessage.getXComponent(), affectedComponentMessage.getYComponent());
            PageController.getEventView().highlightCell(affectedComponent, Color.rgb(255, 0, 0, 0.3));
        }

        else if(messageObj instanceof AvailableBoxesMessage availableBoxesMessage) {
           PageController.getEventView().renderRewardBoxes(
                   "Select your reward boxes",
                   "Select the boxes you want to keep...",
                   availableBoxesMessage.getBoxes()
           );
           PageController.getEventView().enableDragAndDropBoxesSpaceship();
        }

        else if(messageObj instanceof AvailablePlanetsMessage availablePlanetsMessage) {
           PageController.getEventView().askPlanetSelection(
                     "Select a planet",
                     "Select a planet to land on...",
                     availablePlanetsMessage.getPlanetsTaken(),
                     (planet) -> GameData.getSender().responsePlanetLandRequest(planet)
           );
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getBuildingView().updateTimer(timer);
        }

        else if (messageObj instanceof DestroyedComponentMessage destroyedComponentMessage) {

            switch (GameData.getPhaseGame()) {

                case "ADJUSTING":
                    GameData.getSender().showSpaceship(GameData.getNamePlayer());
                    break;

                case "EVENT":
                    PageController.getEventView().setEventLabels("COMPONENT DESTROYED", "Wait for other players to finish their turn...");
                    break;
            }
        }

        else if (messageObj instanceof AnotherPlayerDestroyedComponentMessage anotherPlayerDestroyedComponentMessage) {

            if (GameData.getPhaseGame().equals("EVENT")) {
                PageController.getEventView().addChatMessage(anotherPlayerDestroyedComponentMessage.getNamePlayer() + " destroyed a component", "INFO");
            }
        }

        else if (messageObj instanceof CrewInfectedAmountMessage crewInfectedAmountMessage) {
            int infectedCount = crewInfectedAmountMessage.getInfectedCrew();

            if (infectedCount == 0) {
                PageController.getEventView().setEventLabels("NO CREW MEMBER GOT INFECTED", "You have no crew members infected, very lucky...");
            } else {
                PageController.getEventView().setEventLabels("CREW MEMBERS GOT INFECTED", "You have " + infectedCount + " crew members infected, you lost them...");
            }
        }

        else if (messageObj instanceof ExposedConnectorsMessage exposedConnectorsMessage) {
            int exposedConnectorsCount = exposedConnectorsMessage.getExposedConnectorsCount();

            if (exposedConnectorsCount == 0) {
                PageController.getEventView().setEventLabels("YOU HAVE NO EXPOSED CONNECTOR", "You have no exposed connectors, your ship is well built...");
            } else {
                PageController.getEventView().setEventLabels("YOU HAVE SOME EXPOSED CONNECTORS", "You have " + exposedConnectorsCount + " exposed connectors, so you will move back by the same number of positions...");
            }
        }

        else if (messageObj instanceof PlayerIsContinuingMessage playerIsContinuingMessage) {
            PageController.getTravelView().setPlayerStatus(playerIsContinuingMessage.getPlayerName(), false);

            PageController.getEventView().addChatMessage(playerIsContinuingMessage.getPlayerName() + " is continuing travel", "INFO");
        }

        else if (messageObj instanceof PlayerLeftMessage playerLeftMessage) {
            PageController.getTravelView().setPlayerStatus(playerLeftMessage.getPlayerName(), true);

            PageController.getEventView().addChatMessage(playerLeftMessage.getPlayerName() + " left travel", "INFO");
        }

        else if(messageObj instanceof AnotherPlayerWonBattleMessage anotherPlayerWonBattleMessage) {
            PageController.getEventView().addChatMessage(anotherPlayerWonBattleMessage.getPlayerName() + " won battle", "INFO");
        }

        else if(messageObj instanceof AnotherPlayerLostBattleMessage anotherPlayerLostBattleMessage) {
            PageController.getEventView().addChatMessage(anotherPlayerLostBattleMessage.getPlayerName() + " lost battle", "INFO");
        }

        else if(messageObj instanceof AnotherPlayerDrewBattleMessage anotherPlayerDrewBattleMessage){
            PageController.getEventView().addChatMessage(anotherPlayerDrewBattleMessage.getPlayerName() + " drew battle", "INFO");
        }

        else if (messageObj instanceof PlayerDefeatedMessage playerDefeatedMessage) {
            PageController.getEventView().addChatMessage(playerDefeatedMessage.getPlayerName() + " was defeated!", "INFO");
        }

        else if (messageObj instanceof ScoreBoardMessage scoreBoardMessage) {
            PageController.getGameOverView().initScoreboard(scoreBoardMessage.getScoreBoard());
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
                    Alerts.showError("Impossible to flip timer!", true);
                    break;

                case "FinalResetNotAllowed":
                    Alerts.showError("Final flip not allowed, player not ready!", true);
                    break;

                case "TimerFlipped":
                    Alerts.showError("Timer flipped!", false);
                    BuildingData.setFlipsRemaining(BuildingData.getFlipsRemaining() - 1);
                    PageController.getBuildingView().updateTimerFlips(BuildingData.getFlipsRemaining());
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
                        PageController.getBuildingView().updateBuildingTitle("YOU ARE READY");
                        PageController.getBuildingView().setReadyButtonDisabled();
                    }
                    break;

                case "ComponentsNotConnectedGotRemoved":
                    Alerts.showError("Some components not connected got removed!", true);
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

                case "YouArePenalizedPlayer":
                    GameData.setActivePlayer(GameData.getNamePlayer());
                    PageController.getEventView().updateActivePlayer(GameData.getNamePlayer());

                    PageController.getEventView().setEventLabels("YOU ARE PENALIZED", "You are penalized for current condition...");
                    PageController.getEventView().addChatMessage("You are penalized for current condition", "INFO");
                    break;

                case "BoxNotDiscarded":
                    Alerts.showError("You cannot discard this box!", true);
                    break;

                case "ResetActivePlayer":
                    GameData.setActivePlayer("");
                    PageController.getEventView().updateActivePlayer("");
                    break;

                case "NotEnoughBatteries":
                    Alerts.showWarning("Not enough batteries!");
                    break;

                case "AskToUseShield":
                    PageController.getEventView().askYesNo(
                            "DO YOU WANT TO USE SHIELD?",
                            "You can use shield to avoid damage, select your choice...",
                            response -> {
                                Sender sender = GameData.getSender();
                                sender.responseChooseToUseShield(response ? "YES" : "NO");
                            }
                    );
                    break;

                case "NoShieldAvailable":
                    PageController.getEventView().setEventLabels("NO SHIELD AVAILABLE", "You cannot use shield, wait for other players to finish their turn...");
                    break;

                case "AskToUseDoubleCannon":
                    PageController.getEventView().askYesNo(
                            "DO YOU WANT TO USE DOUBLE CANNON?",
                            "You can use double cannon to avoid damage, select your choice...",
                            response -> {
                                Sender sender = GameData.getSender();
                                sender.responseUseDoubleCannonRequest(response ? "YES" : "NO");
                            }
                    );
                    break;

                case "LandingCompleted":
                    break;

                case "BoxChosen":
                    PageController.getEventView().disableDragAndDropBoxesSpaceship();
                    break;

                case "BoxRemoved":
                    GameData.getSender().showSpaceship(GameData.getNamePlayer());
                    break;


                case "EmptyReward":
                    PageController.getEventView().setEventLabels("THE REWARD IS EMPTY", "Wait for other players to finish their turn...");
                    break;

                case "PlanetLeft":
                    PageController.getEventView().setEventLabels("YOU LEFT THE PLANET", "Wait for other players to finish their turn...");
                    break;

                case "RollDiceToFindColumn":
                    PageController.getEventView().askToRollDice(
                            "Roll dice",
                            "Find impact column rolling the dice..."
                    );
                    break;

                case "RollDiceToFindRow":
                    PageController.getEventView().askToRollDice(
                            "Roll dice",
                            "Find impact row rolling the dice..."
                    );
                    break;

                case "NoComponentHit":
                    PageController.getEventView().setEventLabels("NO COMPONENT HIT", "Wait for other players to finish their turn...");
                    break;

                case "NoComponentDamaged":
                    PageController.getEventView().setEventLabels("NO COMPONENT DAMAGED", "Wait for other players to finish their turn...");
                    break;

                case "NothingGotDestroyed":
                    PageController.getEventView().setEventLabels("NOTHING GOT DESTROYED", "Wait for other players to finish their turn...");
                    break;

                case "MeteorDestroyed":
                    PageController.getEventView().setEventLabels("METEOR DESTROYED", "You destroyed the meteor, wait for other players to finish their turn...");
                    break;

                case "AskSelectSpaceshipPart":
                    PageController.getEventView().askToSelectShipComponent(
                            "Select the spaceship part to keep",
                            "Select spaceship part to maintain clicking on a component...",
                            "ANY",
                            (x, y) -> GameData.getSender().responseSelectSpaceshipPart(x, y)
                    );
                    break;

                case "YouAreSafe":
                    PageController.getEventView().setEventLabels("YOU ARE SAFE", "Wait for other players to finish their turn...");
                    break;

                case "YouAnsweredYes":
                    PageController.getEventView().setEventLabels("YOU ANSWERED YES", "Wait for other players to finish their turn...");
                    break;

                case "YouAnsweredNo":
                    PageController.getEventView().setEventLabels("YOU ANSWERED NO", "Wait for other players to finish their turn...");
                    break;

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

                case "YouGotLapped":
                    PageController.getEventView().setEventLabels("YOU GOT LAPPED", "You cannot continue the travel...");
                    break;

                case "YouHaveNoCrew":
                    PageController.getEventView().setEventLabels("YOU HAVE NO CREW", "You cannot continue the travel...");
                    break;

                case "YouLostBattle":
                    PageController.getEventView().setEventLabels("YOU LOST THE BATTLE", "You lost the battle, wait for other players to finish their turn...");
                    PageController.getEventView().addChatMessage("You lost the battle", "INFO");
                    break;

                case "YouWonBattle":
                    PageController.getEventView().setEventLabels("YOU WON THE BATTLE", "You won the battle, wait for other players to finish their turn...");
                    PageController.getEventView().addChatMessage("You won the battle", "INFO");
                    break;

                case "YouDrewBattle":
                    PageController.getEventView().setEventLabels("YOU DREW THE BATTLE", "You drew the battle, wait for other players to finish their turn...");
                    PageController.getEventView().addChatMessage("You drew the battle", "INFO");
                    break;

                case "YouLost":
                    PageController.getGameOverView().initGameOver(1);
                    break;

                case "YouWon":
                    PageController.getGameOverView().initGameOver(0);
                    break;

                case "EventCardSkipped":
                    PageController.getEventView().setEventLabels("EVENT CARD GOT SKIPPED", "There aren’t enough travelers for that event...");
                    break;

                case "EventCardEnded":
                    PageController.getEventView().setEventLabels("THE EVENT IS ENDED", "Get ready for the travel...");
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