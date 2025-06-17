package org.progetto.server.controller.events;

import org.progetto.messages.toClient.ActivePlayerMessage;
import org.progetto.messages.toClient.EventGeneric.*;
import org.progetto.messages.toClient.Spaceship.UpdateSpaceshipMessage;
import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.Slavers;
import java.util.ArrayList;

public class SlaversController extends EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Slavers slavers;
    private final ArrayList<Player> activePlayers;
    private boolean defeated;
    private float playerFirePower;
    private int requestedBatteries;
    private int requestedCrew;

    private final ArrayList<BatteryStorage> batteryStorages;

    // =======================
    // CONSTRUCTORS
    // =======================

    public SlaversController(GameManager gameManager) {
        this.gameManager = gameManager;
        this.slavers = (Slavers) gameManager.getGame().getActiveEventCard();
        this.phase = EventPhase.START;
        this.activePlayers = gameManager.getGame().getBoard().getCopyTravelers();
        this.defeated = false;
        this.playerFirePower = 0;
        this.requestedBatteries = 0;
        this.requestedCrew = 0;
        this.batteryStorages = new ArrayList<>();
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Starts event card effect
     *
     * @author Stefano
     */
    @Override
    public void start() throws InterruptedException {
        if (phase.equals(EventPhase.START)) {
            phase = EventPhase.ASK_CANNONS;
            askHowManyCannonsToUse();
        }
    }

    @Override
    public boolean isParticipant(Player player){
        return activePlayers.contains(player);
    }

    /**
     * Asks current player how many double cannons he wants to use
     *
     * @author Stefano
     */
    private void askHowManyCannonsToUse() throws InterruptedException {
        if (!phase.equals(EventPhase.ASK_CANNONS))
            throw new IllegalStateException("IncorrectPhase");

        for (Player player : activePlayers) {

            // Checks if card got defeated
            if (defeated) {
                gameManager.broadcastGameMessage("RaidersDefeated");
                break;
            }

            gameManager.getGame().setActivePlayer(player);
            gameManager.broadcastGameMessage(new ActivePlayerMessage(player.getName()));

            // Retrieves sender reference
            Sender sender = gameManager.getSenderByPlayer(player);

            Spaceship spaceship = player.getSpaceship();

            playerFirePower = spaceship.getNormalShootingPower();

            // Calculates max number of double cannons usable
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();

            // If he can use any double cannon, and he doesn't win with normalShootingPower
            if (maxUsable != 0 && slavers.battleResult(playerFirePower) != 1) {
                batteryStorages.clear();

                phase = EventPhase.CANNON_NUMBER;
                MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if (!player.getIsReady()){
                    playerFirePower = spaceship.getNormalShootingPower();
                }
            }

            battleResult(player);
        }

        // Reset activePlayer
        gameManager.getGame().setActivePlayer(null);
    }

    /**
     * Receives numbers of double cannons to use
     *
     * @author Stefano
     * @param player current player
     * @param num number of double cannons player want to use
     * @param sender current player
     */
    @Override
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) {
        if (!phase.equals(EventPhase.CANNON_NUMBER)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Spaceship spaceship = player.getSpaceship();

        // Player doesn't want to use double cannons
        if (num == 0) {
            playerFirePower = spaceship.getNormalShootingPower();

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else if (num <= (spaceship.getFullDoubleCannonCount() + spaceship.getHalfDoubleCannonCount()) && num <= player.getSpaceship().getBatteriesCount() && num > 0) {
            requestedBatteries = num;

            // Updates player's firepower based on his decision
            if (num <= spaceship.getFullDoubleCannonCount()) {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * num;
            } else {
                playerFirePower = spaceship.getNormalShootingPower() + 2 * spaceship.getFullDoubleCannonCount() + (num - spaceship.getFullDoubleCannonCount());
            }

            phase = EventPhase.DISCARDED_BATTERIES;
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(num), sender);

        } else {
            MessageSenderService.sendMessage("IncorrectNumber", sender);
            int maxUsable = spaceship.maxNumberOfDoubleCannonsUsable();
            MessageSenderService.sendMessage(new HowManyDoubleCannonsMessage(maxUsable, slavers.getFirePowerRequired(), player.getSpaceship().getNormalShootingPower()), sender);
        }
    }

    /**
     * Receives the coordinates of BatteryStorage component from which remove a battery
     *
     * @author Stefano
     * @param player current player
     * @param xBatteryStorage x coordinate of chosen battery storage
     * @param yBatteryStorage y coordinate of chosen battery storage
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_BATTERIES)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xBatteryStorage < 0 || yBatteryStorage < 0 || yBatteryStorage >= spaceshipMatrix.length || xBatteryStorage >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        Component batteryStorageComp = spaceshipMatrix[yBatteryStorage][xBatteryStorage];

        // Checks if component is a battery storage
        if (batteryStorageComp == null || !batteryStorageComp.getType().equals(ComponentType.BATTERY_STORAGE)) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        BatteryStorage batteryStorage = (BatteryStorage) batteryStorageComp;

        if(batteryStorage.getItemsCount() == 0) {
            MessageSenderService.sendMessage("EmptyBatteryStorage", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
            return;
        }

        batteryStorages.add(batteryStorage);
        requestedBatteries--;

        MessageSenderService.sendMessage(new BatteryDiscardedMessage(xBatteryStorage, yBatteryStorage), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerBatteryDiscardedMessage(player.getName(), xBatteryStorage, yBatteryStorage), sender);

        if (requestedBatteries == 0) {

            for (BatteryStorage component : batteryStorages) {
                component.decrementItemsCount(player.getSpaceship(), 1);
            }

            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedBatteries), sender);
        }
    }

    private void battleResult(Player player) throws InterruptedException {

        Sender sender = gameManager.getSenderByPlayer(player);

        switch(slavers.battleResult(playerFirePower)){
            case 1:
                MessageSenderService.sendMessage("YouWonBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerWonBattleMessage(player.getName()), sender);
                defeated = true;

                phase = EventPhase.REWARD_DECISION;
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()), sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);
                break;

            case 0:
                MessageSenderService.sendMessage("YouDrewBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerDrewBattleMessage(player.getName()), sender);
                break;

            case -1:
                MessageSenderService.sendMessage("YouLostBattle", sender);
                gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);

                phase = EventPhase.PENALTY_EFFECT;
                sendPenaltyEffect(sender);

                gameManager.getGameThread().resetAndWaitTravelerReady(player);

                // Update spaceship to remove highlight components
                // For others, it's used to reload the spaceship in case of disconnections while he was discarding.
                gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));

                // If the player is disconnected
                if(!player.getIsReady()) {
                    handleLostBattleDisconnection(player, player.getSpaceship());
                }
                break;
        }
    }

    /**
     * Receives response for rewardPenalty
     *
     * @author Stefano
     * @param player current player
     * @param response player's response
     * @param sender current sender
     */
    public void receiveRewardDecision(Player player, String response, Sender sender) {
        if (!phase.equals(EventPhase.REWARD_DECISION)){
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if current player is active one
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        String upperCaseResponse = response.toUpperCase();

        switch (upperCaseResponse) {
            case "YES":
                phase = EventPhase.EFFECT;
                eventEffect();
                break;

            case "NO":
                player.setIsReady(true, gameManager.getGame());
                gameManager.getGameThread().notifyThread();
                break;

            default:
                MessageSenderService.sendMessage("IncorrectResponse", sender);
                MessageSenderService.sendMessage(new AcceptRewardCreditsAndPenaltyDaysMessage(slavers.getRewardCredits(), slavers.getPenaltyDays()), sender);
                break;
        }
    }

    /**
     * If the player accepted, he receives the reward and loses the penalty days
     *
     * @author Stefano
     */
    private void eventEffect() {
        if (!phase.equals(EventPhase.EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        Player player = gameManager.getGame().getActivePlayer();

        // Event effect applied for single player
        slavers.rewardPenalty(gameManager.getGame().getBoard(), player);

        // Retrieves sender reference
        Sender sender = gameManager.getSenderByPlayer(player);

        MessageSenderService.sendMessage(new PlayerMovedBackwardMessage(slavers.getPenaltyDays()), sender);
        MessageSenderService.sendMessage(new PlayerGetsCreditsMessage(slavers.getRewardCredits()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerMovedBackwardMessage(player.getName(), slavers.getPenaltyDays()), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerGetsCreditsMessage(player.getName(), slavers.getRewardCredits()), sender);

        player.setIsReady(true, gameManager.getGame());
        gameManager.getGameThread().notifyThread();
    }

    /**
     * If the player is defeated he suffers the penalty
     *
     * @author Stefano
     * @param sender current sender
     */
    private void sendPenaltyEffect(Sender sender){
        if (!phase.equals(EventPhase.PENALTY_EFFECT))
            throw new IllegalStateException("IncorrectPhase");

        requestedCrew = slavers.getPenaltyCrew();

        phase = EventPhase.DISCARDED_CREW;
        MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
    }

    /**
     * Receives the coordinates of HousingUnit component from which remove a crew member
     *
     * @author Stefano
     * @param player current player
     * @param xHousingUnit x coordinate of chosen housing unit
     * @param yHousingUnit y coordinate of chosen housing unit
     * @param sender current sender
     */
    @Override
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) {
        if (!phase.equals(EventPhase.DISCARDED_CREW)) {
            MessageSenderService.sendMessage("IncorrectPhase", sender);
            return;
        }

        // Checks if the player that calls the methods is also the current one in the controller
        if (!player.equals(gameManager.getGame().getActivePlayer())) {
            MessageSenderService.sendMessage("NotYourTurn", sender);
            return;
        }

        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        // Checks if component index is correct
        if (xHousingUnit < 0 || yHousingUnit < 0 || yHousingUnit >= spaceshipMatrix.length || xHousingUnit >= spaceshipMatrix[0].length ) {
            MessageSenderService.sendMessage("InvalidCoordinates", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        Component housingUnitComp = spaceshipMatrix[yHousingUnit][xHousingUnit];

        // Checks if component is a housing unit
        if (housingUnitComp == null || (!housingUnitComp.getType().equals(ComponentType.HOUSING_UNIT) && !housingUnitComp.getType().equals(ComponentType.CENTRAL_UNIT))) {
            MessageSenderService.sendMessage("InvalidComponent", sender);
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
            return;
        }

        HousingUnit housingUnit = (HousingUnit) housingUnitComp;

        if(housingUnit.getCrewCount() == 0) {
            MessageSenderService.sendMessage("EmptyHousingUnit", sender);
            MessageSenderService.sendMessage(new BatteriesToDiscardMessage(requestedCrew), sender);
            return;
        }

        slavers.chooseDiscardedCrew(player.getSpaceship(), housingUnit);
        requestedCrew--;

        MessageSenderService.sendMessage(new CrewDiscardedMessage(xHousingUnit, yHousingUnit), sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerCrewDiscardedMessage(player.getName(), xHousingUnit, yHousingUnit), sender);

        if (requestedCrew == 0 || player.getSpaceship().getTotalCrewCount() == 0) {
            player.setIsReady(true, gameManager.getGame());
            gameManager.getGameThread().notifyThread();

        } else {
            MessageSenderService.sendMessage(new CrewToDiscardMessage(requestedCrew), sender);
        }
    }

    private void handleLostBattleDisconnection(Player player, Spaceship spaceship){

        Sender sender = gameManager.getSenderByPlayer(player);

        MessageSenderService.sendMessage("YouLostBattle", sender);
        gameManager.broadcastGameMessageToOthers(new AnotherPlayerLostBattleMessage(player.getName()), sender);
        slavers.randomDiscardCrew(spaceship, requestedCrew);

        // For others, it's used to reload the spaceship in case they got disconnected while it was discarding.
        gameManager.broadcastGameMessage(new UpdateSpaceshipMessage(player.getSpaceship(), player));
    }
}