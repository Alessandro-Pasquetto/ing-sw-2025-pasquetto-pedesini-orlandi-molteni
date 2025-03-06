package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class GameController {

    private ArrayList<PrintWriter> printWriters = new ArrayList<>();
    private Game game;

    GameController(int idGame) {
        this.game = new Game(idGame);

        GameControllersQueue.addGameController(this);
    }

    public Game getGame() {
        return game;
    }

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

    static class ClientHandler extends Thread {
        private GameController gameController;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Player player;

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
                    // Legge ciò che inviano i client collegati al game
                }

            } catch (IOException e) {
                System.err.println("Errore durante la comunicazione con il client: " + e.getMessage());

                gameController.removePrintWriter(out);

                System.out.println(player.getUsername() + " si è disconnesso!");
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
