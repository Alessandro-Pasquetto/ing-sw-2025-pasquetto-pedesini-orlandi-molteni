package org.progetto.server.controller.events;

import org.progetto.server.connection.MessageSenderService;
import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;

abstract public class EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    protected GameManager gameManager;
    protected EventPhase phase;

    // =======================
    // GETTERS
    // =======================

    public EventPhase getPhase() {
        return phase;
    }

    // =======================
    // SETTERS
    // =======================

    public void setPhase(EventPhase phase) {
        this.phase = phase;
    }

    // =======================
    // OTHER METHODS
    // =======================

    abstract public void start() throws InterruptedException;

    public void rollDice(Player player, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseHowManyDoubleCannons
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseHowManyDoubleEngines
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender){
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseBatteryToDiscard
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseCrewToDiscard
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseBoxToDiscard
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseChooseToUseShield
    //responseUseDoubleCannonRequest
    public void receiveProtectionDecision(Player player, String response, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseAcceptRewardCreditsAndPenalties
    public void receiveRewardAndPenaltiesDecision(Player player, String response, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseLandRequest
    public void receiveDecisionToLand(Player player, String decision, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseAcceptRewardCreditsAndPenaltyDays
    public void receiveRewardDecision(Player player, String response, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responsePlanetLandRequest
    public void receiveDecisionToLandPlanet(Player player, int planetIdx, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    //responseRewardBox
    public void receiveRewardBox(Player player, int rewardIdxBox, int x, int y, int idx, Sender sender) {
        MessageSenderService.sendMessage("FunctionNotAvailable", sender);
    }

    public void reconnectPlayer(Player player, Sender sender) {

    }
}