package org.progetto.client.connection.rmi;

import org.progetto.client.PageController;
import org.progetto.server.connection.rmi.VirtualServer;

import java.io.IOException;
import java.rmi.Naming;

public class RmiClientSender{

    private static VirtualServer server;

    public static void connect(String serverIp, int serverPort) {
        try {
            server = (VirtualServer) Naming.lookup("//" + serverIp + ":" + serverPort + "/VirtualServer");

            server.connect(new RmiClientReceiver());

            PageController.switchScene("chooseGame.fxml", "ChooseGame");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createGame(String name) {

        server.createGame(name);

        System.out.println("You have created a new game");

        try {
            PageController.switchScene("game.fxml", "Game");
        } catch (IOException e) {
            System.out.println("Error loading the page");
        }

    }

}