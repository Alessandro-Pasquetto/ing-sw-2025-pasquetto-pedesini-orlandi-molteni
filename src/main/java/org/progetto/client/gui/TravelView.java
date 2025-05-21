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
                    {223, 118}, {306, 82}, {389, 69}, {480, 79},
                    {566, 89}, {647, 113}, {723, 167}, {763, 264},
                    {716, 343}, {640, 391}, {555, 419}, {470, 433},
                    {385, 431}, {299, 419}, {215, 393}, {142, 342},
                    {99, 246}, {148, 165}

            };

        }
        else if(levelGame == 2){

            cellPositions = new double[][]{
                    {197, 115}, {259, 95}, {325, 72}, {389, 65}, {467, 61},
                    {536, 71}, {605, 85}, {672, 113}, {717, 159}, {758, 226},
                    {757, 303}, {707, 360}, {770, 280}, {647, 397}, {583, 427},
                    {514, 440}, {446, 449}, {379, 449}, {307, 436}, {242, 420},
                    {179, 397}, {122, 355}, {85, 289}, {86, 211}, {128, 150}
            };

        }


        for (int i = 0; i < Objects.requireNonNull(cellPositions).length; i++) {
            double x = cellPositions[i][0];
            double y = cellPositions[i][1];

            Rectangle cell = new Rectangle();
            cell.setHeight(50);
            cell.setWidth(50);
            cell.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-border-color: white; -fx-background-radius: 50; -fx-border-radius: 50;");
            cell.setFill(Color.TRANSPARENT);
            cell.setLayoutX(x);
            cell.setLayoutY(y);


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
        Sender sender = GameData.getSender();
        sender.responseContinueTravel("YES");
        return null;
    }

    private Runnable noResponse(){
        Sender sender = GameData.getSender();
        sender.responseContinueTravel("NO");
        return null;
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

                rocketView.setTranslateX(cell.getLayoutX());
                rocketView.setTranslateY(cell.getLayoutY());


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
