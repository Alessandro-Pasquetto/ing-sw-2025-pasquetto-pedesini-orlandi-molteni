package org.progetto.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PageController {

    static Stage stage;
    static ConnetionView connetionView;
    static ChooseGameView chooseGameView;
    static CreateGameView createGameView;
    static GameView gameView;

    static void start() throws IOException {
        switchScene("connection.fxml", "Connection");
        stage.show();
    }

    static void switchScene(String fxmlFile, String title) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));

        Parent root = null;
        if(fxmlLoader.getController() == null){
            root = fxmlLoader.load();

            // Save the controller
            switch (fxmlFile){
                case "connection.fxml":
                    connetionView = fxmlLoader.getController();
                    break;
                case "chooseGame.fxml":
                    chooseGameView = fxmlLoader.getController();
                    break;
                case "createGame.fxml":
                    createGameView = fxmlLoader.getController();
                    break;
                case "game.fxml":
                    gameView = fxmlLoader.getController();
                    break;
            }
        } else {
            root = fxmlLoader.getRoot();
        }

        Scene scene = new Scene(root);

        // Execute this when the GUI thread is ready
        Platform.runLater(() -> {
            stage.setScene(scene);
            stage.setTitle(title);
        });
    }


    static void generateGameList(int id){
        chooseGameView.generateGameList(id);
    }

    static void generateComponent(String imgComponent){
        gameView.generateComponent(imgComponent);
    }
}