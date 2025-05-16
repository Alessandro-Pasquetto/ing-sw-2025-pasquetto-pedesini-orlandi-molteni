package org.progetto.client.connection;

import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.*;
import org.progetto.messages.toClient.*;
import org.progetto.messages.toClient.Building.*;
import org.progetto.messages.toClient.EventCommon.*;
import org.progetto.messages.toClient.LostStation.AcceptRewardCreditsAndPenaltiesMessage;
import org.progetto.messages.toClient.OpenSpace.AnotherPlayerMovedAheadMessage;
import org.progetto.messages.toClient.OpenSpace.PlayerMovedAheadMessage;
import org.progetto.messages.toClient.Planets.AnotherPlayerLandedPlanetMessage;
import org.progetto.messages.toClient.Planets.AvailablePlanetsMessage;
import org.progetto.messages.toClient.Populating.AlienPlacedMessage;
import org.progetto.messages.toClient.Populating.AskAlienMessage;
import org.progetto.messages.toClient.Smugglers.AcceptRewardBoxesAndPenaltyDaysMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipMessage;
import org.progetto.messages.toClient.Spaceship.ResponseSpaceshipStatsMessage;
import org.progetto.server.controller.BuildingController;
import org.progetto.server.model.Player;

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

        if (messageObj instanceof ShowWaitingGamesMessage showWaitingGamesMessage) {
            TuiPrinters.printWaitingGames(showWaitingGamesMessage.getWaitingGames());
        }

        else if (messageObj instanceof GameInfoMessage initGameMessage) {
            System.out.println("You joined a game:");
            System.out.printf ("│ ID: %d %n", initGameMessage.getIdGame());
            System.out.printf ("│ Level: %d %n", initGameMessage.getLevelGame());

            GameData.setIdGame(initGameMessage.getIdGame());
            GameData.setLevelGame(initGameMessage.getLevelGame());
        }

        else if (messageObj instanceof ShowWaitingPlayersMessage showWaitingPlayersMessage) {
            for (Player player : showWaitingPlayersMessage.getPlayers()) {
                if(player.getName().equals(GameData.getNamePlayer())) {
                    GameData.setColor(player.getColor());
                }
            }
        }

        else if (messageObj instanceof NewGamePhaseMessage newGamePhaseMessage) {
            System.out.println();
            System.out.println(newGamePhaseMessage.getPhaseGame() + " phase started...");
            GameData.setPhaseGame(newGamePhaseMessage.getPhaseGame());
        }

        else if (messageObj instanceof ResponseSpaceshipMessage responseSpaceshipMessage) {
            TuiPrinters.printSpaceship(responseSpaceshipMessage.getOwner().getName(), responseSpaceshipMessage.getSpaceship(), responseSpaceshipMessage.getOwner().getColor());
        }

        else if (messageObj instanceof ResponseSpaceshipStatsMessage responseSpaceshipStatsMessage) {
            TuiPrinters.printSpaceshipStats(responseSpaceshipStatsMessage.getSpaceship());
        }

        else if (messageObj instanceof ResponsePlayerStatsMessage responsePlayerStatsMessage) {
            TuiPrinters.printPlayerStats(responsePlayerStatsMessage.getPlayerName(), responsePlayerStatsMessage.getCredits(), responsePlayerStatsMessage.getPosition(), responsePlayerStatsMessage.getHasLeft());
        }

        else if (messageObj instanceof ResponseTrackMessage responseTrackMessage) {
            TuiPrinters.printTrack(responseTrackMessage.getTravelers(), responseTrackMessage.getTrack());
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
            System.out.println(anotherPlayerBookedComponentMessage.getNamePlayer() + " booked a component at " + anotherPlayerBookedComponentMessage.getBookedIndex());
        }

        else if (messageObj instanceof TimerMessage timerMessage) {
            int timer = timerMessage.getTime();
            if (timer == 10)
                System.out.println("10 seconds to the end");
            else if(timer == 0)
                System.out.println("Timer is at 0s");
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

        else if (messageObj instanceof AskStartingPositionMessage askStartingPositionMessage) {
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
            System.out.println("You have moved ahead of " + playerMovedAheadMessage.getStepsCount() + " positions");
        }

        else if(messageObj instanceof AnotherPlayerMovedAheadMessage anotherPlayerMovedAheadMessage) {
            System.out.println(anotherPlayerMovedAheadMessage.getNamePlayer() + " have moved ahead of " + anotherPlayerMovedAheadMessage.getStepsCount() + " positions");
        }

        else if(messageObj instanceof PlayerMovedBackwardMessage playerMovedBackwardMessage) {
            System.out.println("You have moved backward of " + playerMovedBackwardMessage.getStepsCount() + " positions");
        }

        else if(messageObj instanceof AnotherPlayerMovedBackwardMessage anotherPlayerMovedBackwardMessage) {
            System.out.println(anotherPlayerMovedBackwardMessage.getNamePlayer() + " have moved backward of " + anotherPlayerMovedBackwardMessage.getStepsCount() + " positions");
        }

        else if(messageObj instanceof PlayerGetsCreditsMessage playerGetsCreditsMessage) {
            System.out.println("You received " + playerGetsCreditsMessage.getCredits() + " credits");
        }

        else if(messageObj instanceof AnotherPlayerGetsCreditsMessage anotherPlayerGetsCreditsMessage) {
            System.out.println(anotherPlayerGetsCreditsMessage.getNamePlayer() + " received " + anotherPlayerGetsCreditsMessage.getCredits() + " credits");
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
            System.out.println(playerLeftMessage.getPlayerName() + " left travel");
        }

        else if(messageObj instanceof PlayerDefeatedMessage playerDefeatedMessage) {
            System.out.println(playerDefeatedMessage.getPlayerName() + " was defeated by !");
        }

        else if(messageObj instanceof ScoreBoardMessage scoreBoardMessage) {
            TuiPrinters.printScoreBoard(scoreBoardMessage.getScoreBoard());
        }


        else if (messageObj instanceof String messageString) {

            switch (messageString) {
                case "UpdateGameList":
                    break;

                case "NotValidGameId":
                    System.out.println("Not valid game ID!");
                    break;

                case "NotAvailableName":
                    System.out.println("Username not available!");
                    break;

                case "HandComponentDiscarded":
                    System.out.println("Current hand component discarded");
                    break;

                case "FullHandComponent":
                    System.out.println("Hand is full!");
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

                case "CannotPickUpEventCardDeck":
                    System.out.println("You cannot pick up the event card deck!");
                    break;

                case "TimerExpired":
                    System.out.println("TimerExpired");
                    BuildingData.setIsTimerExpired(true);
                    break;

                case "YouAreReady":
                    System.out.println("You are ready");
                    break;

                case "ActionNotAllowedInReadyState":
                    System.out.println("Action not allowed in ready state!");
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

                case "LandingCompleted":
                    System.out.println("Landing completed!");
                    break;

                case "NotYourTurn":
                    System.out.println("Not your turn!");
                    break;

                case "IncorrectPhase":
                    System.out.println("Can't use that, incorrect phase!");
                    break;

                case "NotValidCoordinates":
                    System.out.println("Invalid coordinates!");
                    break;

                case "EmptyHandComponent":
                    System.out.println("You can't place with an empty hand, draw first!");
                    break;

                case "IllegalIndexEventCardDeck":
                    System.out.println("You have only 3 decks, choose a valid index!");
                    break;

                case "NotEnoughBatteries":
                    System.out.println("Not enough batteries!");
                    break;

                case "FullHandEventDeck":
                    System.out.println("You first need to put down the deck you are looking at! ");
                    break;

                case "InvalidCoordinates":
                    System.out.println("Invalid coordinates!");
                    break;

                case "InvalidComponent":
                    System.out.println("Invalid component!");
                    break;

                case "BookedCellOccupied":
                    System.out.println("You have already a component in that cell!");
                    break;

                case "IllegalBookIndex":
                    System.out.println("You only have 2 cells, choose a valid index!");
                    break;

                case "ImpossibleToDestroyCentralUnit":
                    System.out.println("Impossible to destroy central unit!");
                    break;

                case "ImpossibleToDestroyCorrectlyPlaced":
                    System.out.println("Impossible to destroy a correctly placed component!");
                    break;

                case "BatteryDiscarded":
                    System.out.println("Battery discarded");
                    break;

                case "BatteryNotDiscarded":
                    System.out.println("Unable to discard the battery!");
                    break;

                case "IncorrectNumber":
                    System.out.println("Incorrect number!");
                    break;

                case "NotEnoughBoxes":
                    System.out.println("Not enough boxes!");
                    break;

                case "NotEnoughCrew":
                    System.out.println("Not enough crew!");
                    break;

                case "CrewMemberDiscarded":
                    System.out.println("Crew member discarded");
                    break;

                case "CrewMemberNotDiscarded":
                    System.out.println("Unable to discard the crew member!");
                    break;

                case "BoxDiscarded":
                    System.out.println("Box discarded");
                    break;

                case "BoxNotDiscarded":
                    System.out.println("Unable to discard the box!");
                    break;

                case "BoxChosen":
                    System.out.println("Box placed correctly");
                    break;

                case "BoxNotChosen":
                    System.out.println("Unable to place selected box in that position!");
                    break;

                case "NotValidBoxContainer":
                    System.out.println("The box storage is not correct for that type of box!");
                    break;

                case "EmptyReward":
                    System.out.println("No reward boxes left!");
                    break;

                case "YouAreSafe":
                    System.out.println("You are safe");
                    break;

                case "NotValidSpaceShip":
                    System.out.println("Your spaceship is trash, fix it!");
                    break;

                case "ValidSpaceShip":
                    System.out.println("Your spaceship is pretty good, you're ready to go!");
                    break;

                case "StartingPositionSet":
                    System.out.println("Starting position set successfully!");
                    break;

                case "AskContinueTravel":
                    EventCommands.responseContinueTravel();
                    break;

                case "YouLeftTravel":
                    System.out.println("You left travel");
                    break;

                case "YouAreContinuingTravel":
                    System.out.println("You are continuing travel");
                    break;

                case "NoComponentHit":
                    System.out.println("What a luck, no component hit!");
                    break;

                case "NoComponentDamaged":
                    System.out.println("Close call, no component damaged!");
                    break;

                case "NoShieldAvailable":
                    System.out.println("Oh no, you've no shield available!");
                    break;

                case "NoCannonAvailable":
                    System.out.println("Oh no, you've no cannons available!");
                    break;

                case "MeteorDestroyed":
                    System.out.println("Out of danger, you've destroyed the meteor!");
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
                    System.out.println("Nothing got destroyed!");
                    break;

                case "YouWon":
                    System.out.println("You won against raiders!");
                    break;

                case "YouLost":
                    System.out.println("You lost against raiders!");
                    break;

                case "YouDrew":
                    System.out.println("You drew against raiders!");
                    break;

                case "RaidersDefeated":
                    System.out.println("Raiders got defeated!");
                    break;

                case "YouGotLapped":
                    System.out.println("You got lapped by leader, you cannot continue travel!");
                    break;

                case "YouHaveNoCrew":
                    System.out.println("You have no crew left, you cannot continue travel!");
                    break;

                case "NoEnginePower":
                    System.out.println("You have zero engine power in Open Space, you cannot continue travel!");
                    break;

                case "IDShipOutOfBounds":
                    System.out.println("Building configuration not present!");
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
            System.out.println("A message was received but is not handled: " + messageObj.toString());
    }
}