package org.progetto.client.connection;

import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.BuildingCommands;
import org.progetto.client.tui.EventCommands;
import org.progetto.client.tui.GameCommands;
import org.progetto.client.tui.TuiCommandFilter;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventCommon.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
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
    public synchronized static void handleMessage(Object messageObj) {

        if (messageObj instanceof ShowWaitingGamesMessage showWaitingGamesMessage) {
            ArrayList<Integer> idGames = showWaitingGamesMessage.getIdWaitingGames();

            for (Integer idGame : idGames) {
                System.out.println("New Game: " + idGame);
            }
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {
            System.out.println("You joined a game with ID: " + initGameMessage.getIdGame());
            GameData.setIdGame(initGameMessage.getIdGame());
            GameData.setLevelGame(initGameMessage.getLevelGame());
        }

        else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println(newGamePhaseMessage.getPhaseGame() + " phase started...");
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());
        }

        else if (messageObj instanceof ShowHandComponentMessage showHandComponentMessage) {
            System.out.println("Current Hand Component:");
            BuildingCommands.printComponent(showHandComponentMessage.getHandComponent());
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            System.out.println("New component picked:");
            BuildingCommands.printComponent(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
            System.out.println(anotherPlayerPlacedComponentMessage.getNamePlayer() + " has placed: " );
            BuildingCommands.printComponent(anotherPlayerPlacedComponentMessage.getComponent());
        }

        else if (messageObj instanceof ShowVisibleComponentsMessage pickedVisibleComponentsMessage) {
            BuildingCommands.printVisibleComponents(pickedVisibleComponentsMessage.getVisibleComponentDeck());
        }

        else if (messageObj instanceof ShowBookedComponentsMessage pickedBookedComponentsMessage) {
            BuildingCommands.printBookedComponents(pickedBookedComponentsMessage.getBookedComponents());
        }

        else if (messageObj instanceof PickedUpEventCardDeckMessage pickedUpEventCardDeckMessage) {
            EventCommands.printEventCardDeck(pickedUpEventCardDeckMessage.getEventCardsDeck());
        }

        else if (messageObj instanceof AnotherPlayerPickedUpEventCardDeck anotherPlayerPickedUpEventCardDeck) {
            System.out.println(anotherPlayerPickedUpEventCardDeck.getNamePlayer() + " picked up event card deck " + anotherPlayerPickedUpEventCardDeck.getDeckIdx());
        }

        else if (messageObj instanceof AnotherPlayerPutDownEventCardDeckMessage anotherPlayerPutDownEventCardDeckMessage) {
            System.out.println(anotherPlayerPutDownEventCardDeckMessage.getNamePlayer() + " put down event card deck " + anotherPlayerPutDownEventCardDeckMessage.getDeckIdx());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            if (timer == 10)
                System.out.println("10 seconds to the end");
            else if(timer == 0)
                System.out.println("Timer is at 0s");
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            EventCommands.printEventCard(pickedEventCardMessage.getEventCard());
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {
            System.out.println(responseSpaceshipMessage.getOwner() + "'s spaceship:");
            GameCommands.printSpaceship(responseSpaceshipMessage.getSpaceship());
        }

        else if(messageObj instanceof HowManyDoubleCannonsMessage howManyDoubleCannonsMessage) {
            EventCommands.responseHowManyDoubleCannons(
                    howManyDoubleCannonsMessage.getFirePowerRequired(),
                    howManyDoubleCannonsMessage.getMaxUsable()
                    );
        }

        else if(messageObj instanceof HowManyDoubleEnginesMessage howManyDoubleEnginesMessage) {
            EventCommands.responseHowManyDoubleEngines(howManyDoubleEnginesMessage.getMaxUsable());
        }

        else if(messageObj instanceof BatteriesToDiscardMessage batteriesToDiscardMessage) {
            EventCommands.responseBatteryToDiscard(batteriesToDiscardMessage.getBatteriesToDiscard());
        }

        else if(messageObj instanceof CrewToDiscardMessage crewToDiscardMessage) {
            EventCommands.responseCrewToDiscard(crewToDiscardMessage.getCrewToDiscard());
        }

        else if(messageObj instanceof BoxToDiscardMessage boxToDiscardMessage) {
            EventCommands.responseBoxToDiscard(boxToDiscardMessage.getBoxToDiscard());
        }

        else if(messageObj instanceof AcceptRewardCreditsAndPenaltiesMessage acceptRewardCreditsAndPenaltiesMessage) {
            EventCommands.responseAcceptRewardCreditsAndPenalties(
                    acceptRewardCreditsAndPenaltiesMessage.getRewardCredits(),
                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyDays(),
                    acceptRewardCreditsAndPenaltiesMessage.getPenaltyCrew()
            );
        }

        else if(messageObj instanceof AcceptRewardCreditsAndPenaltyDaysMessage acceptRewardCreditsAndPenaltyDaysMessage) {
            EventCommands.responseAcceptRewardCreditsAndPenaltyDays(
                    acceptRewardCreditsAndPenaltyDaysMessage.getRewardCredits(),
                    acceptRewardCreditsAndPenaltyDaysMessage.getPenaltyDays()
            );
        }

        else if(messageObj instanceof AvailablePlanetsMessage availablePlanetsMessage) {
            EventCommands.responsePlanetLandRequest(availablePlanetsMessage.getPlanetsTaken());
        }

        else if(messageObj instanceof AvailableBoxesMessage availableBoxesMessage) {
            EventCommands.responseRewardBox(availableBoxesMessage.getBoxes());
        }

        else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "UpdateGameList":
                    break;

                case "NotAvailableName":
                    System.out.println("Username not available");
                    break;

                case "HandComponentDiscarded":
                    System.out.println("Current hand component discarded");
                    break;

                case "AllowedToPlaceComponent":
                    System.out.println("Component placed successfully!");
                    BuildingData.resetHandComponent();
                    break;

                case "NotAllowedToPlaceComponent":
                    System.out.println("Component not placed");
                    if(BuildingData.getIsTimerExpired())
                        System.out.print("Time finished");
                    break;

                case "ComponentBooked":
                    System.out.println("Component booked");
                    break;

                case "HasBeenBooked":
                    System.out.println("You cannot discard a booked component");
                    break;

                case "PickedBookedComponent":
                    System.out.println("Picked booked");
                    break;

                case "EventCardDeckPutDown":
                    System.out.println("Event card deck put down!");
                    break;

                case "TimerExpired":
                    System.out.println("TimerExpired");
                    BuildingData.setIsTimerExpired(true);
                    break;

                case "PlayerNameNotFound":
                    System.out.println("Unable to find player");
                    break;

                case "AskToUseShield":
                    EventCommands.responseChooseToUseShield();
                    break;

                case "AskToUseDoubleCannon":
                    EventCommands.responseUseDoubleCannonRequest();
                    break;

                case "LandRequest":
                    EventCommands.responseLandRequest();
                    break;

                case "NotYourTurn":
                    System.out.println("Not your turn!");
                    break;

                case "IncorrectPhase":
                    System.out.println("Can't use that, incorrect phase!");
                    break;

                case "NotEnoughBatteries":
                    System.out.println("Not enough batteries!");
                    break;

                case "InvalidCoordinates":
                    System.out.println("Invalid coordinates!");
                    break;

                case "BatteryDiscarded":
                    System.out.println("Battery discarded");
                    break;

                case "YouAreSafe":
                    System.out.println("You are safe");
                    break;

                case "NotValidSpaceShip":
                    System.out.println("Your spaceship is trash, fix it!");
                    TuiCommandFilter.setFixing(true);
                    break;

                case "ValidSpaceShip":
                    System.out.println("Your spaceship is pretty good, you're ready to go!");
                    TuiCommandFilter.setFixing(false);
                    break;

                default:
                    System.out.println(messageString);
                    break;
            }
        }
    }
}