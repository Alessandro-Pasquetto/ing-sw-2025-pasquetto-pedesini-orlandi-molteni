package org.progetto.server.connection.socket;

import org.progetto.server.connection.Sender;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Socket message writer that sends messages to a single client
 */
public class SocketWriter extends Thread implements Sender {

    private ClientHandler clientHandler;
    private ObjectOutputStream out;
    private LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
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

    @Override
    public synchronized void sendMessage(Object messageObj) {
        try {
            messageQueue.put(messageObj);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void sendMessageToOtherPlayersInGame(Object messageObj) {
        ArrayList<SocketWriter> socketWritersCopy = new ArrayList<>(clientHandler.getGameManager().getSocketWritersCopy());

        for (SocketWriter sw : socketWritersCopy) {
            if(!sw.equals(clientHandler.getSocketWriter()))
                sw.sendMessage(messageObj);
        }
    }
}