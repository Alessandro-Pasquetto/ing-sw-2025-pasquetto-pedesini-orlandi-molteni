package org.progetto.server.connection.socket;

import org.progetto.server.connection.Sender;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SocketWriter implements Sender {

    private final ObjectOutputStream out;

    public SocketWriter(ObjectOutputStream out) {
        this.out = out;
    }

    // =======================
    // OTHER METHODS
    // =======================

    @Override
    public synchronized void sendMessage(Object messageObj) {
        try {
            out.reset();
            out.writeObject(messageObj);
            out.flush();
        } catch (Exception _) {
        }
    }

    @Override
    public void sendPing() {
        sendMessage("Ping");
    }
}