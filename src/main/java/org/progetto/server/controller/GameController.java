package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class GameController {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<PrintWriter> printWriters = new ArrayList<>();
    private final Game game;

    // =======================
    // CONSTRUCTORS
    // =======================

    GameController(int idGame, int level) {
        this.game = new Game(idGame, level);

        GameControllersQueue.addGameController(this);
    }

    // =======================
    // GETTERS
    // =======================

    public Game getGame() {
        return game;
    }

    // =======================
    // SETTERS
    // =======================

    public void addPrintWriter(PrintWriter printWriter) {
        synchronized (printWriters) {
            printWriters.add(printWriter);
        }
    }

    public void removePrintWriter(PrintWriter printWriter) {
        synchronized (printWriters) {
            printWriters.remove(printWriter);
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    static class ClientHandler extends Thread {
        private final GameController gameController;
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;
        private final Player player;

        public ClientHandler(GameController gameController, Socket socket, PrintWriter out, BufferedReader in, Player player) {
            this.gameController = gameController;
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.player = player;
        }

        @Override
        public void run() {
            try {
                gameController.addPrintWriter(out);
                gameController.getGame().addPlayer(player);

                String message;
                while ((message = in.readLine()) != null) {
                    // Reads what the clients connected to the game send
                }

            } catch (IOException e) {
                System.err.println("Errore durante la comunicazione con il client: " + e.getMessage());

                gameController.removePrintWriter(out);

                System.out.println(player.getName() + " si è disconnesso!");
                try {
                    in.close();
                    out.close();
                    socket.close();
                }catch (IOException e2) {
                    System.err.println("Errore durante la chiusura della connessione con il client: " + e2.getMessage());
                }
            }
        }
    }
}