package org.progetto.client.connection;

import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.*;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedPlanetMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Populating.AlienPlacedMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.messages.toClient.Positioning.AskStartingPositionMessage;
import org.progetto.messages.toClient.Positioning.PlayersInPositioningDecisionOrderMessage;
import org.progetto.messages.toClient.Positioning.StartingPositionsMessage;
import org.progetto.messages.toClient.Smugglers.AcceptRewardBoxesAndPenaltyDaysMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipStatsMessage;
import org.progetto.messages.toClient.Travel.PlayerLeftMessage;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles messages coming from server
 */
public class TuiHandlerMessage {

    // =======================
    // COLORS
    // =======================

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";
    private static final String ORANGE = "\u001B[38;5;208m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Method that handles the messages coming from the server updating the TUI
     *
     * @param messageObj the message that has arrived
     */
    public static void handleMessage(Object messageObj) {

        if (messageObj instanceof WaitingGamesMessage waitingGamesMessage) {
            TuiPrinters.printWaitingGames(waitingGamesMessage.getWaitingGames());
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {
            System.out.println("You joined a game:");
            System.out.printf ("│ ID: %d %n", initGameMessage.getIdGame());
            System.out.printf ("│ Level: %d %n", initGameMessage.getLevelGame());

            GameData.setIdGame(initGameMessage.getIdGame());
            GameData.setLevelGame(initGameMessage.getLevelGame());
        }

        else if (messageObj instanceof WaitingPlayersMessage waitingPlayersMessage) {

        }

        else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            System.out.println(newGamePhaseMessage.getPhaseGame() + " phase started...");
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());
            switch (newGamePhaseMessage.getPhaseGame()) {
                case "WAITING":

                    break;

                case "INIT":
                    System.out.println("Write ready when you are ready to play");
                    break;

                case "BUILDING":
                    System.out.println("Write ready when you have finished to build your ship");
                    break;

                case "ADJUSTING":

                    break;

                case "POPULATING":

                    break;

                case "POSITIONING":

                    break;

                case "TRAVEL":

                    break;

                case "EVENT":

                    break;

                case "ENDGAME":

                    break;
            }
        }

        else if (messageObj instanceof PlayerColorMessage playerColorMessage) {
            GameData.setColor(playerColorMessage.getColor());
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {
            TuiPrinters.printSpaceship(responseSpaceshipMessage.getOwner().getName(), responseSpaceshipMessage.getSpaceship(), responseSpaceshipMessage.getOwner().getColor());
        }

        else if (messageObj instanceof ResponseSpaceshipStatsMessage responseSpaceshipStatsMessage) {
            TuiPrinters.printSpaceshipStats(responseSpaceshipStatsMessage.getSpaceship());
        }

        else if (messageObj instanceof PlayerStatsMessage playerStatsMessage) {
            TuiPrinters.printPlayerStats(playerStatsMessage.getPlayerName(), playerStatsMessage.getCredits(), playerStatsMessage.getPosition(), playerStatsMessage.getHasLeft());
        }

        else if (messageObj instanceof TrackMessage trackMessage) {
            TuiPrinters.printTrack(trackMessage.getTravelers(), trackMessage.getTrack());
        }

        else if (messageObj instanceof ShowHandComponentMessage showHandComponentMessage) {
            System.out.println("Current hand component:");
            TuiPrinters.printComponent(showHandComponentMessage.getHandComponent());
        }

        else if (messageObj instanceof PickedComponentMessage pickedComponentMessage) {
            System.out.println("New component picked:");
            TuiPrinters.printComponent(pickedComponentMessage.getPickedComponent());
        }

        else if (messageObj instanceof AnotherPlayerDiscardComponentMessage anotherPlayerDiscardComponentMessage) {
            System.out.println(anotherPlayerDiscardComponentMessage.getNamePlayer() + " discarded a component");
        }

        else if (messageObj instanceof AnotherPlayerPlacedComponentMessage anotherPlayerPlacedComponentMessage) {
        }

        else if (messageObj instanceof ShowVisibleComponentsMessage pickedVisibleComponentsMessage) {
            TuiPrinters.printVisibleComponents(pickedVisibleComponentsMessage.getVisibleComponentDeck());
        }

        else if (messageObj instanceof ShowBookedComponentsMessage pickedBookedComponentsMessage) {
            TuiPrinters.printBookedComponents(pickedBookedComponentsMessage.getBookedComponents());
        }

        else if (messageObj instanceof PickedUpEventCardDeckMessage pickedUpEventCardDeckMessage) {
            TuiPrinters.printEventCardDeck(pickedUpEventCardDeckMessage.getEventCardsDeck());
        }

        else if (messageObj instanceof AnotherPlayerPickedUpEventCardDeck anotherPlayerPickedUpEventCardDeck) {
            System.out.println(anotherPlayerPickedUpEventCardDeck.getNamePlayer() + " picked up event card deck " + anotherPlayerPickedUpEventCardDeck.getDeckIdx());
        }

        else if (messageObj instanceof AnotherPlayerPutDownEventCardDeckMessage anotherPlayerPutDownEventCardDeckMessage) {
            System.out.println(anotherPlayerPutDownEventCardDeckMessage.getNamePlayer() + " put down event card deck " + anotherPlayerPutDownEventCardDeckMessage.getDeckIdx());
        }

        else if(messageObj instanceof AnotherPlayerBookedComponentMessage anotherPlayerBookedComponentMessage){
            System.out.println(anotherPlayerBookedComponentMessage.getPlayerName() + " booked a component at " + anotherPlayerBookedComponentMessage.getIdx());
        }

        else if(messageObj instanceof AnotherPlayerPickedBookedComponentMessage anotherPlayerPickedBookedComponentMessage){
            System.out.println(anotherPlayerPickedBookedComponentMessage.getPlayerName() + " picked a booked component at " + anotherPlayerPickedBookedComponentMessage.getIdx());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            if (timer == 10)
                System.out.println(ORANGE + "10 seconds to the end" + RESET);
            else if(timer == 0)
                System.out.println(ORANGE + "Timer is at 0s" + RESET);
        }

        else if (messageObj instanceof AnotherPlayerIsReadyMessage anotherPlayerIsReadyMessage) {
            System.out.println(anotherPlayerIsReadyMessage.getNamePlayer() + " is ready");
            System.out.println();
        }

        else if (messageObj instanceof PickedEventCardMessage pickedEventCardMessage) {
            System.out.println("Card picked: " + pickedEventCardMessage.getEventCard().getType());
            TuiPrinters.printEventCard(pickedEventCardMessage.getEventCard());
        }

        else if(messageObj instanceof AskAlienMessage askAlien) {
            BuildingCommands.responsePlaceAlien(askAlien.getColor(), askAlien.getSpaceship());
        }

        else if(messageObj instanceof AlienPlacedMessage alienPlacedMessage) {
            System.out.println("Alien successfully placed at:");
            System.out.printf ("│ X: %d %n", alienPlacedMessage.getX());
            System.out.printf ("│ Y: %d %n", alienPlacedMessage.getY());
            System.out.println();
        }

        else if(messageObj instanceof PlayersInPositioningDecisionOrderMessage playersInPositioningDecisionOrderMessage) {
            List<String> playersNames = new ArrayList<>();
            for (Player player: playersInPositioningDecisionOrderMessage.getPlayers()){
                playersNames.add(player.getName());
            }
            System.out.println("Players in decision order: " + playersNames);
        }

        else if(messageObj instanceof ActivePlayerMessage activePlayerMessage) {
            System.out.println("Active player: " + activePlayerMessage.getPlayerName());
        }

        else if(messageObj instanceof AskStartingPositionMessage askStartingPositionMessage) {
            BuildingCommands.responseStartingPosition(askStartingPositionMessage.getStartingPositions());
        }

        else if(messageObj instanceof HowManyDoubleCannonsMessage howManyDoubleCannonsMessage) {
            EventCommands.responseHowManyDoubleCannons(
                    howManyDoubleCannonsMessage.getFirePowerRequired(),
                    howManyDoubleCannonsMessage.getMaxUsable(),
                    howManyDoubleCannonsMessage.getShootingPower()
                    );
        }

        else if(messageObj instanceof HowManyDoubleEnginesMessage howManyDoubleEnginesMessage) {
            EventCommands.responseHowManyDoubleEngines(howManyDoubleEnginesMessage.getMaxUsable(), howManyDoubleEnginesMessage.getEnginePower());
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

        else if(messageObj instanceof AcceptRewardBoxesAndPenaltyDaysMessage acceptRewardBoxesAndPenaltyDaysMessage) {
            EventCommands.responseAcceptRewardBoxesAndPenaltyDays(
                    acceptRewardBoxesAndPenaltyDaysMessage.getRewardBoxes(),
                    acceptRewardBoxesAndPenaltyDaysMessage.getPenaltyDays()
            );
        }

        else if(messageObj instanceof AvailablePlanetsMessage availablePlanetsMessage) {
            EventCommands.responsePlanetLandRequest(availablePlanetsMessage.getRewardsForPlanets(), availablePlanetsMessage.getPlanetsTaken());
        }

        else if(messageObj instanceof AvailableBoxesMessage availableBoxesMessage) {
            EventCommands.responseRewardBox(availableBoxesMessage.getBoxes());
        }

        else if (messageObj instanceof AnotherPlayerLandedPlanetMessage anotherPlayerLandedPlanetMessage){
            String name = anotherPlayerLandedPlanetMessage.getPlayer().getName();
            int idxPlanet = anotherPlayerLandedPlanetMessage.getPlanetIdx();

            System.out.println(name + " landed on planet: " + idxPlanet);
        }

        else if (messageObj instanceof AnotherPlayerLandedMessage anotherPlayerLandedMessage){
            String name = anotherPlayerLandedMessage.getPlayer().getName();

            System.out.println(name + "landed");
        }

        else if(messageObj instanceof PlayerMovedAheadMessage playerMovedAheadMessage) {
            System.out.println(GREEN + "You have moved ahead of " + playerMovedAheadMessage.getStepsCount() + " positions" + RESET);
        }

        else if(messageObj instanceof AnotherPlayerMovedAheadMessage anotherPlayerMovedAheadMessage) {
            System.out.println(GREEN + anotherPlayerMovedAheadMessage.getNamePlayer() + " have moved ahead of " + anotherPlayerMovedAheadMessage.getStepsCount() + " positions" + RESET);
        }

        else if(messageObj instanceof PlayerMovedBackwardMessage playerMovedBackwardMessage) {
            System.out.println(BLUE + "You have moved backward of " + playerMovedBackwardMessage.getStepsCount() + " positions" + RESET);
        }

        else if(messageObj instanceof AnotherPlayerMovedBackwardMessage anotherPlayerMovedBackwardMessage) {
            System.out.println(BLUE + anotherPlayerMovedBackwardMessage.getNamePlayer() + " have moved backward of " + anotherPlayerMovedBackwardMessage.getStepsCount() + " positions" + RESET);
        }

        else if(messageObj instanceof PlayerGetsCreditsMessage playerGetsCreditsMessage) {
            System.out.println(YELLOW + "You received " + playerGetsCreditsMessage.getCredits() + " credits" + RESET);
        }

        else if(messageObj instanceof AnotherPlayerGetsCreditsMessage anotherPlayerGetsCreditsMessage) {
            System.out.println(YELLOW + anotherPlayerGetsCreditsMessage.getNamePlayer() + " received " + anotherPlayerGetsCreditsMessage.getCredits() + " credits" + RESET);
        }

        else if(messageObj instanceof IncomingProjectileMessage incomingProjectileMessage) {
            System.out.println();
            TuiPrinters.printIncomingProjectile(incomingProjectileMessage);
        }

        else if(messageObj instanceof DiceResultMessage diceResultMessage) {
            System.out.println("Dice result: " +  diceResultMessage.getDiceResult());
        }

        else if(messageObj instanceof AnotherPlayerDiceResultMessage anotherPlayerDiceResultMessage) {
            System.out.println("Dice result: " +  anotherPlayerDiceResultMessage.getDiceResult());
        }

        else if (messageObj instanceof DestroyedComponentMessage destroyedComponentMessage){
            TuiPrinters.printDestroyedComponent(null, destroyedComponentMessage.getxComponent(), destroyedComponentMessage.getyComponent());
        }

        else if(messageObj instanceof AnotherPlayerDestroyedComponentMessage anotherPlayerDestroyedComponentMessage){
            TuiPrinters.printDestroyedComponent(
                    anotherPlayerDestroyedComponentMessage.getNamePlayer(),
                    anotherPlayerDestroyedComponentMessage.getxComponent(),
                    anotherPlayerDestroyedComponentMessage.getyComponent()
            );
        }

        else if(messageObj instanceof PlayerLeftMessage playerLeftMessage) {
            System.out.println(PURPLE + playerLeftMessage.getPlayerName() + " left travel" + RESET);
        }

        else if(messageObj instanceof PlayerDefeatedMessage playerDefeatedMessage) {
            System.out.println(RED + playerDefeatedMessage.getPlayerName() + " was defeated by !" + RESET);
        }

        else if(messageObj instanceof ScoreBoardMessage scoreBoardMessage) {
            TuiPrinters.printScoreBoard(scoreBoardMessage.getScoreBoard());
        }


        else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "UpdateGameList":
                    break;

                case "NotValidGameId":
                    System.err.println("Not valid game ID!");
                    break;

                case "NotAvailableName":
                    System.err.println("Username not available!");
                    break;

                case "HandComponentDiscarded":
                    System.out.println("Current hand component discarded");
                    break;

                case "FullHandComponent":
                    System.err.println("Hand is full!");
                    break;

                case "AllowedToPlaceComponent":
                    System.out.println("Component placed successfully!");
                    BuildingData.resetHandComponent();
                    break;

                case "NotAllowedToPlaceComponent":
                    System.err.println("Component not placed");
                    if(BuildingData.getIsTimerExpired())
                        System.out.print(ORANGE + "Time finished" + RESET);
                    break;

                case "ComponentBooked":
                    System.out.println("Component booked");
                    break;

                case "HasBeenBooked":
                    System.err.println("You cannot discard a booked component");
                    break;

                case "PickedBookedComponent":
                    System.out.println("Picked booked");
                    break;

                case "EventCardDeckPutDown":
                    System.out.println("Event card deck put down!");
                    break;

                case "CannotPickUpEventCardDeck":
                    System.err.println("You cannot pick up the event card deck!");
                    break;

                case "TimerExpired":
                    System.out.println(ORANGE + "Timer is expired!" + RESET);
                    BuildingData.setIsTimerExpired(true);
                    break;

                case "YouAreReady":
                    System.out.println("You are ready");
                    break;

                case "ActionNotAllowedInReadyState":
                    System.err.println("Action not allowed in ready state!");
                    break;

                case "ValidStartingPosition":
                    System.out.println("Starting position set successfully!");
                    break;

                case "ComponentAlreadyOccupied":
                    System.err.println("Component already occupied!");
                    break;

                case "CannotContainOrangeAlien":
                    System.err.println("Cannot contain orange alien!");
                    break;

                case "CannotContainPurpleAlien":
                    System.err.println("Cannot contain purple alien!");
                    break;

                case "PlayerNameNotFound":
                    System.err.println("Unable to find player");
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

                case "LandingCompleted":
                    System.out.println("Landing completed!");
                    break;

                case "NotYourTurn":
                    System.err.println("Not your turn!");
                    break;

                case "IncorrectPhase":
                    System.err.println("Can't use that, incorrect phase!");
                    break;

                case "NotValidCoordinates":
                    System.err.println("Invalid coordinates!");
                    break;

                case "EmptyHandComponent":
                    System.err.println("You can't place with an empty hand, draw first!");
                    break;

                case "IllegalIndexEventCardDeck":
                    System.err.println("You have only 3 decks, choose a valid index!");
                    break;

                case "NotEnoughBatteries":
                    System.err.println("Not enough batteries!");
                    break;

                case "FullHandEventDeck":
                    System.err.println("You first need to put down the deck you are looking at! ");
                    break;

                case "InvalidCoordinates":
                    System.err.println("Invalid coordinates!");
                    break;

                case "InvalidComponent":
                    System.err.println("Invalid component!");
                    break;

                case "BookedCellOccupied":
                    System.err.println("You have already a component in that cell!");
                    break;

                case "IllegalBookIndex":
                    System.err.println("You only have 2 cells, choose a valid index!");
                    break;

                case "ImpossibleToDestroyCentralUnit":
                    System.err.println("Impossible to destroy central unit!");
                    break;

                case "ImpossibleToDestroyCorrectlyPlaced":
                    System.err.println("Impossible to destroy a correctly placed component!");
                    break;

                case "BatteryDiscarded":
                    System.out.println("Battery discarded");
                    break;

                case "BatteryNotDiscarded":
                    System.err.println("Unable to discard the battery!");
                    break;

                case "IncorrectNumber":
                    System.err.println("Incorrect number!");
                    break;

                case "NotEnoughBoxes":
                    System.err.println("Not enough boxes!");
                    break;

                case "NotEnoughCrew":
                    System.err.println("Not enough crew!");
                    break;

                case "CrewMemberDiscarded":
                    System.out.println("Crew member discarded");
                    break;

                case "CrewMemberNotDiscarded":
                    System.err.println("Unable to discard the crew member!");
                    break;

                case "BoxDiscarded":
                    System.out.println("Box discarded");
                    break;

                case "BoxNotDiscarded":
                    System.err.println("Unable to discard the box!");
                    break;

                case "BoxChosen":
                    System.out.println("Box placed correctly");
                    break;

                case "BoxNotChosen":
                    System.err.println("Unable to place selected box in that position!");
                    break;

                case "NotValidBoxContainer":
                    System.err.println("The box storage is not correct for that type of box!");
                    break;

                case "EmptyReward":
                    System.out.println("No reward boxes left!");
                    break;

                case "PermissionDenied":
                    System.err.println("You cannot do that right now!");
                    break;

                case "BoxAlreadyThere":
                    System.err.println("The box is already there!");
                    break;

                case "RedBoxMoved":
                    System.out.println("Box moved successfully");
                    break;

                case "RedBoxNotMoved":
                    System.err.println("Unable to move the box!");
                    break;

                case "CantStoreInANonRedStorage":
                    System.err.println("You cannot store a red box in a non-red storage!");
                    break;

                case "BoxMoved":
                    System.out.println("Box moved successfully");
                    break;

                case "BoxNotMoved":
                    System.err.println("Unable to move the box!");
                    break;

                case "NotAStorageComponent":
                    System.err.println("The component is not a storage component!");
                    break;

                case "BoxRemoved":
                    System.out.println("Box removed successfully");
                    break;

                case "BoxNotRemoved":
                    System.err.println("Unable to remove the box!");
                    break;

                case "YouAreSafe":
                    System.out.println(GREEN + "You are safe" + RESET);
                    break;

                case "NotValidSpaceShip":
                    System.out.println(RED + "Your spaceship is trash, fix it!" + RESET);
                    break;

                case "ValidSpaceShip":
                    System.out.println(GREEN + "Your spaceship is pretty good, you're ready to go!" + RESET);
                    break;

                case "AskContinueTravel":
                    EventCommands.responseContinueTravel();
                    break;

                case "YouLeftTravel":
                    System.out.println(PURPLE + "You left travel" + RESET);
                    break;

                case "YouAreContinuingTravel":
                    System.out.println("You are continuing travel");
                    break;

                case "NoComponentHit":
                    System.out.println(GREEN + "What a luck, no component hit!" + RESET);
                    break;

                case "NoComponentDamaged":
                    System.out.println(GREEN + "Close call, no component damaged!" + RESET);
                    break;

                case "NoShieldAvailable":
                    System.out.println(RED + "Oh no, you've no shield available!" + RESET);
                    break;

                case "NoCannonAvailable":
                    System.out.println(RED + "Oh no, you've no cannons available!" + RESET);
                    break;

                case "MeteorDestroyed":
                    System.out.println(GREEN + "Out of danger, you've destroyed the meteor!" + RESET);
                    break;

                case "RollDiceToFindColumn":
                    System.out.println("Roll dice to find column (ROLL)");
                    EventCommands.responseRollDice();
                    break;

                case "RollDiceToFindRow":
                    System.out.println("Roll dice to find row (ROLL)");
                    EventCommands.responseRollDice();
                    break;

                case "NothingGotDestroyed":
                    System.out.println(GREEN + "Nothing got destroyed!" + RESET);
                    break;

                case "YouWonBattle":
                    System.out.println(GREEN + "You won against raiders!" + RESET);
                    break;

                case "YouLostBattle":
                    System.out.println(RED + "You lost against raiders!" + RESET);
                    break;

                case "YouDrewBattle":
                    System.out.println(YELLOW + "You drew against raiders!" + RESET);
                    break;

                case "RaidersDefeated":
                    System.out.println(GREEN + "Raiders got defeated!" + RESET);
                    break;

                case "YouGotLapped":
                    System.out.println(PURPLE + "You got lapped by leader, you cannot continue travel!" + RESET);
                    break;

                case "YouHaveNoCrew":
                    System.out.println(PURPLE + "You have no crew left, you cannot continue travel!" + RESET);
                    break;

                case "NoEnginePower":
                    System.out.println(PURPLE + "You have zero engine power in Open Space, you cannot continue travel!" + RESET);
                    break;

                case "IDShipOutOfBounds":
                    System.err.println("Building configuration not present!");
                    break;

                case "AskSelectSpaceshipPart":
                    EventCommands.responseSelectSpaceshipPart();
                    break;

                case "SpaceshipPartKept":
                    System.out.println("Spaceship part kept successfully!");

                default:
                    System.out.println(messageString);
                    break;
            }
        }

        else
            System.err.println("A message was received but is not handled: " + messageObj.toString());
    }
}