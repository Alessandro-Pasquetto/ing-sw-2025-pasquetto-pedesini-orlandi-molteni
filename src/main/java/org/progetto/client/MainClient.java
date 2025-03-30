package org.progetto.client;

import javafx.application.Application;
import javafx.stage.Stage;
import org.progetto.client.gui.PageController;

import java.io.IOException;

public class MainClient extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        PageController.setStage(stage);

        PageController.start();
    }

    public static void main(String[] args) {
        launch();
    }
}