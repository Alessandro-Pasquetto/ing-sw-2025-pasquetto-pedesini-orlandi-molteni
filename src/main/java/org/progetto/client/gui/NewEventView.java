package org.progetto.client.gui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.EventCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewEventView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;

    @FXML
    public StackPane eventPane;

    @FXML
    public Label activePlayersLabel;

    @FXML
    public VBox playerListViewContainer;

    @FXML
    public ListView playerListView;

    @FXML
    public ImageView spaceShipImage;

    @FXML
    public GridPane spaceshipMatrix;

    @FXML
    public VBox btnContainer;

    @FXML
    public ImageView boardImage;

    @FXML
    public ImageView eventCard;

    private static final Map<String, GridPane> shipGridsByPlayer = new HashMap<>();
    private static final Map<String, GridPane> bookedGridsByPlayer = new HashMap<>();

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
        eventPane.setBackground(background);
    }

    /**
     * Initializes the board image
     *
     * @author Gabriele
     * @param levelGame is the game level
     */
    public void initBoard(int levelGame) {
        Image img = null;
        if(levelGame == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/board1.png")));

        else if(levelGame == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/board2.png")));

        boardImage.setImage(img);
    }

    public void initEventCard(EventCard card) {
        String imgSource = card.getImgSrc();

        Image img = new Image(String.valueOf(MainClient.class.getResource("img/cards/" + imgSource)));
        eventCard.setImage(img);
    }

    /**
     * Initializes the spaceship matrix
     *
     * @author Gabriele
     * @param levelShip is the spaceship level
     */
    public void initSpaceship(int levelShip) {

        // Spaceship matrix
        int sizeX = 5;
        int sizeY = 5;

        if (levelShip == 2){
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                if(BuildingData.getCellMask(col, row))
                    cell.setId("spaceshipCell");

                spaceshipMatrix.add(cell, col, row);
            }
        }

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/spaceship" + levelShip + ".jpg")));
        spaceShipImage.setImage(image);
    }

    /**
     * Updates the spaceship matrix with the current spaceship
     *
     * @author Stefano
     * @param ship is the spaceship to be updated
     */
    public void updateSpaceship(Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();

        GridPane shipGrid = spaceshipMatrix;

        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(COMPONENT_SIZE);
                    iv.setFitHeight(COMPONENT_SIZE);
                    iv.setPreserveRatio(true);
                    cell.getChildren().add(iv);

                    switch (comp.getRotation()) {
                        case 0:
                            iv.setRotate(0);
                            break;
                        case 1:
                            iv.setRotate(90);
                            break;
                        case 2:
                            iv.setRotate(180);
                            break;
                        case 3:
                            iv.setRotate(270);
                            break;
                    }

                    // Adds alien if present
                    if (comp instanceof HousingUnit) {
                        HousingUnit housingUnit = (HousingUnit) comp;

                        if (housingUnit.getHasPurpleAlien()) {
                            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/PurpleAlien.png")));
                            ImageView alienImageView = new ImageView(alienImage);
                            alienImageView.setFitWidth(COMPONENT_SIZE * 0.6);
                            alienImageView.setFitHeight(COMPONENT_SIZE * 0.6);
                            alienImageView.setLayoutX((COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
                            alienImageView.setLayoutY((COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
                            alienImageView.setPreserveRatio(true);
                            cell.getChildren().add(alienImageView);

                        } else if (housingUnit.getHasOrangeAlien()) {
                            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/OrangeAlien.png")));
                            ImageView alienImageView = new ImageView(alienImage);
                            alienImageView.setFitWidth(COMPONENT_SIZE * 0.6);
                            alienImageView.setFitHeight(COMPONENT_SIZE * 0.6);
                            alienImageView.setLayoutX((COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
                            alienImageView.setLayoutY((COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
                            alienImageView.setPreserveRatio(true);
                            cell.getChildren().add(alienImageView);
                        }

                        else if (housingUnit.getCrewCount() == 2) {
                            Image crewImage = new Image(String.valueOf(MainClient.class.getResource("img/items/CrewMate_icon.png")));

                            double imageSize = COMPONENT_SIZE * 0.4;
                            double spacing = COMPONENT_SIZE * 0;

                            double totalWidth = imageSize * 2 + spacing;
                            double startX = (COMPONENT_SIZE - totalWidth) / 2;
                            double centerY = (COMPONENT_SIZE - (imageSize * 3/2)) / 2;

                            ImageView crewImageView1 = new ImageView(crewImage);
                            crewImageView1.setFitWidth(imageSize);
                            crewImageView1.setPreserveRatio(true);
                            crewImageView1.setLayoutX(startX);
                            crewImageView1.setLayoutY(centerY);
                            cell.getChildren().add(crewImageView1);

                            ImageView crewImageView2 = new ImageView(crewImage);
                            crewImageView2.setFitWidth(imageSize);
                            crewImageView2.setPreserveRatio(true);
                            crewImageView2.setLayoutX(startX + imageSize + spacing);
                            crewImageView2.setLayoutY(centerY);
                            cell.getChildren().add(crewImageView2);
                        }
                    }
                }

                shipGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Asks the player if they want to place an alien
     *
     * @author Stefano
     * @param alienColor is the color of the alien
     * @param ship is the spaceship where the alien will be placed
     */
    public void askForAlien(String alienColor, Spaceship ship) {

//        populatingSectionDesc.setText("Select a component to fill it with the " + alienColor + " alien clicking on it...");
//
//        PageController.getPopulatingView().updateSpaceship(ship);
//        highlightCellsForAlien(ship, alienColor);
//
//        clearBtnContainer();
//
//        Button btn = new Button("Skip");
//        btn.setOnAction(e -> GameData.getSender().responsePlaceAlien(-1, -1, alienColor));
//        btnContainer.getChildren().add(btn);
    }

    /**
     * Clears the button container
     *
     * @author Alessandro
     */
    public void clearBtnContainer() {
        btnContainer.getChildren().clear();
    }

    /**
     * Updates scene labels
     *
     * @author Gabriele
     */
    public void updateLabels() {
//        populatingSectionTitle.setText("YOUR SPACESHIP IS POPULATED");
//        populatingSectionDesc.setText("Please wait while the other players do so...");
    }

    /**
     * First call to server for players list
     *
     * @author Lorenzo
     */
    public void initPlayersList() {
        GameData.getSender().showPlayers();
    }

    /**
     * Populate the list of active players
     *
     * @author Gabriele
     * @param players is the list of players
     */
    public void updatePlayersList(ArrayList<Player> players) {
        players.removeIf(player -> player.getName().equals(GameData.getNamePlayer()));

        if (players.isEmpty()) {
            playerListViewContainer.getChildren().removeAll(activePlayersLabel, playerListView);
            playerListViewContainer.setMaxHeight(0);
            VBox.setVgrow(playerListViewContainer, Priority.NEVER);
            playerListViewContainer.setVisible(false);
            playerListViewContainer.setManaged(false);
            return;
        }

        ObservableList<Player> playersList = FXCollections.observableArrayList(players);
        playerListView.setItems(playersList);

        if (playersList.isEmpty()) {
            playerListViewContainer.getChildren().removeAll(activePlayersLabel, playerListView);
            playerListViewContainer.setMaxHeight(0);
            VBox.setVgrow(playerListViewContainer, Priority.NEVER);
        }

        // Create a blank separator item between each player
        ObservableList<Player> playersWithSeparators = FXCollections.observableArrayList();
        for (int i = 0; i < playersList.size(); i++) {
            playersWithSeparators.add(playersList.get(i));

            if (i < playersList.size() - 1) {
                playersWithSeparators.add(null);
            }
        }

        playerListView.setItems(playersWithSeparators);

        playerListView.setCellFactory(listView -> new ListCell<Player>() {
            private final VBox content = new VBox(5);
            private final Label nameLabel = new Label();
            private final GridPane shipGrid = new GridPane();
            private final GridPane bookedGrid = new GridPane();
            private final ImageView shipBackgroundImage = new ImageView();
            private final StackPane shipDisplay = new StackPane();
            private final Pane overlayPane = new Pane();

            // To store whether we've already rendered the spaceship for this player
            private boolean isShipRendered = false;

            {
                shipBackgroundImage.setFitWidth(267);
                shipBackgroundImage.setPreserveRatio(true);

                shipGrid.setStyle("-fx-background-color: transparent;");
                shipGrid.setLayoutX(GameData.getLevelGame() == 1 ? 50.0 : 15.0);
                shipGrid.setLayoutY(9.0);

                bookedGrid.setStyle("-fx-background-color: transparent;");
                bookedGrid.setLayoutX(195.0);
                bookedGrid.setLayoutY(6.0);

                overlayPane.setPrefSize(267, 195);
                overlayPane.getChildren().add(shipGrid);
                overlayPane.getChildren().add(bookedGrid);

                // Style name label
                nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                nameLabel.setAlignment(Pos.CENTER);
                nameLabel.setMaxWidth(Double.MAX_VALUE);

                shipDisplay.getChildren().addAll(shipBackgroundImage, overlayPane);
                content.getChildren().addAll(nameLabel, shipDisplay);

                content.setPadding(new Insets(5, 5, 5, 5));
            }

            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent !important;");
                    setMaxHeight(0);

                } else if (player == null) {
                    // This is our separator
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent !important;");
                    setPrefHeight(0);

                } else {

                    if (player.getName().equals(GameData.getActivePlayer())) {
                        nameLabel.setText(player.getName() + " (active)");
                    } else {
                        nameLabel.setText(player.getName());
                    }

                    int color = player.getColor();
                    String colorStyle = switch (color) {
                        case 0 -> "-fx-background-color: rgba(0,0,178,0.25); " +
                                "-fx-border-color: rgba(0,0,178,1); " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8;";
                        case 1 -> "-fx-background-color: rgba(30,164,0,0.25); " +
                                "-fx-border-color: rgba(30,164,0,1); " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8;";
                        case 2 -> "-fx-background-color: rgba(178,0,0,0.25); " +
                                "-fx-border-color: rgba(178,0,0,1); " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8;";
                        case 3 -> "-fx-background-color: rgba(255,221,0,0.25); " +
                                "-fx-border-color: rgba(255,221,0,1); " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-radius: 8;";
                        default -> throw new IllegalStateException("Unexpected value: " + color);
                    };

                    setStyle(colorStyle);

                    // Only clear the grid if it's the first time rendering or new data is needed
                    if (!isShipRendered) {
                        shipGrid.getChildren().clear();
                        bookedGrid.getChildren().clear();

                        int level = GameData.getLevelGame();
                        String imgPath = "img/cardboard/spaceship" + level + ".jpg";
                        Image image = new Image(String.valueOf(MainClient.class.getResource(imgPath)));
                        shipBackgroundImage.setImage(image);

                        // Mark the ship as rendered
                        isShipRendered = true;
                    }

                    setGraphic(content);
                    registerPlayerShipGrid(player.getName(), shipGrid);
                    registerPlayerBookedGrid(player.getName(), bookedGrid);
                }
            }
        });

        // Add a delay to show the spaceship
        PauseTransition delay = new PauseTransition(Duration.millis(250));
        delay.setOnFinished(event -> {
            for (Player player : playersList) {
                GameData.getSender().showSpaceship(player.getName());
            }
        });
        delay.play();
    }

    /**
     * Updates other player's spaceship view
     *
     * @author Gabriele
     * @param player is the player to update
     * @param ship is the spaceship to show
     */
    public void updateOtherPlayerSpaceship(Player player, Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();
        Component[] bookedComponents = ship.getBuildingBoard().getBookedCopy();

        GridPane shipGrid = getShipGridByPlayer(player.getName());
        GridPane bookedGrid = getBookedGridByPlayer(player.getName());
        if (shipGrid == null || bookedGrid == null) return;

        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(35, 35);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(35);
                    iv.setFitHeight(35);
                    iv.setPreserveRatio(true);
                    cell.getChildren().add(iv);
                    switch (comp.getRotation()){
                        case 0:
                            cell.setRotate(0);
                            break;
                        case 1:
                            cell.setRotate(90);
                            break;
                        case 2:
                            cell.setRotate(180);
                            break;
                        case 3:
                            cell.setRotate(270);
                            break;
                    }
                }

                shipGrid.add(cell, col, row);
            }
        }

        bookedGrid.getChildren().clear();
        for (int i = 0; i < bookedComponents.length; i++) {
            Component comp = bookedComponents[i];
            Pane cell = new Pane();
            cell.setPrefSize(35, 35);

            if (comp != null) {
                Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(35);
                iv.setFitHeight(35);
                iv.setPreserveRatio(true);
                cell.getChildren().add(iv);
            }

            bookedGrid.add(cell, i, 0);
        }
    }

    /**
     * Register the ship grid for a player
     *
     * @author Gabriele
     */
    public static void registerPlayerShipGrid(String playerName, GridPane grid) {
        shipGridsByPlayer.put(playerName, grid);
    }

    /**
     * Get the ship grid for a player
     *
     * @author Gabriele
     */
    public static GridPane getShipGridByPlayer(String playerName) {
        return shipGridsByPlayer.get(playerName);
    }

    /**
     * Register the booked grid for a player
     *
     * @author Gabriele
     */
    public static void registerPlayerBookedGrid(String playerName, GridPane grid) {
        bookedGridsByPlayer.put(playerName, grid);
    }

    /**
     * Get the booked grid for a player
     *
     * @author Gabriele
     */
    public static GridPane getBookedGridByPlayer(String playerName) {
        return bookedGridsByPlayer.get(playerName);
    }

    /**
     * Renders active player
     *
     * @author Gabriele
     * @param name is the name of the player to update
     */
    public void updateActivePlayer(String name) {
        for (Node cell : playerListView.lookupAll(".list-cell")) {
            if (cell instanceof ListCell<?>) {
                ListCell<?> listCell = (ListCell<?>) cell;
                Object item = listCell.getItem();

                if (item instanceof Player player && player.getName().equals(name)) {
                    Label nameLabel = (Label) ((VBox) listCell.getGraphic()).getChildren().get(0);
                    nameLabel.setText(player.getName() + " (active)");
                    break;
                }
            }
        }
    }
}
