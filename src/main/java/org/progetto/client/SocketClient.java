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
    private static volatile boolean closingConnection = false, stopReading = false;

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
                    while (!stopReading && (message = in.readLine()) != null) {

                        if(message.equals("SpecificAnsware")) {
                            stopReading = true;
                        }else{
                            System.out.println(message);
                        }
                        //PageController.page2Controller.addMessageToUI(message);
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

    /*
    // Metodo per inviare messaggi in formato JSON
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
        out.println(username);

        System.out.println("Hai creato una partita");
        try {
            PageController.switchScene("game.fxml", "Game");
        } catch (IOException e) {
            System.out.println("Errore nel caricamente della pagina");
        }
    }

    public static void JoinToGame(String username){
        out.println("joinGame");
        out.println(username);


        String response;
        try {
            response = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopReading = false;

        System.out.println("s: " + response);

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

    static void close() throws IOException {
        closingConnection = true;

        socket.close();
        out.close();
        in.close();
        System.out.println("Ti sei disconnesso!");

        PageController.switchScene("connection.fxml", "Page1");
    }
}
