package org.progetto.client.connection;

import org.progetto.client.gui.Alerts;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.AnotherPlayerPlacedComponentMessage;
import org.progetto.messages.toClient.Building.PickedComponentMessage;
import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.Building.TimerMessage;
import org.progetto.server.connection.games.WaitingGameInfo;
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
            ArrayList<WaitingGameInfo> gamesInfo = showWaitingGamesMessage.getWaitingGames();
            PageController.getChooseGameView().generateGameRecordList(gamesInfo);
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {

            System.out.println("You joined a game");

            int gameId = initGameMessage.getIdGame();
            int levelGame = initGameMessage.getLevelGame();
            int numMaxPlayer = initGameMessage.getNumMaxPlayers();
            int color = initGameMessage.getColor();

            try {
                // todo: se i game possono essere da 1 persona controllare per non mandarlo in waiting room??
                PageController.switchScene("waitingRoom.fxml", "WaitingRoom");
                PageController.getWaitingRoomView().init(gameId, levelGame, numMaxPlayer);
            } catch (IOException e) {
                System.out.println("Error loading the page");
            }

            GameData.setIdGame(gameId);
            GameData.setColor(color);
            PageController.initGame(levelGame, color);

        } else if (messageObj instanceof ShowWaitingPlayersMessage showWaitingPlayersMessage) {
            PageController.getWaitingRoomView().updatePlayersList(showWaitingPlayersMessage.getPlayers());

        } else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());

            if(GameData.getPhaseGame().equalsIgnoreCase("BUILDING")) {
                try {
                    PageController.switchScene("game.fxml", "Game");
                } catch (IOException e) {
                    System.out.println("Error loading the page");
                }
            }
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            PageController.getGameView().generateComponent(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            System.out.println(anotherPlayerPlacedComponentMessage.getNamePlayer() + " has placed: " + anotherPlayerPlacedComponentMessage.getImgSrcPlacedComponent());
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
                    Alerts.showWarning("Not valid game ID");
                    break;

                case "NotAvailableName":
                    Alerts.showWarning("Username not available for this game");
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