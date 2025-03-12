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

    public Game getGame() {
        return game;
    }

    public ArrayList<PrintWriter> getPrintWriters() {
        ArrayList<PrintWriter> writersCopy;

        synchronized (printWriters) {
            writersCopy = new ArrayList<>(printWriters);
        }

        return writersCopy;
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

    public void broadcastMessage(String message) {
        ArrayList<PrintWriter> writersCopy;

        synchronized (printWriters) {
            writersCopy = new ArrayList<>(printWriters);
        }

        for (PrintWriter out : writersCopy) {
            synchronized (out) {
                out.println(message);
            }
        }
    }


    public void startTimer() {
        timer.startTimer();
    }


    // =======================
    // CLIENT HANDLER
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
            String message;
            try {
                gameController.addPrintWriter(out);
                Game game = gameController.getGame();
                game.addPlayer(player);

                while ((message = in.readLine()) != null) {
                    // Reads what the clients connected to the game send

                    switch (game.getPhase()) {
                        case INIT:
                            InitController.handle(gameController::startTimer, gameController::broadcastMessage, game, player, message);
                            break;

                        case BUILDING:
                            if(gameController.timer.getTimer() > 0)
                                BuildingController.handle(gameController::broadcastMessage, this::sendMessageToPlayer, game, player, message);
                            else
                                sendMessageToPlayer("Timer scaduto");
                            break;

                        case TRAVEL:

                            break;

                        case EVENT:

                            break;

                        case ENDGAME:

                            break;

                        default:
                            System.out.println("Fase di gioco inesistente");
                            break;
                    }

                }

            } catch (IOException e) {
                System.err.println("Errore durante la comunicazione con il client: " + e.getMessage());

                gameController.removePrintWriter(out);

                System.out.println(player.getName() + " si è disconnesso!");
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e2) {
                    System.err.println("Errore durante la chiusura della connessione con il client: " + e2.getMessage());
                }
            }
        }

        public void sendMessageToPlayer(String message) {
            synchronized(out) {
                out.println(message);
            }
        }

        public void sendMessageToOtherPlayers(String message) {
            ArrayList<PrintWriter> writersCopy = gameController.getPrintWriters();

            for (PrintWriter out : writersCopy) {
                synchronized (out) {
                    out.println(message);
                }
            }
        }
    }
}