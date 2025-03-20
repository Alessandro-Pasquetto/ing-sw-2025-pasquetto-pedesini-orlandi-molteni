package org.progetto.server.controller;

import org.progetto.server.model.Game;

import java.util.ArrayList;

class GameController{

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<SocketWriter> socketWriters = new ArrayList<>();
    private final Game game;
    private final TimerController timer;

    // =======================
    // CONSTRUCTORS
    // =======================

    GameController(int idGame, int numPlayers, int level) {
        this.game = new Game(idGame, numPlayers, level);
        this.timer = new TimerController(this::broadcastMessage,60,3);
        GameControllersQueue.addGameController(this);
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

    public void broadcastMessage(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy;

        synchronized (socketWriters) {
            socketWritersCopy = new ArrayList<>(socketWriters);
        }

        for (SocketWriter sk : socketWritersCopy) {
            sk.sendMessage(messageObj);
        }
    }

    public void startTimer() {
        timer.startTimer();
    }

}