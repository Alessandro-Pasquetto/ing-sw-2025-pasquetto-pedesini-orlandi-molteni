package org.progetto.server.controller;

import org.progetto.client.connection.rmi.VirtualView;
import org.progetto.server.connection.socket.SocketWriter;
import org.progetto.server.model.Game;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class GameManager {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<SocketWriter> socketWriters = new ArrayList<>();
    private final ArrayList<VirtualView> rmiClients = new ArrayList<>();
    private final Game game;
    private final TimerController timer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public GameManager(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.timer = new TimerController(this::broadcastGameMessage,40,0);
        GameManagersQueue.addGameManager(this);
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<SocketWriter> getSocketWritersCopy() {
        ArrayList<SocketWriter> socketWritersCopy;

        synchronized (socketWriters) {
            socketWritersCopy = new ArrayList<>(socketWriters);
        }

        return socketWritersCopy;
    }

    public ArrayList<VirtualView> getRmiClientsCopy() {
        ArrayList<VirtualView> rmiClientsCopy;

        synchronized (rmiClients) {
            rmiClientsCopy = new ArrayList<>(rmiClients);
        }

        return rmiClientsCopy;
    }

    public Game getGame() {
        return game;
    }

    public TimerController getTimerObj() {
        return timer;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void addSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters){
            socketWriters.add(socketWriter);
        }
    }

    public void removeSocketWriter(SocketWriter socketWriter){
        synchronized (socketWriters ){
            socketWriters.remove(socketWriter);
        }
    }

    public void addRmiClient(VirtualView rmiClient){
        synchronized (rmiClients){
            rmiClients.add(rmiClient);
        }
    }

    public void removeRmiClient(VirtualView rmiClient){
        synchronized (rmiClients){
            rmiClients.remove(rmiClient);
        }
    }

    public void broadcastGameMessage(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            sw.sendMessage(messageObj);
        }

        ArrayList<VirtualView> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualView vv : rmiClientsCopy) {
                vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastGameMessageToOthers(Object messageObj, SocketWriter swSender, VirtualView vvSender) {
        ArrayList<SocketWriter> socketWritersCopy = getSocketWritersCopy();

        for (SocketWriter sw : socketWritersCopy) {
            if(!sw.equals(swSender))
                sw.sendMessage(messageObj);
        }

        ArrayList<VirtualView> rmiClientsCopy = getRmiClientsCopy();

        try{
            for (VirtualView vv : rmiClientsCopy) {
                if(!vv.equals(vvSender))
                    vv.sendMessage(messageObj);
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTimer() {
        timer.startTimer();
    }
}