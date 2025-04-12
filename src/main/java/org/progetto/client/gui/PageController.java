package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.progetto.client.MainClient;

import java.io.IOException;
import java.util.Objects;

/**
 * GUI page controller
 */
public class PageController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Stage stage;
    private static ConnectionView connectionView;
    private static ChooseGameView chooseGameView;
    private static CreateGameView createGameView;
    private static GameView gameView;

    // =======================
    // GETTERS
    // =======================

    public static Stage getStage() {
        return stage;
    }

    public static ConnectionView getConnectionView() {
        return connectionView;
    }

    public static ChooseGameView getChooseGameView() {
        return chooseGameView;
    }

    public static CreateGameView getCreateGameView() {
        return createGameView;
    }

    public static GameView getGameView() {
        return gameView;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setStage(Stage stage) {
        PageController.stage = stage;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void start() throws IOException {
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
                    connectionView = fxmlLoader.getController();
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
            //todo
            //gameView.loadBoardImg(imgSrcBoard);
            //gameView.loadShipImg(imgSrcSpaceship);
        });
    }

    public static void updateTimer(int timer) {
        Platform.runLater(() -> {
            gameView.updateTimer(timer);
        });
    }

    public static void removeHandComponent(){
        Platform.runLater(() -> {
            gameView.removeHandComponent();
        });
    }

    public static void placeLastComponent(){
        Platform.runLater(() -> {
            gameView.placeLastComponent();
        });
    }

    public static void disableDraggableBookedComponents(){
        Platform.runLater(() -> {
            gameView.disableDraggableBookedComponents();
        });
    }
}