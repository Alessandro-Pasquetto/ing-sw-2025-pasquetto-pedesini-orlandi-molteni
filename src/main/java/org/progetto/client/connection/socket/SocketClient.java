package org.progetto.client.connection.socket;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.messages.toServer.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Handles the invocation of methods on the server
 */
public class SocketClient implements Sender {

    // =======================
    // ATTRIBUTES
    // =======================

    private Socket socket;

    public SocketClient() {
        socket = null;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Method to connect to the socket server
     */
    @Override
    public void connect(String serverIp, int port) {
        try{

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
    public boolean isSocketServerReachable(String serverIp, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverIp, port), 200);
            socket.setSoTimeout(200);

            int testByte = socket.getInputStream().read();

            return testByte != -1;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void createGame() {
        SocketWriter.sendMessage(new CreateGameMessage(1, 4, GameData.getNamePlayer()));
    }

    @Override
    public void tryJoinToGame(int idGame){
        SocketWriter.sendMessage(new JoinGameMessage(idGame, GameData.getNamePlayer()));
    }

    @Override
    public void startGame(){
        SocketWriter.sendMessage("StartGame");
    }

    @Override
    public void pickHiddenComponent(){
        SocketWriter.sendMessage("PickHiddenComponent");
    }

    @Override
    public void pickVisibleComponent(){
        SocketWriter.sendMessage("PickVisibleComponent");
    }

    @Override
    public void placeHandComponentAndPickHiddenComponent(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickHiddenComponentMessage(xHandComponent, yHandComponent, rHandComponent));
    }

    @Override
    public void placeHandComponentAndPickVisibleComponent(int xHandComponent, int yHandComponent, int rHandComponent, int componentIdx) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickVisibleComponentMessage(xHandComponent, yHandComponent, rHandComponent, componentIdx));
    }

    @Override
    public void placeHandComponentAndPickUpEventCardDeck(int xHandComponent, int yHandComponent, int rHandComponent, int idxDeck) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickUpEventCardDeckMessage(xHandComponent, yHandComponent, rHandComponent, idxDeck));
    }

    @Override
    public void placeHandComponentAndPickBookedComponent(int xHandComponent, int yHandComponent, int rHandComponent, int idx) {
        SocketWriter.sendMessage(new PlaceHandComponentAndPickBookedComponentMessage(xHandComponent, yHandComponent, rHandComponent, idx));
    }

    @Override
    public void discardComponent(){
        SocketWriter.sendMessage("DiscardComponent");
    }

    @Override
    public void pickUpEventCardDeck(int idxDeck){
        SocketWriter.sendMessage(new PickUpEventCardDeckMessage(idxDeck));
    }

    @Override
    public void putDownEventCardDeck(){
        SocketWriter.sendMessage("PutDownEventCardDeck");
    }

    @Override
    public void destroyComponent(int yComponent, int xComponent){
        SocketWriter.sendMessage(new DestroyComponentMessage(yComponent,xComponent));
    }

    @Override
    public void bookComponent(int idx){
        SocketWriter.sendMessage(new BookComponentMessage(idx));
    }

    @Override
    public void pickBookedComponent(int idx){
        SocketWriter.sendMessage(new PickBookedComponentMessage(idx));
    }

    @Override
    public void readyPlayer() {
        SocketWriter.sendMessage("Ready");
    }

    @Override
    public void resetTimer(){
        SocketWriter.sendMessage("ResetTimer");
    }

    @Override
    public void rollDice() {
        SocketWriter.sendMessage("RollDice");
    }

    @Override
    public void close() {
        SocketListener.stopListener();
        SocketWriter.stopWriter();
        try {
            socket.close();

            System.out.println("You have disconnected!");

            PageController.switchScene("connection.fxml", "Page1");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
