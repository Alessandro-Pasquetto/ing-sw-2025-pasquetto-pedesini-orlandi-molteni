package org.progetto.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PageController {

    static Stage stage;
    static ConnetionController connetionController;
    static ChooseGameController chooseGameController;
    static CreateGameController createGameController;
    static GameController gameController;

    static void start() throws IOException {
        switchScene("connection.fxml", "Connection");
        stage.show();
    }

    static void switchScene(String fxmlFile, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));

        Parent root = null;

        if(fxmlLoader.getController() == null){
            root = fxmlLoader.load();

            // Salva il controller
            switch (fxmlFile){
                case "connection.fxml":
                    connetionController = fxmlLoader.getController();
                    break;
                case "chooseGame.fxml":
                    chooseGameController = fxmlLoader.getController();
                    break;
                case "createGame.fxml":
                    createGameController = fxmlLoader.getController();
                    break;
                case "game.fxml":
                    gameController = fxmlLoader.getController();
                    break;
            }
        } else {
            root = fxmlLoader.getRoot();
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
    }
}