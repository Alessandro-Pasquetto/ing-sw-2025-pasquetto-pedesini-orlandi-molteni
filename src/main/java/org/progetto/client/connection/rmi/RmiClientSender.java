package org.progetto.client.connection.rmi;

import org.progetto.client.GameData;
import org.progetto.client.gui.PageController;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.server.connection.rmi.VirtualServer;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class RmiClientSender{

    private static VirtualServer server = null;

    /**
     * Method to connect to the RMI server
     */
    public static void connect(String serverIp, int serverPort) {
        try {
            HandlerMessage.setIsSocket(false);

            server = (VirtualServer) Naming.lookup("//" + serverIp + ":" + serverPort + "/VirtualServer");

            server.connect(RmiClientReceiver.getInstance());

            System.out.println("Connected to the RMIServer");
            PageController.switchScene("chooseGame.fxml", "ChooseGame");

        } catch (Exception e) {
            System.out.println("Error connecting to the RMI server");
        }
    }

    // The following methods call functions implemented in the RmiServerReceiver

    public static void createGame() {
        System.out.println("You have created a new game");
        try{
            server.createGame(RmiClientReceiver.getInstance(), GameData.getNamePlayer(), 1, 4);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void tryJoinToGame(int idGame) {
        try {
            server.joinGame(RmiClientReceiver.getInstance(), idGame, GameData.getNamePlayer());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startGame() {
        try {
            server.startGame(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void pickHiddenComponent(){
        try {
            server.pickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void pickVisibleComponent(){
        try {
            server.pickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), -1); // non so dove pescare l'idx :C
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void placeHandComponentAndPickHiddenComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent){
        try {
            server.placeHandComponentAndPickHiddenComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void placeHandComponentAndPickVisibleComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int componentIdx){
        try {
            server.placeHandComponentAndPickVisibleComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, componentIdx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void placeHandComponentAndPickUpEventCardDeck(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int deckIdx){
        try {
            server.placeHandComponentAndPickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, deckIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void placeHandComponentAndPickBookedComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent, int idx){
        try {
            server.placeHandComponentAndPickBookedComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), yPlaceComponent, xPlaceComponent, rPlaceComponent, idx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     */
    public static void discardComponent(){
        try {
            server.discardComponent(RmiClientReceiver.getInstance(), GameData.getIdGame());
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     * @param idx is the index were the component will be placed
     */
    public static void bookComponent(int idx){
        try {
            server.bookComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(), idx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows RMI on client to call for the respective method on RMI server
     *
     * @author Lorenzo
     * @param idx is the index were the component will be picked
     */
    public static void pickBookedComponent(int idx){
        try {
            server.pickBookedComponent(RmiClientReceiver.getInstance(),GameData.getIdGame(),idx);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void pickUpEventCardDeck(int deckIdx) {
        try {
            server.pickUpEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame(), deckIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void putDownEventCardDeck() {
        try {
            server.putDownEventCardDeck(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows client to call for the destroyComponent method on RMI server
     *
     * @author Lorenzo
     * @param yComponent
     * @param xComponent
     */
    public static void destroyComponent(int yComponent, int xComponent){
        try {
            server.destroyComponent(RmiClientReceiver.getInstance(), GameData.getIdGame(),yComponent,xComponent);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readyPlayer() {
        try {
            server.playerReady(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetTimer() {
        try {
            server.resetTimer(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Allows client to call for the pickEventCard method on RMI server
     *
     * @author Lorenzo
     */
    public static void pickEventCard() {
        try {
            server.pickEventCard(RmiClientReceiver.getInstance(),GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void rollDice() {
        try {
            server.rollDice(RmiClientReceiver.getInstance(), GameData.getIdGame());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}