package org.progetto.server.controller;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketWriter extends Thread {

    private ClientHandler clientHandler;
    private ObjectOutputStream out;
    private BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private boolean running = true;

    public SocketWriter(ClientHandler clientHandler, ObjectOutputStream out) {
        this.clientHandler = clientHandler;
        this.out = out;
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Object messageObj = messageQueue.take();
                out.writeObject(messageObj);
                out.flush();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // =======================
    // GETTERS
    // =======================

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    // =======================
    // SETTERS
    // =======================

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }


    // =======================
    // OTHER METHODS
    // =======================

    public void stopWriter() {
        running = false;
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(Object messageObj) {
        try {
            messageQueue.put(messageObj);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void sendMessageToOtherPlayersiInGame(Object messageObj) {
        ArrayList<SocketWriter> writersCopy = clientHandler.getGameController().getSocketWritersCopy();

        for (SocketWriter sk : writersCopy) {
            sk.sendMessage(messageObj);
        }
    }
}