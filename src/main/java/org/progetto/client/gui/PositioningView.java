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
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PositioningView {

    // =======================
    // ATTRIBUTES
    // =======================

    @FXML
    public StackPane positioningPane;

    @FXML
    public VBox playerListContainer;

    @FXML
    public Label positioningSectionTitle;

    @FXML
    public Label positioningSectionDesc;

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
        positioningPane.setBackground(background);
    }

    /**
     * Initializes the track
     *
     * @author Gabriele
     */
    public void initTrack(int levelGame) {
        cellsGroup.getChildren().clear();
        boardCells.clear();

        // Position 1
        Rectangle cell1 = createTrackCell();
        cell1.setTranslateX(599);
        cell1.setTranslateY(72);

        // Position 2
        Rectangle cell2 = createTrackCell();
        cell2.setTranslateX(393);
        cell2.setTranslateY(50);

        // Position 3
        Rectangle cell3 = createTrackCell();
        cell3.setTranslateX(262);
        cell3.setTranslateY(73);

        // Position 4
        Rectangle cell4 = createTrackCell();
        cell4.setTranslateX(196);
        cell4.setTranslateY(97);

        // Add all cells to the group and list
        cellsGroup.getChildren().addAll(cell1, cell2, cell3, cell4);
        boardCells.addAll(Arrays.asList(cell1, cell2, cell3, cell4));

        // Set the board image
        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/board" + levelGame + ".png")));
        boardImage.setImage(image);
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
     * Updates the track with the starting positions of the players
     *
     * @author Gabriele
     * @param startingPositions The starting positions of the players
     */
    public void updateTrack(Player[] startingPositions) {
        cellsGroup.getChildren().removeIf(node -> node instanceof ImageView);

        for (int i = 0; i < startingPositions.length; i++) {
            Player player = startingPositions[i];
            if (player != null) {
                Rectangle cell = boardCells.get(i);

                cell.getStyleClass().add("selected");

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

    /**
     * Creates a track cell
     *
     * @author Gabriele
     * @return the created Rectangle cell
     */
    private Rectangle createTrackCell() {
        Rectangle cell = new Rectangle(50, 50);
        cell.setFill(Color.TRANSPARENT);
        cell.setCursor(Cursor.HAND);
        cell.setOnMouseClicked(event -> reservePosition(cell));

        return cell;
    }

    /**
     * Reserves a position on the track
     *
     * @author Gabriele
     * @param cell The cell to reserve
     */
    private void reservePosition(Rectangle cell) {
        if (cell.getStyleClass().contains("selected")) {
            Alerts.showError("Position already taken!", true);
            return;
        }

        if (!GameData.getNamePlayer().equals(GameData.getActivePlayer())) {
            Alerts.showError("It's not your turn!", true);
            return;
        }

        int positionIndex = boardCells.indexOf(cell);
        if (positionIndex != -1) {
            GameData.getSender().responseStartingPosition(positionIndex);
        }
    }

    /**
     * Updates the labels based on the player's turn
     *
     * @author Gabriele
     * @param isHisTurn True if it's the player's turn, false otherwise
     */
    public void updateLabels(boolean isHisTurn) {
        if (isHisTurn) {
            positioningSectionTitle.setText("IN WHICH POSITION DO YOU WANT TO START?");
            positioningSectionDesc.setText("Select your starting position by clicking on a cell on the track...");
        } else {
            positioningSectionTitle.setText("YOU CHOSE YOUR STARTING POSITION");
            positioningSectionDesc.setText("Please wait while the other players make a decision...");
        }
    }
}
