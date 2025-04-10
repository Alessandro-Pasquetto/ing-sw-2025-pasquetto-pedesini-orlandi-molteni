package org.progetto.client.connection;

import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.BuildingCommands;
import org.progetto.client.tui.GameCommands;
import org.progetto.messages.toClient.Building.AnotherPlayerPlacedComponentMessage;
import org.progetto.messages.toClient.Building.PickedComponentMessage;
import org.progetto.messages.toClient.Building.PickedEventCardMessage;
import org.progetto.messages.toClient.Building.TimerMessage;
import org.progetto.messages.toClient.GameInfoMessage;
import org.progetto.messages.toClient.GameListMessage;
import org.progetto.messages.toClient.NotifyNewGameMessage;
import org.progetto.messages.toClient.Spaceship.RequestedSpaceshipMessage;
import java.util.ArrayList;

/**
 * Handles messages coming from server
 */
public class TuiHandlerMessage {

    /**
     * Method that handles the messages coming from the server updating the TUI
     *
     * @param messageObj the message that has arrived
     */
    public static void handleMessage(Object messageObj) {

        if (messageObj instanceof GameListMessage gameListMessage) {
            ArrayList<Integer> idGames = gameListMessage.getIdGames();
            System.out.println("Gamelist arrivata... da elaborare");
        }

        else if (messageObj instanceof NotifyNewGameMessage notifyNewGameMessage) {
            System.out.println("New Game: " + notifyNewGameMessage.getIdGame());
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {
            System.out.println("Create new game with ID:"+initGameMessage.getIdGame());
            GameData.setIdGame(initGameMessage.getIdGame());
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            BuildingCommands.printComponentInfo(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            System.out.println(anotherPlayerPlacedComponentMessage.getNamePlayer() + " has placed: " );
            BuildingCommands.printComponentInfo(anotherPlayerPlacedComponentMessage.getComponent());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            if(timer == 10)
                System.out.print("10 seconds to the end");
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            BuildingCommands.printEventCard(pickedEventCardMessage.getEventCard());
        }

        else if (messageObj instanceof RequestedSpaceshipMessage requestedSpaceshipMessage) {
            System.out.println(requestedSpaceshipMessage.getOwner()+"'s spaceship:");
            GameCommands.printShipStatus(requestedSpaceshipMessage.getSpaceship());
        }

        else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "AllowedToJoinGame":
                    System.out.println("You joined a game");
                    break;

                case "NotAvailableName":
                    System.out.println("Username not available");
                    break;

                case "AllowedToPlaceComponent":
                    BuildingData.resetHandComponent();
                    break;

                case "NotAllowedToPlaceComponent":
                    if(BuildingData.getIsTimerExpired())
                        System.out.print("Time finished");
                    break;

                case "PickedBookedComponent":
                    BuildingData.setNewHandComponent(BuildingData.getTempBookedComponent());
                    break;

                case "TimerExpired":
                    System.out.println("TimerExpired");
                    BuildingData.setIsTimerExpired(true);
                    break;

                case "PlayerNameNotFound":
                    System.out.println("Unable to find player");

                default:
                    System.out.println(messageString);
                    break;
            }
        }
    }
}