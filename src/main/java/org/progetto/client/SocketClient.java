package org.progetto.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String interceptMessage;
    private static volatile boolean closingConnection = false, enableInterceptMessage = false;
    private static final Object lock = new Object();

    static void connect(String serverIp, int port) {
        try{
            socket = new Socket(serverIp, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connesso al server!");

            PageController.switchScene("chooseGame.fxml", "ChooseGame");

            // Gamelist listener
            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        //todo: Maybe once the JSON messages are implemented, I’ll be able to handle it with switch(typeMessage)
                        if(enableInterceptMessage) {
                            interceptMessage = message;
                            enableInterceptMessage = false;
                            synchronized (lock) {
                                lock.notify();
                            }
                        }else{
                            if(message.equals("C'è un nuovo game"))
                                gameListListener(message);
                            else{
                                if(message.startsWith("P#")){
                                    int index = message.indexOf(": ");
                                    String result = message.substring(index + 2); // "+2" perché ": " ha lunghezza 2
                                    System.out.println(result);
                                    PageController.generateComponent(result);
                                }else
                                    System.out.println(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    if(!closingConnection){
                        e.printStackTrace();
                        System.out.println("Errore: il server non è più raggiungibile, sei stato disconnesso");
                    }
                }
            }).start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void gameListListener(String message) {
        System.out.println(message);
        try {
            message = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int idGame = Integer.parseInt(message);

        PageController.generateGameList(idGame);
    }

    /*
    // Method to send messages in JSON format
    static void sendJsonMessage(NetworkMessageType type, Object obj) {
        NetworkMessage message = new NetworkMessage(type, obj);

        Gson gson = new Gson();
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
    }

    public static void sendName(String clientName) {
        out.println(clientName);
    }
     */

    public static void createNewGame(String username) {

        out.println("newGame");
        out.println(1);
        out.println(4);
        out.println(username);

        System.out.println("Hai creato una partita");
        try {
            PageController.switchScene("game.fxml", "Game");
        } catch (IOException e) {
            System.out.println("Errore nel caricamente della pagina");
        }
    }

    public static void joinToGame(String username, int idGame){
        out.println("joinGame");
        out.println(idGame);
        out.println(username);

        enableInterceptMessage = true;

        while(enableInterceptMessage) {

            synchronized (lock){
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String response = interceptMessage;

        if(response.equals("true")){
            System.out.println("Ti sei unito ad un game");

            try {
                PageController.switchScene("game.fxml", "Game");
            } catch (IOException e) {
                System.out.println("Errore nel caricamente della pagina");
            }
        }else{
            System.out.println("In quella partita c'è già un giocatore con lo stesso nome");
        }
    }

    public static void startGame(){
        out.println("StartGame");
    }

    public static void pickComponent(){
        out.println("PickComponent");
    }

    static void close() throws IOException {
        closingConnection = true;

        socket.close();
        out.close();
        in.close();
        System.out.println("Ti sei disconnesso!");

        PageController.switchScene("connection.fxml", "Page1");
    }
}
