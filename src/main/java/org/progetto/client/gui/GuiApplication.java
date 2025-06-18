package org.progetto.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiApplication extends Application {

    static {
        // Suppress JavaFX warnings
        System.setProperty("java.util.logging.config.file", "logging.properties");
    }

    @Override
    public void start(Stage stage) throws IOException {
        PageController.setStage(stage);

        stage.setOnCloseRequest(event -> {
            System.out.println("Closing GUI...");
            System.exit(0);
        });

        PageController.start();
    }

    public static void main(){
        launch();
    }
}