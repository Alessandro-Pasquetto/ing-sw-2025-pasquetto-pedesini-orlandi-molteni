package org.progetto.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.progetto.client.gui.PageController;

import java.io.IOException;
import java.util.Scanner;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select TUI/GUI:");

        while(true){
            String command = scanner.nextLine().toUpperCase();

            if(command.equals("TUI")){
                TuiCommandFilter.setProtocol();
                TuiCommandFilter.listenCommand();
                break;

            }else if(command.equals("GUI")){
                PageController.setStage(stage);
                PageController.start();
                break;

            }else
                System.out.println("Command not found");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}