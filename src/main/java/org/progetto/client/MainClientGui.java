package org.progetto.client;

import org.progetto.client.connection.ClientDisconnectionDetection;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.gui.GuiApplication;
import org.progetto.client.model.GameData;

public class MainClientGui {

    public static void main(String[] args) {
        String clientId;
        GameData.setUIType("GUI");

        if (args.length == 0) {
            // System.out.println("Default client ID: 0");
            clientId = "0";
        } else {
            clientId = args[0];
            // System.out.println("Client ID: " + clientId);
            // System.out.println();
        }

        GameData.setClientId(clientId);

        GameData.createSaveFile();

        GuiApplication.main();
    }
}