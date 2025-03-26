package org.progetto.client.connection.rmi;

import org.progetto.client.PageController;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.server.connection.rmi.VirtualServer;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class RmiClientSender{

    private static VirtualServer server = null;
    private static String namePlayer = "";

    public static void connect(String serverIp, int serverPort) {
        try {
            HandlerMessage.setIsSocket(false);

            server = (VirtualServer) Naming.lookup("//" + serverIp + ":" + serverPort + "/VirtualServer");

            server.connect(RmiClientReceiver.getInstance());

            System.out.println("Connected to the RMIServer");
            PageController.switchScene("chooseGame.fxml", "ChooseGame");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createGame(String name) {
        System.out.println("You have created a new game");
        try{
            namePlayer = name;
            server.createGame(RmiClientReceiver.getInstance(), name, 1, 4);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void tryJoinToGame(String name, int idGame) {
        try {
            namePlayer = name;
            server.joinGame(RmiClientReceiver.getInstance(), idGame, name);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startGame() {
        try {
            server.startGame(RmiClientReceiver.getInstance());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void pickHiddenComponent(){
        try {
            server.pickHiddenComponent(RmiClientReceiver.getInstance(), namePlayer);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void placeHandComponentAndPickHiddenComponent(int yPlaceComponent, int xPlaceComponent, int rPlaceComponent){
        try {
            server.placeHandComponentAndPickHiddenComponent(RmiClientReceiver.getInstance(), namePlayer, yPlaceComponent, xPlaceComponent, rPlaceComponent);
        }catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }
}