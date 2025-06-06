package org.progetto.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.gui.PageController;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.TuiCommandFilter;

import java.io.IOException;
import java.util.Scanner;

public class MainClientGui extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        PageController.setStage(stage);

        stage.setOnCloseRequest(event -> {
            System.out.println("Closing GUI...");
            System.exit(0);
        });

        PageController.start();
    }

    public static void main(String[] args) {

        String clientId;
        GameData.setUIType("GUI");

        if (args.length == 0) {
            System.out.println("Default client ID: 0");
            clientId = "0";
        }
        else{
            clientId = args[0];
            System.out.println("Client ID: " + clientId);
            System.out.println();
        }

        GameData.setClientId(clientId);

        GameData.createSaveFile();

        RmiClientSender.setRmiServerDisconnectionDetectionInterval(1000);

        launch();
    }
}