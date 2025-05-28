package org.progetto.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import java.io.IOException;
import java.util.Objects;
import javafx.scene.text.Font;

/**
 * GUI page controller
 */
public class PageController {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Parent connectionRoot;
    private static Parent chooseGameRoot;
    private static Parent waitingRoomRoot;
    private static Parent buildingRoot;
    private static Parent adjustingRoot;
    private static Parent populatingRoot;
    private static Parent positioningRoot;
    private static Parent eventRoot;
    private static Parent planetsRoot;
    private static Parent travelRoot;
    private static Parent gameOverRoot;

    private static Stage stage;
    private static ConnectionView connectionView;
    private static ChooseGameView chooseGameView;
    private static WaitingRoomView waitingRoomView;
    private static BuildingView buildingView;
    private static AdjustingView adjustingView;
    private static PopulatingView populatingView;
    private static PositioningView positioningView;
    private static NewEventView eventView;
    private static PlanetsView planetsView;
    private static TravelView travelView;
    private static GameOverView gameOverView;

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

    public static BuildingView getBuildingView() {
        return buildingView;
    }

    public static AdjustingView getAdjustingView() {
        return adjustingView;
    }

    public static PopulatingView getPopulatingView() {
        return populatingView;
    }

    public static PositioningView getPositioningView() {
        return positioningView;
    }

    public static NewEventView getEventView() {
        return eventView;
    }

    public static PlanetsView getPlanetsView() {
        return planetsView;
    }

    public static TravelView getTravelView() {
        return travelView;
    }

    public static GameOverView getGameOverView() {
        return gameOverView;
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

    /**
     * Initializes the GUI
     *
     * @author Alessandro
     * @throws IOException if the FXML files cannot be loaded
     */
    public static void start() throws IOException {
        Image icon = new Image(Objects.requireNonNull(MainClient.class.getResourceAsStream("img/game-icon.png")));
        stage.getIcons().add(icon);

        // Loads the custom font
        Font.loadFont(MainClient.class.getResourceAsStream("/org/progetto/client/fonts/title-font.ttf"), 10);

        loadControllers();

        switchScene("connection.fxml", "Connection");
    }

    /**
     * Loads the FXML files and their corresponding controllers
     *
     * @author Alessandro
     * @throws IOException if the FXML files cannot be loaded
     */
    public static void loadControllers() throws IOException {
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
        buildingRoot = loader.load();
        buildingView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("adjustingPage.fxml"));
        adjustingRoot = loader.load();
        adjustingView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("populatingPage.fxml"));
        populatingRoot = loader.load();
        populatingView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("positioningPage.fxml"));
        positioningRoot = loader.load();
        positioningView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("newEventPage.fxml"));
        eventRoot = loader.load();
        eventView = loader.getController();


        loader = new FXMLLoader(MainClient.class.getResource("planetsPage.fxml"));
        planetsRoot = loader.load();
        planetsView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("travelPage.fxml"));
        travelRoot = loader.load();
        travelView = loader.getController();

        loader = new FXMLLoader(MainClient.class.getResource("gameOverPage.fxml"));
        gameOverRoot = loader.load();
        gameOverView = loader.getController();
    }

    /**
     * Switches the scene to the specified FXML file
     *
     * @author Alessandro
     * @param fxmlFile the name of the FXML file
     * @param title    the title of the window
     * @throws IOException if the FXML file cannot be loaded
     */
    public static void switchScene(String fxmlFile, String title) throws IOException {
        Parent root = switch (fxmlFile) {
            case "connection.fxml" -> connectionRoot;
            case "chooseGame.fxml" -> chooseGameRoot;
            case "waitingRoom.fxml" -> waitingRoomRoot;
            case "buildingPage.fxml" -> buildingRoot;
            case "adjustingPage.fxml" -> adjustingRoot;
            case "populatingPage.fxml" -> populatingRoot;
            case "positioningPage.fxml" -> positioningRoot;
            case "newEventPage.fxml" -> eventRoot;
            case "planetsPage.fxml" -> planetsRoot;
            case "travelPage.fxml" -> travelRoot;
            case "gameOverPage.fxml" -> gameOverRoot;

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

        stage.setMaximized(true);

        stage.setScene(scene);
        stage.setTitle(title);

        stage.show();
    }

    /**
     * Initializes the Building view with the specified level and color
     *
     * @author Alessandro
     * @param levelGame the level of the game
     * @param color the color of the spaceship
     */
    public static void initBuilding(int levelGame, int color) {
        BuildingData.initMask(levelGame);
        buildingView.initImages();
        buildingView.initBackground(levelGame);
        buildingView.initSpaceship(levelGame, color);
        buildingView.initTimer(levelGame);
        buildingView.initEventCardDecks(levelGame);
    }

    /**
     * Initializes the Adjusting view with the specific game level
     *
     * @author Gabriele
     * @param levelGame the level of the game
     */
    public static void initAdjusting(int levelGame) {
        BuildingData.initMask(levelGame);
        adjustingView.initBackground(levelGame);
        adjustingView.initSpaceship(levelGame);
    }

    /**
     * Initializes the Populating view with the specific game level
     *
     * @author Stefano
     * @param levelGame the level of the game
     */
    public static void initPopulating(int levelGame) {
        BuildingData.initMask(levelGame);
        populatingView.initBackground(levelGame);
        populatingView.initSpaceship(levelGame);
    }

    /**
     * Initializes the Positioning view with the specific game level
     *
     * @author Gabriele
     * @param levelGame the level of the game
     */
    public static void initPositioning(int levelGame) {
        positioningView.initBackground(levelGame);
        positioningView.initTrack(levelGame);
    }

    /**
     * Initializes the Event view with the specific game level
     *
     * @author Lorenzo
     * @param levelGame the level of the game
     */
    public static void initEvent(int levelGame) {
        BuildingData.initMask(levelGame);
        eventView.initBackground(levelGame);
        eventView.initSpaceship(levelGame);
        eventView.initTrack(levelGame);
        eventView.initEventLabels();
    }

    /**
     * Initializes the Event view for the card Planets with the specific game level
     *
     * @author Lorenzo
     * @param levelGame the level of the game
     */
    public static void initPlanets(int levelGame) {
        BuildingData.initMask(levelGame);
        planetsView.initEvent();
    }

    /**
     * Initializes the Travel view with the specific game level
     *
     * @author Lorenzo
     * @param levelGame the level of the game
     */
    public static void initTravel(int levelGame) {
        travelView.initBackground(levelGame);
        travelView.initTrack(levelGame);
        travelView.initTravelLabels();
    }

    /**
     * Initializes the EndGame view with the specific game level
     *
     * @author Lorenzo
     * @param levelGame the level of the game
     */
    public static void initEndGame(int levelGame) {
        gameOverView.initBackground(levelGame);
    }
}