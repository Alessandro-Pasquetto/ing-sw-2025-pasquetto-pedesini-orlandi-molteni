package org.progetto.client.connection.socket;

import org.progetto.client.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.messages.toServer.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Handles the invocation of methods on the server
 */
public class SocketClient {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Socket socket;

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Method to connect to the socket server
     */
    public static void connect(String serverIp, int port) {
        try{
            HandlerMessage.setIsSocket(true);

            if (!isSocketServerReachable(serverIp, port)) {
                System.out.println("Error: The port " + port + " is not available for socket connection");
                return;
            }

            socket = new Socket(serverIp, port);

            System.out.println("Connected to the socketServer!");
            PageController.switchScene("chooseGame.fxml", "ChooseGame");

            new SocketWriter(new ObjectOutputStream(socket.getOutputStream())).start();
            new SocketListener(new ObjectInputStream(socket.getInputStream())).start();

        }catch(IOException e){
            System.out.println("Error connecting to the socket server");
        }
    }

    /**
     * Check if the port is open for a socket communication
     */
    public static boolean isSocketServerReachable(String serverIp, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverIp, port), 200);
            socket.setSoTimeout(200);

            int testByte = socket.getInputStream().read();

            return testByte != -1;
        } catch (IOException e) {
            return false;
        }
    }

    public static void createNewGame() {
        SocketWriter.sendMessage(new CreateGameMessage(1, 4, GameData.getNamePlayer()));
    }

    public static void tryJoinToGame(int idGame){
        SocketWriter.sendMessage(new JoinGameMessage(idGame, GameData.getNamePlayer()));
    }

    public static void startGame(){
        SocketWriter.sendMessage("StartGame");
    }

    public static void pickHiddenComponent(){
        SocketWriter.sendMessage("PickHiddenComponent");
    }

    public static void pickVisibleComponent(){
        SocketWriter.sendMessage("PickVisibleComponent");
    }

    public static void placeHandComponentAndPickHiddenComponent(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickHiddenComponentMessage(xHandComponent, yHandComponent, rHandComponent));
    }

    public static void placeHandComponentAndPickVisibleComponent(int xHandComponent, int yHandComponent, int rHandComponent, int componentIdx) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickVisibleComponentMessage(xHandComponent, yHandComponent, rHandComponent, componentIdx));
    }

    public static void placeHandComponentAndPickUpEventCardDeck(int xHandComponent, int yHandComponent, int rHandComponent, int idxDeck) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickUpEventCardDeckMessage(xHandComponent, yHandComponent, rHandComponent, idxDeck));
    }

    public static void discardComponent(){
        SocketWriter.sendMessage("DiscardComponent");
    }

    public static void pickUpEventCardDeck(int idxDeck){
        SocketWriter.sendMessage(new PickUpEventCardDeckMessage(idxDeck));
    }

    public static void putDownEventCardDeck(){
        SocketWriter.sendMessage("PutDownEventCardDeck");
    }

    public static void destroyComponent(int yComponent, int xComponent){
        SocketWriter.sendMessage(new DestroyComponentMessage(yComponent,xComponent));
    }

    static void playerReady() {
        SocketWriter.sendMessage("Ready");
    }

    static void close() throws IOException {
        SocketListener.stopListener();
        SocketWriter.stopWriter();
        socket.close();
        System.out.println("You have disconnected!");

        PageController.switchScene("connection.fxml", "Page1");
    }
}
