package org.progetto.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.gui.PageController;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.TuiCommandFilter;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println(" ██████╗  █████╗ ██╗      █████╗ ██╗  ██╗██╗   ██╗    ████████╗██████╗ ██╗   ██╗ ██████╗██╗  ██╗███████╗██████╗ ");
        System.out.println("██╔════╝ ██╔══██╗██║     ██╔══██╗╚██╗██╔╝╚██╗ ██╔╝    ╚══██╔══╝██╔══██╗██║   ██║██╔════╝██║ ██╔╝██╔════╝██╔══██╗");
        System.out.println("██║  ███╗███████║██║     ███████║ ╚███╔╝  ╚████╔╝        ██║   ██████╔╝██║   ██║██║     █████╔╝ █████╗  ██████╔╝");
        System.out.println("██║   ██║██╔══██║██║     ██╔══██║ ██╔██╗   ╚██╔╝         ██║   ██╔══██╗██║   ██║██║     ██╔═██╗ ██╔══╝  ██╔══██╗");
        System.out.println("╚██████╔╝██║  ██║███████╗██║  ██║██╔╝ ██╗   ██║          ██║   ██║  ██║╚██████╔╝╚██████╗██║  ██╗███████╗██║  ██║");
        System.out.println(" ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝          ╚═╝   ╚═╝  ╚═╝ ╚═════╝  ╚═════╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝");

        System.out.println();

        System.out.println("Select TUI/GUI:");

        while(true){
            String command = scanner.nextLine().toUpperCase();

            if(command.equals("TUI")){
                GameData.setUIType(command);
                TuiCommandFilter.setProtocol();
                TuiCommandFilter.listenerCommand();
                break;

            }else if(command.equals("GUI")){
                GameData.setUIType(command);
                PageController.setStage(stage);

                stage.setOnCloseRequest(event -> {
                    System.out.println("Closing GUI...");
                    System.exit(0);
                });

                PageController.start();
                break;

            }else
                System.err.println("Command not found");
        }
    }

    public static void main(String[] args) {

        String clientId;

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