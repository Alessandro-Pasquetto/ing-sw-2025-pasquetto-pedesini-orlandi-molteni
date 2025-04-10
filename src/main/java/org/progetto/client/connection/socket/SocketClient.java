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

            new SocketWriter(new ObjectOutputStream(socket.getOutputStream())).start();
            new SocketListener(new ObjectInputStream(socket.getInputStream())).start();

            if(GameData.getUIType().equals("GUI"))
                PageController.switchScene("chooseGame.fxml", "ChooseGame");

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
        SocketWriter.sendMessage(new CreateGameMessage(1, 2, GameData.getNamePlayer()));
    }

    @Override
    public void tryJoinToGame(int idGame){
        SocketWriter.sendMessage(new JoinGameMessage(idGame, GameData.getNamePlayer()));
    }

    @Override
    public void pickHiddenComponent(){
        SocketWriter.sendMessage("PickHiddenComponent");
    }

    @Override
    public void pickVisibleComponent(int idx){
        SocketWriter.sendMessage(new PickVisibleComponentMessage(idx));
    }

    @Override
    public void placeLastComponent(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceLastComponent(xHandComponent, yHandComponent, rHandComponent));
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
    public void placeHandComponentAndReady(int xHandComponent, int yHandComponent, int rHandComponent) {
        SocketWriter.sendMessage(new PlaceHandComponentAndReadyMessage(xHandComponent, yHandComponent, rHandComponent));
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
    public void destroyComponent(int yComponent, int xComponent) {
        SocketWriter.sendMessage(new DestroyComponentMessage(yComponent, xComponent));
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
    public void showSpaceship(String owner){
        SocketWriter.sendMessage(new RequestSpaceshipMessage(owner));
    }


    @Override
    public void close() {
        SocketListener.stopListener();
        SocketWriter.stopWriter();
        try {
            socket.close();
            System.out.println("You have disconnected!");
            if(GameData.getUIType().equals("GUI"))
                PageController.switchScene("connection.fxml", "Page1");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseHowManyDoubleCannons(int howManyWantToUse) {
        SocketWriter.sendMessage(new ResponseHowManyDoubleCannonsMessage(howManyWantToUse));
    }

    @Override
    public void responseHowManyDoubleEngines(int howManyWantToUse) {
        SocketWriter.sendMessage(new ResponseHowManyDoubleEnginesMessage(howManyWantToUse));
    }

    @Override
    public void responseBatteryToDiscard(int xBatteryStorage, int yBatteryStorage) {
        SocketWriter.sendMessage(new ResponseBatteryToDiscardMessage(xBatteryStorage, yBatteryStorage));
    }

    @Override
    public void responseCrewToDiscard(int xHousingUnit, int yHousingUnit) {
        SocketWriter.sendMessage(new ResponseCrewToDiscardMessage(xHousingUnit, yHousingUnit));
    }

    @Override
    public void responseBoxToDiscard(int xBoxStorage, int yBoxStorage, int idx) {
        SocketWriter.sendMessage(new ResponseBoxToDiscardMessage(xBoxStorage, yBoxStorage, idx));
    }

    @Override
    public void responseChooseToUseShield(String response) {
        SocketWriter.sendMessage(new ResponseChooseToUseShieldMessage(response));
    }

    @Override
    public void responseAcceptRewardCreditsAndPenalties(String response) {
        SocketWriter.sendMessage(new ResponseAcceptRewardCreditsAndPenaltiesMessage(response));
    }

    @Override
    public void responseLandRequest(String response) {
        SocketWriter.sendMessage(new ResponseLandRequestMessage(response));
    }

    @Override
    public void responseAcceptRewardCreditsAndPenaltyDays(String response) {
        SocketWriter.sendMessage(new ResponseAcceptRewardCreditsAndPenaltyDaysMessage(response));
    }

    @Override
    public void responsePlanetLandRequest(String response, int idx) {
        SocketWriter.sendMessage(new ResponsePlanetLandRequestMessage(response, idx));
    }

    @Override
    public void responseRewardBox(int idxBox, int xBoxStorage, int yBoxStorage, int idx) {
        SocketWriter.sendMessage(new ResponseRewardBoxMessage(idxBox, xBoxStorage, yBoxStorage, idx));
    }

    @Override
    public void responseUseDoubleCannonRequest(String response) {
        SocketWriter.sendMessage(new ResponseUseDoubleCannonRequestMessage(response));
    }
}
