package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TravelView {

    // =======================
    // ATTRIBUTES
    // =======================

    @FXML
    public StackPane trackPane;

    @FXML
    public VBox playerListContainer;

    @FXML
    private Group cellsGroup;

    @FXML
    private ImageView boardImage;

    private List<Rectangle> boardCells = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();

    // =======================
    // METHODS
    // =======================

    /**
     * Initializes the background
     *
     * @author Lorenzo
     * @param levelGame is the game level
     */
    public void initBackground(int levelGame) {

        // Initialize background
        Image img = null;
        if(levelGame == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-1.png")));

        else if(levelGame == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-2.png")));

        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,   // horizontal repetition
                BackgroundRepeat.NO_REPEAT,   // vertical repetition
                BackgroundPosition.CENTER,    // position
                new BackgroundSize(
                        100, 100, true, true, false, true
                )
        );

        Background background = new Background(backgroundImage);
        trackPane.setBackground(background);
    }

    /**
     * Initializes the track
     *
     * @author Lorenzo
     */
    public void initTrack(int levelGame) {
        cellsGroup.getChildren().clear();
        boardCells.clear();

        double[][] cellPositions = null;

        if(levelGame == 1) {

            cellPositions = new double[][]{
                    {233, 112}, {311, 89}, {391, 79}, {472, 78},
                    {550, 88}, {629, 111}, {697, 160}, {736, 248},
                    {693, 325}, {621, 370}, {542, 396}, {461, 409},
                    {382, 409}, {302,395}, {225, 372}, {155, 321},
                    {118, 231}, {159, 157}

            };

        } else if(levelGame == 2){

            cellPositions = new double[][]{
                    {196, 97}, {259, 72}, {325, 60}, {389, 51}, {464, 51},
                    {536, 57}, {597, 73}, {661, 97}, {717, 141}, {758, 207},
                    {757, 285}, {712, 342}, {651, 381}, {586, 407}, {518, 423},
                    {449, 432}, {382, 430}, {313, 423}, {247, 407}, {184, 380},
                    {127, 338}, {85, 270}, {88, 192}, {134, 135}
            };

        }

        for (int i = 0; i < Objects.requireNonNull(cellPositions).length; i++) {
            double x = cellPositions[i][0];
            double y = cellPositions[i][1];

            Rectangle cell = new Rectangle(50, 50);
            cell.setFill(Color.TRANSPARENT);
            cell.setTranslateX(x);
            cell.setTranslateY(y);

            cellsGroup.getChildren().add(cell);
            boardCells.add(cell);
        }

        // Set the board image
        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/board" + levelGame + ".png")));
        boardImage.setImage(image);
    }

    /**
     * Ask the player if he wants to continue travel
     *
     * @author Lorenzo
     */
    public void askToContinue(){
        Alerts.showYesNoPopup(trackPane,"Continue?","Do you want to continue travel?",yesResponse(),noResponse());
    }

    private Runnable yesResponse(){
        return () -> {
            Sender sender = GameData.getSender();
            sender.responseContinueTravel("YES");
        };
    }

    private Runnable noResponse(){
        return () -> {
            Sender sender = GameData.getSender();
            sender.responseContinueTravel("NO");
        };
    }

    /**
     * Initializes the players list
     *
     * @author Gabriele
     * @param players is the list of players
     */
    public void initPlayersList(ArrayList<Player> players) {
        playerListContainer.getChildren().clear();
        this.players = players;

        for (Player player : players) {
            HBox playerBox = createPlayerItem(player, GameData.getActivePlayer());
            playerListContainer.getChildren().add(playerBox);
        }
    }

    /**
     * Update the players list with colors and highlight the active player
     *
     * @author Gabriele
     * @param activePlayer The currently active player's name
     */
    public void highlightsActivePlayer(String activePlayer) {
        playerListContainer.getChildren().clear();

        for (Player player : players) {
            HBox playerBox = createPlayerItem(player, activePlayer);
            playerListContainer.getChildren().add(playerBox);
        }
    }

    /**
     * Creates a player item with consistent styling
     *
     * @author Gabriele
     * @param player The player to create an item for
     * @param activePlayer The currently active player's name (can be null)
     * @return HBox containing the player item
     */
    private HBox createPlayerItem(Player player, String activePlayer) {
        HBox playerBox = new HBox(10);
        playerBox.setAlignment(Pos.CENTER_LEFT);
        playerBox.setPadding(new Insets(8, 12, 8, 12));
        playerBox.getStyleClass().add("player-item");

        Circle colorIndicator = new Circle(8);

        String colorCode;
        switch (player.getColor()) {
            case 0:
                colorCode = "#1E90FF"; // Blue
                break;

            case 1:
                colorCode = "#32CD32"; // Green
                break;

            case 2:
                colorCode = "#FF4444"; // Red
                break;

            case 3:
                colorCode = "#FFD700"; // Yellow
                break;

            default:
                colorCode = "#808080";
        }
        colorIndicator.setFill(Color.web(colorCode));

        Label nameLabel;

        if (player.getName().equals(GameData.getNamePlayer())) {
            nameLabel = new Label(player.getName() + " (You)");
        } else {
            nameLabel = new Label(player.getName());
        }

        nameLabel.setTextFill(Color.WHITE);

        playerBox.getChildren().addAll(colorIndicator, nameLabel);

        // Highlight active player
        if (activePlayer != null && player.getName().equals(activePlayer)) {
            playerBox.setStyle("-fx-border-color: white; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-background-color: rgba(255,255,255,0.2); ");
        } else {
            playerBox.setStyle("-fx-background-color: rgba(0,0,0,0.2); " +
                    "-fx-background-radius: 8;");
        }

        return playerBox;
    }

    /**
     * Updates the track with the current positions of the players
     *
     * @author Lorenzo
     * @param  playersPosition The current positions of the players
     */
    public void updateTrack(Player[]  playersPosition) {
        cellsGroup.getChildren().removeIf(node -> node instanceof ImageView);

        for (int i = 0; i <  playersPosition.length; i++) {
            Player player =  playersPosition[i];
            if (player != null) {
                Rectangle cell = boardCells.get(i);

                String rocketImage = getRocketImagePath(player.getColor());
                Image rocket = new Image(String.valueOf(MainClient.class.getResource(rocketImage)));
                ImageView rocketView = new ImageView(rocket);

                rocketView.setFitWidth(37.5);
                rocketView.setFitHeight(50);
                rocketView.setPreserveRatio(true);

                rocketView.setTranslateX(cell.getTranslateX() + (cell.getWidth() - rocketView.getFitWidth()) / 2);
                rocketView.setTranslateY(cell.getTranslateY() + (cell.getHeight() - rocketView.getFitHeight()) / 2);

                cellsGroup.getChildren().add(rocketView);
            }
        }
    }

    /**
     * Returns the image path of the rocket based on the color
     *
     * @author Gabriele
     * @param color The color of the rocket
     * @return The image path of the rocket
     */
    private String getRocketImagePath(int color) {
        return switch (color) {
            case 0 -> "img/items/blue_pawn.png";
            case 1 -> "img/items/green_pawn.png";
            case 2 -> "img/items/red_pawn.png";
            case 3 -> "img/items/yellow_pawn.png";
            default -> "";
        };
    }

    public void showEventView() throws IOException {
        PageController.initEvent(GameData.getLevelGame());
        PageController.switchScene("gamePage.fxml", "Game");

    }
}