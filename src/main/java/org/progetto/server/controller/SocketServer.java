package org.progetto.server.controller;

import org.progetto.server.model.Game;
import org.progetto.server.model.Player;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketServer {

    // =======================
    // ATTRIBUTES
    // =======================

    private static List<PrintWriter> printWriters = new ArrayList<>();
    private static AtomicInteger currentIdGame = new AtomicInteger(0);

    // =======================
    // MAIN
    // =======================

    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server in ascolto sulla porta 8080...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'avvio del server: " + e.getMessage());
        }
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void updateGameList(int idGame) {
        synchronized (printWriters) {
            for (PrintWriter out : printWriters) {
                out.println("C'è un nuovo game");
                out.println(idGame);
            }
        }
    }

    public static void loadGameList() {
        /*
        for (int i = 0; i < GameControllersQueue.getNumWaitingGames(); i++) {

            GameControllersQueue.getGameController(i).getGame().;
        }
         */
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        public Socket getSocket() {
            return socket;
        }
        public PrintWriter getPrintWriter() {
            return out;
        }
        public BufferedReader getBufferedReader() {
            return in;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (printWriters) {
                    printWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if(message.equals("newGame")) {

                        int idGame = currentIdGame.getAndIncrement();
                        int level = Integer.parseInt(in.readLine());
                        GameController gameController = new GameController(idGame, level);

                        String name = in.readLine();
                        Player player = new Player(name, 0);
                        new GameController.ClientHandler(gameController, socket, out, in, player).start();

                        synchronized (printWriters) {
                            printWriters.remove(out);
                        }

                        System.out.println(name + " ha creato il game " + idGame);
                        updateGameList(idGame);

                        return;
                    } else if (message.equals("joinGame")) {

                        int idGame = Integer.parseInt(in.readLine());
                        String name = in.readLine();

                        System.out.println(name + " vuole unirsi al game " + idGame);

                        GameController gameController = GameControllersQueue.getGameController(idGame);
                        Game game = gameController.getGame();


                        if(game.tryAddPlayer(name)){
                            Player player = new Player(name, game.getPlayersSize());
                            new GameController.ClientHandler(gameController, socket, out, in, player).start();

                            synchronized (printWriters) {
                                printWriters.remove(out);
                            }

                            out.println("true");
                            return;
                        }else{
                            out.println("false");
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Errore durante la comunicazione con il client: " + e.getMessage());

                synchronized (printWriters) {
                    printWriters.remove(out);
                }

                try{
                    socket.close();
                }catch(IOException ioe){
                    System.err.println("Errore durante la chiusura della connessione con un client: " + ioe.getMessage());
                }
            }
        }
    }
}
