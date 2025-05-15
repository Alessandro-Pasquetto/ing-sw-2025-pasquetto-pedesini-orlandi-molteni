package org.progetto.client.connection;

import javafx.scene.control.Alert;
import org.progetto.client.gui.Alerts;
import org.progetto.client.gui.DragAndDrop;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.client.tui.TuiPrinters;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventCommon.PlayerLeftMessage;
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

        if (messageObj instanceof ShowWaitingGamesMessage showWaitingGamesMessage) {
            ArrayList<WaitingGameInfoMessage> gamesInfo = showWaitingGamesMessage.getWaitingGames();
            PageController.getChooseGameView().generateGameRecordList(gamesInfo);
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {

            System.out.println("You joined a game");

            int gameId = initGameMessage.getIdGame();
            int levelGame = initGameMessage.getLevelGame();
            int numMaxPlayer = initGameMessage.getNumMaxPlayers();

            try {
                PageController.switchScene("waitingRoom.fxml", "WaitingRoom");
                PageController.getWaitingRoomView().init(gameId, levelGame, numMaxPlayer);

            } catch (IOException e) {
                Alerts.showWarning("Error loading the page");
                System.out.println("Error loading the page");
            }

            GameData.setIdGame(gameId);
            GameData.setLevelGame(levelGame);
        }

        else if (messageObj instanceof ReconnectionGameData reconnectionGameData) {
            try {

                int levelGame = reconnectionGameData.getLevelGame();
                String gamePhase = reconnectionGameData.getGamePhase();
                int playerColor = reconnectionGameData.getPlayerColor();

                GameData.setLevelGame(levelGame);
                GameData.setPhaseGame(gamePhase);
                GameData.setColor(playerColor);

                PageController.initGame(GameData.getLevelGame(), GameData.getColor());
                PageController.switchScene("buildingPage.fxml", "Building");

                Sender sender = GameData.getSender();

                sender.showHandComponent();
                sender.showBookedComponents();
                sender.showSpaceship(GameData.getNamePlayer());
                sender.showPlayers();
                sender.showVisibleComponents();

            } catch (IOException e) {
                Alerts.showWarning("Error loading the page");
                System.out.println("Error loading the page");
            }
        }

        else if (messageObj instanceof ShowWaitingPlayersMessage showWaitingPlayersMessage) {
            PageController.getWaitingRoomView().updatePlayersList(showWaitingPlayersMessage.getPlayers());
        }

        else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());

            if(GameData.getPhaseGame().equalsIgnoreCase("INIT"))
                PageController.getWaitingRoomView().activateReadyBtn();

            else if(GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                try {

                    GameData.saveGameData();

                    PageController.initGame(GameData.getLevelGame(), GameData.getColor());
                    PageController.switchScene("buildingPage.fxml", "Building");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("ADJUSTING")) {

                try {
                    PageController.switchScene("adjustingPage.fxml", "Adjusting");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("POPULATING")) {

                try {
                    PageController.switchScene("buildingPage.fxml", "Populating");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

            else if(GameData.getPhaseGame().equalsIgnoreCase("EVENT")) {
                try {
                    GameData.saveGameData();
                    PageController.initEvent(GameData.getLevelGame());
                    PageController.switchScene("gamePage.fxml", "Game");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }

        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {

            if(GameData.getPhaseGame().equalsIgnoreCase("EVENT")) {
                PageController.getEventView().showPlayerShip(responseSpaceshipMessage.getOwner());
            }

            else if(!responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer()))
                    PageController.getBuildingView().updateOtherPlayerSpaceship(responseSpaceshipMessage.getOwner(), responseSpaceshipMessage.getSpaceship());

            else
                PageController.getBuildingView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
        }

        else if (messageObj instanceof ShowHandComponentMessage showHandComponentMessage) {
            PageController.getBuildingView().generateHandComponent(showHandComponentMessage.getHandComponent());
        }

        else if (messageObj instanceof ShowBookedComponentsMessage pickedBookedComponentsMessage) {
            PageController.getBuildingView().updateBookedComponents(pickedBookedComponentsMessage.getBookedComponents());
        }

        else if(messageObj instanceof ShowPlayersMessage showPlayersMessage) {
            PageController.getBuildingView().updatePlayersList(showPlayersMessage.getPlayers());
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

        else if (messageObj instanceof AnotherPlayerDiscardComponentMessage anotherPlayerDiscardComponentMessage) {
            GameData.getSender().showVisibleComponents();
        }

        else if (messageObj instanceof AnotherPlayerPickedVisibleComponentMessage anotherPlayerDiscardComponentMessage) {
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

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            PageController.getEventView().initEventCard(pickedEventCardMessage.getEventCard().getImgSrc());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getBuildingView().updateTimer(timer);
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
                    Alerts.showPopUp("That game does not exist", false);
                    break;

                case "NotAvailableName":
                    Alerts.showPopUp("Username already taken for this game", false);
                    break;

                case "AllowedToPlaceComponent":
                    BuildingData.resetHandComponent();
                    break;

                case "NotAllowedToPlaceComponent":
                    if(BuildingData.getIsTimerExpired())
                        PageController.getBuildingView().removeHandComponent();
                    Alerts.showPopUp("You are not allowed to place this component", false);
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
                    Alerts.showPopUp("You cannot discard a booked component", false);
                    break;

                case "PickedBookedComponent":
                    BuildingData.setNewHandComponent(BuildingData.getTempPickingBookedComponent());
                    BuildingData.setXHandComponent(BuildingData.getTempXPickingBooked());
                    BuildingData.setYHandComponent(-1);
                    break;

                case "RequirePlacedComponent":
                    Alerts.showPopUp("Its required to place a component before picking up a deck!", true);

                case "EventCardDeckPutDown":
                    PageController.getBuildingView().hideEventDeck();
                    PageController.getBuildingView().updateEventDecksAvailability(BuildingData.getCurrentDeckIdx());
                    BuildingData.setCurrentDeckIdx(-1);
                    break;

                case "ImpossibleToResetTimer":
                    Alerts.showPopUp("Impossible to reset timer!", true);

                case "TimerExpired":
                    Alerts.showPopUp("Timer expired!", false);
                    PageController.getBuildingView().disableDraggableBookedComponents();
                    PageController.getBuildingView().placeLastComponent();
                    BuildingData.setIsTimerExpired(true);
                    break;

                case "YouAreReady":
                    System.out.println("You are ready");
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