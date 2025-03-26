package org.progetto.client.connection.socket;

import org.progetto.client.PageController;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.messages.toServer.CreateGameMessage;
import org.progetto.messages.toServer.JoinGameMessage;
import org.progetto.messages.toServer.PlaceHandComponentAndPickHiddenComponentMessage;
import org.progetto.messages.toServer.PlaceHandComponentAndShowEventCardDeckMessage;

import java.io.*;
import java.net.Socket;

public class SocketClient {
    private static Socket socket;

    public static void connect(String serverIp, int port) {
        try{
            HandlerMessage.setIsSocket(true);

            socket = new Socket(serverIp, port);

            System.out.println("Connected to the socketServer!");

            PageController.switchScene("chooseGame.fxml", "ChooseGame");

            new SocketWriter(new ObjectOutputStream(socket.getOutputStream())).start();
            new SocketListener(new ObjectInputStream(socket.getInputStream())).start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void createNewGame(String name) {
        SocketWriter.sendMessage(new CreateGameMessage(1, 4, name));
    }

    public static void tryJoinToGame(String username, int idGame){
        SocketWriter.sendMessage(new JoinGameMessage(idGame, username));
    }

    public static void startGame(){
        SocketWriter.sendMessage("StartGame");
    }

    public static void pickHiddenComponent(){
        SocketWriter.sendMessage("PickHiddenComponent");
    }

    public static void placeHandComponentAndPickHiddenComponent(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickHiddenComponentMessage(xHandComponent, yHandComponent, rHandComponent));
    }

    public static void discardComponent(){
        SocketWriter.sendMessage("DiscardComponent");
        PageController.gameView.removeHandComponent();
    }

    public static void showEventCardDeck(int idxDeck){

    }

    public static void placeHandComponentAndShowEventCardDeck(int xHandComponent, int yHandComponent, int rHandComponent, int idxDeck) {
        SocketWriter.sendMessage(new PlaceHandComponentAndShowEventCardDeckMessage(xHandComponent, yHandComponent, rHandComponent, idxDeck));
    }


    static void close() throws IOException {
        SocketListener.stopListener();
        SocketWriter.stopWriter();
        socket.close();
        System.out.println("You have disconnected!");

        PageController.switchScene("connection.fxml", "Page1");
    }
}
