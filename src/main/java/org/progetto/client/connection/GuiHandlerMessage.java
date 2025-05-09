package org.progetto.client.connection;

import org.progetto.client.gui.Alerts;
import org.progetto.client.gui.BuildingView;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
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

        } else if (messageObj instanceof ShowWaitingPlayersMessage showWaitingPlayersMessage) {
            PageController.getWaitingRoomView().updatePlayersList(showWaitingPlayersMessage.getPlayers());

        } else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());

            if(GameData.getPhaseGame().equalsIgnoreCase("INIT"))
                PageController.getWaitingRoomView().activateReadyBtn();

            else if(GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                try {

                    GameData.saveGameData();

                    PageController.initGame(GameData.getLevelGame(), GameData.getColor());
                    PageController.switchScene("buildingPage.fxml", "Game");

                } catch (IOException e) {
                    Alerts.showWarning("Error loading the page");
                    System.out.println("Error loading the page");
                }
            }
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {

            if(!responseSpaceshipMessage.getOwner().getName().equals(GameData.getNamePlayer()))
                PageController.getGameView().updateOtherPlayerSpaceship(responseSpaceshipMessage.getOwner(), responseSpaceshipMessage.getSpaceship());
            else
                PageController.getGameView().updateSpaceship(responseSpaceshipMessage.getSpaceship());
        }

        else if(messageObj instanceof ShowPlayersMessage showPlayersMessage) {
            PageController.getGameView().initPlayersList(showPlayersMessage.getPlayers());
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            PageController.getGameView().generateComponent(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof ShowVisibleComponentsMessage pickedVisibleComponentsMessage) {
            PageController.getGameView().loadVisibleComponents(pickedVisibleComponentsMessage.getVisibleComponentDeck());
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
            PageController.getGameView().showEventDeck(pickedUpEventCardDeckMessage.getEventCardsDeck());
            PageController.getGameView().updateEventDecksAvailability(pickedUpEventCardDeckMessage.getDeckIdx());
            BuildingData.setCurrentDeckIdx(pickedUpEventCardDeckMessage.getDeckIdx());
        }

        else if(messageObj instanceof AnotherPlayerPickedUpEventCardDeck anotherPlayerPickedUpEventCardDeck) {
            PageController.getGameView().updateEventDecksAvailability(anotherPlayerPickedUpEventCardDeck.getDeckIdx());
        }

        else if(messageObj instanceof AnotherPlayerPutDownEventCardDeckMessage anotherPlayerPutDownEventCardDeckMessage) {
            PageController.getGameView().updateEventDecksAvailability(anotherPlayerPutDownEventCardDeckMessage.getDeckIdx());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getGameView().updateTimer(timer);
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Current card: " + pickedEventCardMessage.getImgSrc());
        }

        else if (messageObj instanceof String messageString) {

            switch (messageString) {

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
                        PageController.getGameView().removeHandComponent();
                    break;

                case "ComponentBooked":
                    break;

                case "HandComponentDiscarded":
                    PageController.getGameView().removeHandComponent();
                    break;

                case "HasBeenBooked":
                    System.out.println("You cannot discard a booked component");
                    break;

                case "PickedBookedComponent":
                    BuildingData.setNewHandComponent(BuildingData.getTempBookedComponent());
                    break;

                case "EventCardDeckPutDown":
                    PageController.getGameView().hideEventDeck();
                    PageController.getGameView().updateEventDecksAvailability(BuildingData.getCurrentDeckIdx());
                    BuildingData.setCurrentDeckIdx(-1);
                    break;

                case "TimerExpired":
                    System.out.println("TimerExpired");
                    PageController.getGameView().disableDraggableBookedComponents();
                    PageController.getGameView().placeLastComponent();
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