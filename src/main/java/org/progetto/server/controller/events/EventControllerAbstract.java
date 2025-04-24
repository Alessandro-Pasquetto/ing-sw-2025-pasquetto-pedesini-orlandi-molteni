package org.progetto.server.controller.events;

import org.progetto.server.connection.Sender;
import org.progetto.server.connection.games.GameManager;
import org.progetto.server.controller.EventPhase;
import org.progetto.server.model.Player;
import java.rmi.RemoteException;

abstract public class EventControllerAbstract {

    // =======================
    // ATTRIBUTES
    // =======================

    protected GameManager gameManager;
    protected EventPhase phase;

    // =======================
    // GETTERS
    // =======================

    public EventPhase getPhase() throws RemoteException {
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

    abstract public void start() throws RemoteException, InterruptedException;

    public void rollDice(Player player, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseHowManyDoubleCannons
    public void receiveHowManyCannonsToUse(Player player, int num, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseHowManyDoubleEngines
    public void receiveHowManyEnginesToUse(Player player, int num, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseBatteryToDiscard
    public void receiveDiscardedBatteries(Player player, int xBatteryStorage, int yBatteryStorage, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseCrewToDiscard
    public void receiveDiscardedCrew(Player player, int xHousingUnit, int yHousingUnit, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseBoxToDiscard
    public void receiveDiscardedBox(Player player, int xBoxStorage, int yBoxStorage, int idx, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseChooseToUseShield
    //responseUseDoubleCannonRequest
    public void receiveProtectionDecision(Player player, String response, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseAcceptRewardCreditsAndPenalties
    public void receiveRewardAndPenaltiesDecision(Player player, String response, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseLandRequest
    public void receiveDecisionToLand(Player player, String decision, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseAcceptRewardCreditsAndPenaltyDays
    public void receiveRewardDecision(Player player, String response, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responsePlanetLandRequest
    public void receiveDecisionToLandPlanet(Player player, int planetIdx, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }

    //responseRewardBox
    public void receiveRewardBox(Player player, int idxBox, int x, int y, int idx, Sender sender) throws RemoteException, InterruptedException {
        sender.sendMessage("FunctionNotAvailable");
    }
}