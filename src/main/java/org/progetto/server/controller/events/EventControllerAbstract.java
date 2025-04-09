package org.progetto.server.controller.events;

import org.progetto.server.connection.Sender;
import org.progetto.server.model.Player;

import java.rmi.RemoteException;

abstract public class EventControllerAbstract {

    // =======================
    // GETTERS
    // =======================

    abstract public String getPhase() throws RemoteException;

    public Player getCurrPlayer() throws RemoteException {
        return null;
    }

    // =======================
    // OTHER METHODS
    // =======================

    abstract public void start() throws RemoteException, InterruptedException;

    public void rollDice(Player player, Sender sender) throws RemoteException {
        System.out.println("UnableToRollDice");
    }
}
