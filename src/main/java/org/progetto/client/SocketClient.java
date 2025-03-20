package org.progetto.client;

import org.progetto.messages.*;

import java.io.*;
import java.net.Socket;

public class SocketClient {
    private static Socket socket;

    static void connect(String serverIp, int port) {
        try{
            socket = new Socket(serverIp, port);

            System.out.println("Connesso al server!");

            PageController.switchScene("chooseGame.fxml", "ChooseGame");

            new SocketWriter(new ObjectOutputStream(socket.getOutputStream())).start();
            new SocketListener(new ObjectInputStream(socket.getInputStream())).start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void createNewGame(String username) {

        SocketWriter.sendMessage(new CreateGameMessage(1, 4, username));

        System.out.println("You have created a new game");
        try {
            PageController.switchScene("game.fxml", "Game");
        } catch (IOException e) {
            System.out.println("Errore nel caricamente della pagina");
        }
    }

    public static void tryJoinToGame(String username, int idGame){
        SocketWriter.sendMessage(new JoinGameMessage(idGame, username));
    }

    public static void JoinToGame(){
        System.out.println("Ti sei unito ad un game");

        try {
            PageController.switchScene("game.fxml", "Game");
        } catch (IOException e) {
            System.out.println("Errore nel caricamente della pagina");
        }
    }

    public static void startGame(){
        SocketWriter.sendMessage("StartGame");
    }

    public static void placeHandComponentAndPickComponent(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickComponentMessage(xHandComponent, yHandComponent, rHandComponent));
    }

    public static void placeHandComponentAndPickCard(int xHandComponent, int yHandComponent, int rHandComponent, int idxDeck) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickCardMessage(xHandComponent, yHandComponent, rHandComponent, idxDeck));
    }

    public static void pickComponent(){
        SocketWriter.sendMessage("PickComponent");
    }

    static void close() throws IOException {
        SocketListener.stopListener();
        SocketWriter.stopWriter();
        socket.close();
        System.out.println("Ti sei disconnesso!");

        PageController.switchScene("connection.fxml", "Page1");
    }
}
