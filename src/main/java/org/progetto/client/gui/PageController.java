package org.progetto.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import java.io.IOException;
import java.util.Objects;

/**
 * GUI page controller
 */
public class PageController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Parent connectionRoot, chooseGameRoot, waitingRoomRoot, gameRoot;

    private static Stage stage;
    private static ConnectionView connectionView;
    private static ChooseGameView chooseGameView;
    private static WaitingRoomView waitingRoomView;
    private static BuildingView buildingView;

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

    public static WaitingRoomView getWaitingRoomView() {
        return waitingRoomView;
    }

    public static BuildingView getGameView() {
        return buildingView;
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
        Image icon = new Image(Objects.requireNonNull(MainClient.class.getResourceAsStream("img/Game_icon.png")));
        stage.getIcons().add(icon);

        loadControllers();

        // Execute this when the GUI thread is ready
        stage.setScene(new Scene(connectionRoot));
        stage.setTitle("Connection");
        stage.setMaximized(true);
        stage.show();
    }

    private static void loadControllers() throws IOException {
        FXMLLoader loader;

        // Load the controllers for each FXML view
        loader = new FXMLLoader(MainClient.class.getResource("connection.fxml"));
        connectionRoot = loader.load();
        connectionView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("chooseGame.fxml"));
        chooseGameRoot = loader.load();
        chooseGameView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("waitingRoom.fxml"));
        waitingRoomRoot = loader.load();
        waitingRoomView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("buildingPage.fxml"));
        gameRoot = loader.load();
        buildingView = loader.getController();
    }

    public static void switchScene(String fxmlFile, String title) throws IOException {
        Parent root = switch (fxmlFile) {
            case "connection.fxml" -> connectionRoot;
            case "chooseGame.fxml" -> chooseGameRoot;
            case "waitingRoom.fxml" -> waitingRoomRoot;
            case "buildingPage.fxml" -> gameRoot;
            default -> null;
        };

        if (stage.getScene() != null && stage.getScene().getRoot() == root)
            return;

        Scene scene;

        if(root.getScene() != null)
            scene = root.getScene();
        else
            scene = new Scene(root);

        // Maximize the window when switching scenes
        double screenWidth = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
        stage.setWidth(screenWidth);
        stage.setHeight(screenHeight);

        stage.setScene(scene);
        stage.setTitle(title);

        stage.show();
    }

    public static void initGame(int levelGame, int color) {
        BuildingData.initMask(levelGame);
        buildingView.initSpaceship(levelGame, color);
        buildingView.initPlayersList();
        // todo
        // gameView.loadBoardImg(imgSrcBoard);
        // gameView.loadShipImg(imgSrcSpaceship);
    }
}
