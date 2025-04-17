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

    private final ObjectOutputStream out;
    private LinkedBlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private boolean running = true;

    public SocketWriter(ObjectOutputStream out) {
        this.out = out;
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    // todo disattivato il threadWriter perchè non inviava i messaggi sequenzialmente dovuto ai ritardi delle serializzazione i messaggi "pesanti" ...
    //  lascio momentaneamente il codice commentato nel caso si dovesse ripristinare
    @Override
    public void run() {
        /*
        while (running) {
            try {

                Object messageObj = messageQueue.take();
                out.reset();
                out.writeObject(messageObj);
                out.flush();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
         */
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
            out.reset();
            out.writeObject(messageObj);
            out.flush();
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }

        /*
        try{
            messageQueue.put(messageObj);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
         */
    }
}