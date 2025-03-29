package org.progetto.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.progetto.client.view.ChooseGameView;
import org.progetto.client.view.ConnetionView;
import org.progetto.client.view.CreateGameView;
import org.progetto.client.view.GameView;

import java.io.IOException;
import java.util.Objects;

public class PageController {

    static Stage stage;
    static ConnetionView connetionView;
    static ChooseGameView chooseGameView;
    static CreateGameView createGameView;
    public static GameView gameView;

    static void start() throws IOException {
        Image icon = new Image(Objects.requireNonNull(MainClient.class.getResourceAsStream("img/icon.png")));
        stage.getIcons().add(icon);
        switchScene("connection.fxml", "Connection");
        stage.show();
    }

    public static void switchScene(String fxmlFile, String title) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(MainClient.class.getResource(fxmlFile));

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

    public static void generateGameList(int id){
        chooseGameView.generateGameList(id);
    }

    public static void generateComponent(String imgComponent){
        gameView.generateComponent(imgComponent);
    }

    public static void initGame(String imgSrcBoard, String imgSrcSpaceship, String imgSrcCentralUnit) {
        Platform.runLater(() -> {
            gameView.insertCentralUnitComponent(imgSrcCentralUnit, 1);
            //gameView.loadBoardImg(imgSrcBoard);
            //gameView.loadShipImg(imgSrcSpaceship);
        });
    }
}