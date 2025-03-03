package org.progetto.server;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.Gson;

public class SocketServer {

    private static List<PrintWriter> printWriters = new ArrayList<>();

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

    public static void updateGameList() {
        synchronized (printWriters) {
            for (PrintWriter out : printWriters) {
                out.println("C'è un nuovo game");
            }
        }
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

                        GameController gameController = new GameController();

                        message = in.readLine();
                        Player player = new Player(message, 0);
                        new GameController.ClientHandler(gameController, socket, out, in, player).start();

                        synchronized (printWriters) {
                            printWriters.remove(out);
                        }

                        System.out.println(message + " ha creato una partita");
                        updateGameList();

                        return;
                    } else if (message.equals("joinGame")) {

                        GameController gameController = GameControllersQueue.getGameController(0);
                        message = in.readLine();
                        System.out.println(message + " sta tentando di aggiungersi ad un game");

                        out.println("SpecificAnsware");

                        if(gameController.getGame().tryAddPlayer(message)){
                            Player player = new Player(message, 0);
                            new GameController.ClientHandler(gameController, socket, out, in, player).start();

                            synchronized (printWriters) {
                                printWriters.remove(out);
                            }

                            out.println(true);
                            return;
                        }else{
                            System.out.println("un player non è potuto aggiungersi ad un game");
                            out.println(false);
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
