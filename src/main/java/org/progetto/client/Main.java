package org.progetto.client;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        PageController.stage = stage;
        PageController.start();
    }

    public static void main(String[] args) {
        launch();
    }
}