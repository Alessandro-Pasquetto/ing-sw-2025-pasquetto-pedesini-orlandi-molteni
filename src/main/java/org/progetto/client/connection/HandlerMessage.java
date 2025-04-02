package org.progetto.client.connection;

import org.progetto.client.gui.DragAndDrop;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toClient.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Handles messages coming from server
 */
public class HandlerMessage {

    /**
     * Method that handles the messages coming from the server
     *
     * @param messageObj the message that has arrived
     */
    public static void handleMessage(Object messageObj) {

        if (messageObj instanceof GameListMessage gameListMessage) {
            ArrayList<Integer> idGames = gameListMessage.getIdGames();
            System.out.println("Gamelist arrivata... da elaborare");
        }
        else if (messageObj instanceof NotifyNewGameMessage notifyNewGameMessage) {
            PageController.generateGameList(notifyNewGameMessage.getIdGame());

        } else if (messageObj instanceof GameInfoMessage initGameMessage) {
            System.out.println("You have created a new game");
            GameData.setIdGame(initGameMessage.getIdGame());

            try {
                PageController.switchScene("game.fxml", "Game");
            } catch (IOException e) {
                System.out.println("Error loading the page");
            }

            PageController.initGame(initGameMessage.getImgPathBoard(), initGameMessage.getImgPathSpaceship(), initGameMessage.getImgPathCentralUnit());

        } else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            PageController.generateComponent(pickedComponentMessage.getImgPath());

        } else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            System.out.println(anotherPlayerPlacedComponentMessage.getNamePlayer() + " has placed: " + anotherPlayerPlacedComponentMessage.getImgSrcPlacedComponent());

        } else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            PageController.getGameView().updateTimer(timer);

        } else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "AllowedToJoinGame":
                    System.out.println("You joined a game");

                    try {
                        PageController.switchScene("game.fxml", "Game");
                    } catch (IOException e) {
                        System.out.println("Error loading the page");
                    }
                    break;

                case "NotAvailableName":
                    System.out.println("Username not available");
                    break;

                case "NotAllowedToPlaceComponent":
                    break;

                case "PickedBookedComponent":
                    BuildingData.setNewHandComponent(BuildingData.getTempBookedComponent());
                    break;

                case "TimerExpired":
                    System.out.println("TimerExpired");
                    BuildingData.setTimerExpired(true);
                    break;

                default:
                    System.out.println(messageString);
                    break;
            }
        }
    }
}