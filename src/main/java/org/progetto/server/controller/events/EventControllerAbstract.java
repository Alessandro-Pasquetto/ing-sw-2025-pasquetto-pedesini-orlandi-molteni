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
    public void setPhase(EventPhase phase) throws RemoteException {
        this.phase = phase;
    }

    // =======================
    // OTHER METHODS
    // =======================

    abstract public void start() throws RemoteException, InterruptedException;

    public void rollDice(Player player, Sender sender) throws RemoteException, InterruptedException  {
        System.out.println("UnableToRollDice");
    }
}
