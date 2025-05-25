package org.progetto.client.gui;

import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.progetto.client.model.BuildingData;
import org.progetto.client.MainClient;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.EventCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BuildingView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;
    final int OTHER_COMPONENT_SIZE = 35;
    final int BOX_SLOT_SIZE = 28;
    final int CREW_SLOT_SIZE = 28;
    final int BATTERY_SLOT_WIDTH = 14;
    final int BATTERY_SLOT_HEIGHT = 36;

    @FXML
    public ImageView spaceShipImage;

    @FXML
    public StackPane buildingPane;

    @FXML
    public HBox eventCardBox;

    @FXML
    public VBox playerListViewContainer;

    @FXML
    public Label activePlayersLabel;

    @FXML
    public Button timerButton;

    @FXML
    public HBox timerContainer;

    @FXML
    public ImageView trash;

    @FXML
    public ImageView rotateButton;

    @FXML
    public ImageView componentDeck;

    @FXML
    public Button buildingReadyButton;

    @FXML
    private Pane handComponentBox;

    @FXML
    private GridPane spaceshipMatrix;

    @FXML
    private GridPane bookedArray;

    @FXML
    private Label timerLabel;

    @FXML
    private GridPane discardedComponentsList;

    @FXML
    private ListView<Player> playerListView;

    @FXML
    private HBox eventCardDisplay;

    @FXML
    public HBox eventCardContainer;

    @FXML
    private ImageView deck0Image;

    @FXML
    private ImageView deck1Image;

    @FXML
    private ImageView deck2Image;

    private static final Map<String, GridPane> shipGridsByPlayer = new HashMap<>();
    private static final Map<String, GridPane> bookedGridsByPlayer = new HashMap<>();

    // =======================
    // METHODS
    // =======================

    /**
     * Initializes the building view
     *
     * @author Alessandro, Gabriele
     */
    public void initImages() {

        // Initialize black hole image
        Image blackHoleImage;

        if (GameData.getLevelGame() == 1) {
            blackHoleImage = new Image(String.valueOf(MainClient.class.getResource("img/black-hole-1.png")));
        } else {
            blackHoleImage = new Image(String.valueOf(MainClient.class.getResource("img/black-hole-2.png")));
        }

        trash.setImage(blackHoleImage);
        RotateTransition rotate = new RotateTransition(Duration.seconds(5), trash);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        trash.setOnDragDropped(event -> {discardComponent();});

        // Initialize hidden component deck
        componentDeck.setImage(new Image(String.valueOf(MainClient.class.getResource("img/hidden-deck.png"))));
        componentDeck.setOnMouseClicked(event -> {
            pickHiddenComponent();
        });

        // Initialize rotate button image
        rotateButton.setImage(new Image(String.valueOf(MainClient.class.getResource("img/rotate.png"))));
        rotateButton.setOnMouseClicked(event -> {
            rotateComponent();
        });
    }

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
        buildingPane.setBackground(background);
    }

    /**
     * Setups the building matrix with a given size
     *
     * @author Alessandro, Lorenzo
     * @param levelShip is the game level
     * @param color is the player color
     */
    public void initSpaceship(int levelShip, int color) {

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

        insertCentralUnitComponent(levelShip, getImgSrcCentralUnitFromColor(color));
        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/spaceship" + levelShip + ".jpg")));
        spaceShipImage.setImage(image);
    }

    /**
     * Initializes the timer
     *
     * @author Gabriele
     * @param levelGame is the game level
     */
    public void initTimer(int levelGame) {

        // Remove timer if the game is level 1
        if (levelGame == 1) {
            timerContainer.getChildren().remove(timerLabel);
            timerContainer.getChildren().remove(timerButton);
        }
    }

    /**
     * Allows to set up the booked array with a given size
     *
     * @author Alessandro, Lorenzo
     * @param levelGame is the game level
     */
    public void initEventCardDecks(int levelGame) {

        // Init event card decks
        if (levelGame == 2){
            deck0Image.setImage(new Image(String.valueOf(MainClient.class.getResource("img/cards/card-back-lv2.jpg"))));
            deck0Image.setOnMouseClicked(event -> {
                ImageView clickedImage = (ImageView) event.getSource();
                String imageId = clickedImage.getId();
                pickUpDeck(imageId);
            });
            deck1Image.setImage(new Image(String.valueOf(MainClient.class.getResource("img/cards/card-back-lv2.jpg"))));
            deck1Image.setOnMouseClicked(event -> {
                ImageView clickedImage = (ImageView) event.getSource();
                String imageId = clickedImage.getId();
                pickUpDeck(imageId);
            });
            deck2Image.setImage(new Image(String.valueOf(MainClient.class.getResource("img/cards/card-back-lv2.jpg"))));
            deck2Image.setOnMouseClicked(event -> {
                ImageView clickedImage = (ImageView) event.getSource();
                String imageId = clickedImage.getId();
                pickUpDeck(imageId);
            });
        } else {
            deck0Image.setFitHeight(0);
            deck1Image.setFitHeight(0);
            deck2Image.setFitHeight(0);
        }
    }

    /**
     * Load in the slider all the visible components discarded by players
     *
     * @author Lorenzo, Gabriele
     * @param visibleComponents are the discarded components
     */
    public void loadVisibleComponents(ArrayList<Component> visibleComponents) {
        discardedComponentsList.getChildren().clear();

        int col = 0;
        int row = 0;

        for (int i = 0; i < visibleComponents.size(); i++) {
            Component component = visibleComponents.get(i);
            final int idx = i;

            Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(COMPONENT_SIZE);
            imageView.setFitHeight(COMPONENT_SIZE);
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(true);
            imageView.setStyle("-fx-cursor: hand;");

            imageView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    pickVisibleComponent(idx);
                }
            });

            discardedComponentsList.add(imageView, col, row);

            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
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
                    nameLabel.setText(player.getName());

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
                updateOtherPlayerSpaceship(player, player.getSpaceship());
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
        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();
        Component[] bookedComponents = ship.getBuildingBoard().getBookedCopy();

        GridPane shipGrid = getShipGridByPlayer(player.getName());
        GridPane bookedGrid = getBookedGridByPlayer(player.getName());

        if (shipGrid == null || bookedGrid == null){
            System.err.println("Ship grid is null");
            return;
        }

        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(OTHER_COMPONENT_SIZE, OTHER_COMPONENT_SIZE);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(OTHER_COMPONENT_SIZE);
                    iv.setFitHeight(OTHER_COMPONENT_SIZE);
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
            cell.setPrefSize(OTHER_COMPONENT_SIZE, OTHER_COMPONENT_SIZE);

            if (comp != null) {
                Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(OTHER_COMPONENT_SIZE);
                iv.setFitHeight(OTHER_COMPONENT_SIZE);
                iv.setPreserveRatio(true);
                cell.getChildren().add(iv);
            }

            bookedGrid.add(cell, i, 0);
        }
    }

    /**
     * Updates the placed component of another player
     *
     * @author Alessandro
     * @param playerName is the name of the player
     * @param component is the component to place
     * @param x is the x coordinate
     * @param y is the y coordinate
     */
    public void updateOtherPlayerPlacedComponent(String playerName, Component component, int x, int y) {

        GridPane shipGrid = getShipGridByPlayer(playerName);

        if (shipGrid == null){
            System.err.println("Ship grid is null");
            return;
        }

        for (Node node : shipGrid.getChildren()) {
            Integer column = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);

            Pane cell = (Pane) node;

            if (column == x && row == y) {
                cell.getChildren().clear();
                Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(OTHER_COMPONENT_SIZE);
                iv.setFitHeight(OTHER_COMPONENT_SIZE);
                iv.setPreserveRatio(true);
                cell.getChildren().add(iv);
                switch (component.getRotation()){
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
        }
    }

    /**
     * Updates the booked component of another player
     *
     * @author Alessandro
     * @param playerName is the name of the player
     * @param component is the component to place
     * @param idx is the index of the booked component
     */
    public void updateOtherPlayerBookedComponent(String playerName, Component component, int idx){
        GridPane bookedGrid = getBookedGridByPlayer(playerName);

        if (bookedGrid == null){
            System.err.println("Ship grid is null");
            return;
        }

        for (Node node : bookedGrid.getChildren()) {
            Integer column = GridPane.getColumnIndex(node);

            Pane cell = (Pane) node;

            if (column == idx) {
                cell.getChildren().clear();

                if(component == null)
                    return;

                Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(OTHER_COMPONENT_SIZE);
                iv.setFitHeight(OTHER_COMPONENT_SIZE);
                iv.setPreserveRatio(true);
                cell.getChildren().add(iv);
                switch (component.getRotation()){
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
     * Renders ready state for a player
     *
     * @author Gabriele
     * @param name is the name of the player to update
     */
    public void updateOtherPlayerReadyState(String name) {
        for (Node cell : playerListView.lookupAll(".list-cell")) {
            if (cell instanceof ListCell<?>) {
                ListCell<?> listCell = (ListCell<?>) cell;
                Object item = listCell.getItem();

                if (item instanceof Player player && player.getName().equals(name)) {
                    Label nameLabel = (Label) ((VBox) listCell.getGraphic()).getChildren().get(0);
                    nameLabel.setText(player.getName() + " (ready)");
                    break;
                }
            }
        }
    }

    /**
     * Update the eventDeck visual
     *
     * @param deck is the list of cards chosen
     */
    public void showEventDeck(ArrayList<EventCard> deck) {

        eventCardContainer.getChildren().clear();

        for (EventCard card : deck) {
            ImageView cardView = new ImageView(new Image(String.valueOf(MainClient.class.getResource("img/cards/" + card.getImgSrc()))));
            cardView.setFitHeight(300);
            cardView.setPreserveRatio(true);
            cardView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8");
            eventCardContainer.getChildren().add(cardView);
        }

        eventCardDisplay.setVisible(true);
        eventCardDisplay.setMouseTransparent(false);
    }

    /**
     * Allows to hide the deck after it was put down
     *
     * @author Lorenzo
     */
    public void hideEventDeck() {

        eventCardDisplay.setVisible(false);
        eventCardDisplay.setMouseTransparent(true);
    }

    /**
     * Pick up the event card deck
     *
     * @param id is the id of the deck
     */
    @FXML
    private void pickUpDeck(String id) {

        if (BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        int idxDeck = switch (id) {
            case "deck0Image" -> 0;
            case "deck1Image" -> 1;
            case "deck2Image" -> 2;
            default -> -1;
        };

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickUpEventCardDeck(idxDeck);

        else if (BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickUpEventCardDeck(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), idxDeck);

        else
            Alerts.showError("Your hand is already full!", true);
    }

    /**
     * Put down the event card deck
     *
     * @author Lorenzo
     */
    @FXML
    private void putDownDeck() {
        GameData.getSender().putDownEventCardDeck();
    }

    public void updateEventDecksAvailability(int deckIdx) {
        switch (deckIdx) {
            case 0:
                deck0Image.setVisible(!deck0Image.isVisible());
                break;

            case 1:
                deck1Image.setVisible(!deck1Image.isVisible());
                break;

            case 2:
                deck2Image.setVisible(!deck2Image.isVisible());
                break;
        }
    }

    /**
     * Gets the image source of the central unit based on the color
     *
     * @author Alessandro
     */
    public String getImgSrcCentralUnitFromColor(int color) {
        return switch (color) {
            case 0 -> "base-unit-blue.jpg";
            case 1 -> "base-unit-green.jpg";
            case 2 -> "base-unit-red.jpg";
            case 3 -> "base-unit-yellow.jpg";
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

    /**
     * Inserts the central unit component in the spaceship matrix
     *
     * @author Alessandro
     */
    public void insertCentralUnitComponent(int levelShip, String imgSrcCentralUnit) {
        int y = 2;
        int x = 2;

        if(levelShip == 2)
            x = 3;

        for (Node node : spaceshipMatrix.getChildren()) {
            if(node instanceof Pane cell) {
                Integer rowIndex = GridPane.getRowIndex(cell);
                Integer colIndex = GridPane.getColumnIndex(cell);

                if (rowIndex == y && colIndex == x) {

                    Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + imgSrcCentralUnit)));
                    ImageView imageView = new ImageView(image);

                    imageView.setFitWidth(COMPONENT_SIZE);
                    imageView.setFitHeight(COMPONENT_SIZE);

                    Pane componentPane = new Pane();
                    componentPane.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                    Pane slot1 = new Pane();
                    slot1.setId("crewSlot");
                    slot1.setLayoutX(8.0);
                    slot1.setLayoutY(24.0);
                    slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                    slot1.getProperties().put("idx", 0);

                    Pane slot2 = new Pane();
                    slot2.setId("crewSlot");
                    slot2.setLayoutX(40.0);
                    slot2.setLayoutY(24.0);
                    slot2.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                    slot2.getProperties().put("idx", 1);

                    componentPane.getChildren().add(imageView);
                    componentPane.getChildren().add(slot1);
                    componentPane.getChildren().add(slot2);

                    cell.getChildren().add(componentPane);

                    break;
                }
            }
        }
    }

    /**
     * Gets the spaceship matrix
     *
     * @author Alessandro
     */
    public GridPane getSpaceshipMatrix() {
        return spaceshipMatrix;
    }

    /**
     * Gets trash element
     *
     * @author Alessandro
     */
    public ImageView getTrash() {
        return trash;
    }

    /**
     * Gets booked array
     *
     * @author Alessandro
     */
    public GridPane getBookedArray() {
        return bookedArray;
    }

    /**
     * Picks a hidden component from the deck
     *
     * @author Alessandro
     */
    public void pickHiddenComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickHiddenComponent();

        else if (BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickHiddenComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());

        else
            Alerts.showError("Your hand is already full!", true);
    }

    /**
     * Picks a visible component from the table
     *
     * @author Alessandro
     */
    public void pickVisibleComponent(int idx) {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickVisibleComponent(idx);

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickVisibleComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), idx);

        else
            Alerts.showError("Your hand is already full!", true);

        GameData.getSender().showVisibleComponents();
    }

    /**
     * Places the hand component in the spaceship matrix
     *
     * @author Alessandro
     */
    public void placeHandComponentAndReady(){

        if(BuildingData.getHandComponent() == null)
            GameData.getSender().readyPlayer();

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndReady(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());

        else
            Alerts.showError("Your hand is full!", true);
    }

    /**
     * After the timer expires, try to place the last component
     *
     * @author Alessandro
     */
    public void placeLastComponent(){

        if(BuildingData.getHandComponent() == null){
            GameData.getSender().readyPlayer();
            return;
        }

        if(BuildingData.getXHandComponent() == -1){
            removeHandComponent();
            GameData.getSender().readyPlayer();
            return;
        }

        GameData.getSender().placeLastComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());
    }

    /**
     * Disables drag and drop for bookComponents
     *
     * @author Alessandro
     */
    public void disableDraggableBookedComponents() {
        for (Node node : PageController.getBuildingView().getBookedArray().getChildren()) {
            if (node instanceof Pane cell) {
                if (!cell.getChildren().isEmpty()) {
                    Node child = cell.getChildren().get(0);
                    if (child instanceof Pane componentPane) {
                        DragAndDrop.disableDragAndDropComponent(componentPane);
                    }
                }
            }
        }
    }

    /**
     * Handle discard communication between view and controller
     *
     * @author Lorenzo
     */
    public void discardComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if(BuildingData.getHandComponent() != null) {
            GameData.getSender().discardComponent();
            GameData.getSender().showVisibleComponents();
        }
    }

    /**
     * Removes the handComponentPane wherever it is
     *
     * @author Alessandro
     */
    public void removeHandComponent() {
        Node parent = BuildingData.getHandComponent().getParent();

        if (parent instanceof Pane pane)
            pane.getChildren().remove(BuildingData.getHandComponent());

        BuildingData.resetHandComponent();
    }

    /**
     * Generates a component pane
     *
     * @author Alessandro
     * @param component is the component to generate
     * @return the generated component pane
     */
    private Pane generateComponentPane(Component component) {
        Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(COMPONENT_SIZE);
        imageView.setFitHeight(COMPONENT_SIZE);

        Pane componentPane = new Pane();
        componentPane.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

        componentPane.getChildren().add(imageView);

        switch (component) {
            case BoxStorage boxStorage -> {

                switch (boxStorage.getCapacity()) {
                    case 1:
                        Pane slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        componentPane.getChildren().add(slot1);
                        break;

                    case 2:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(8.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(24.0);
                        slot2.setLayoutY(40.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(8.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(8.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("boxSlot");
                        slot3.setLayoutX(40.0);
                        slot3.setLayoutY(40.0);
                        slot3.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            case HousingUnit housingUnit -> {
                Pane slot1 = new Pane();
                slot1.setId("crewSlot");
                slot1.setLayoutX(8.0);
                slot1.setLayoutY(24.0);
                slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                Pane slot2 = new Pane();
                slot2.setId("crewSlot");
                slot2.setLayoutX(40.0);
                slot2.setLayoutY(24.0);
                slot2.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                componentPane.getChildren().add(slot1);
                componentPane.getChildren().add(slot2);
            }
            case BatteryStorage batteryStorage -> {
                switch (batteryStorage.getCapacity()) {
                    case 2:
                        Pane slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(24.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);

                        break;
                    case 3:
                        slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(16.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(32.0);
                        slot2.setLayoutY(24.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("batterySlot");
                        slot3.setLayoutX(48.0);
                        slot3.setLayoutY(24.0);
                        slot3.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            default -> {}
        }

        return componentPane;
    }

    /**
     * Generates a draggable paneComponent
     *
     * @author Alessandro
     */
    public void generateHandComponent(Component component) {

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(COMPONENT_SIZE);
        imageView.setFitHeight(COMPONENT_SIZE);

        Pane componentPane = new Pane();
        componentPane.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

        componentPane.getChildren().add(imageView);

        switch (component) {
            case BoxStorage boxStorage -> {

                switch (boxStorage.getCapacity()) {
                    case 1:
                        Pane slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        componentPane.getChildren().add(slot1);
                        break;

                    case 2:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(8.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(24.0);
                        slot2.setLayoutY(40.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(8.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(8.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("boxSlot");
                        slot3.setLayoutX(40.0);
                        slot3.setLayoutY(40.0);
                        slot3.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            case HousingUnit housingUnit -> {
                Pane slot1 = new Pane();
                slot1.setId("crewSlot");
                slot1.setLayoutX(8.0);
                slot1.setLayoutY(24.0);
                slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                Pane slot2 = new Pane();
                slot2.setId("crewSlot");
                slot2.setLayoutX(40.0);
                slot2.setLayoutY(24.0);
                slot2.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                componentPane.getChildren().add(slot1);
                componentPane.getChildren().add(slot2);
            }
            case BatteryStorage batteryStorage -> {
                switch (batteryStorage.getCapacity()) {
                    case 2:
                        Pane slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(24.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(24.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);

                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(16.0);
                        slot1.setLayoutY(24.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(32.0);
                        slot2.setLayoutY(24.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("batterySlot");
                        slot3.setLayoutX(48.0);
                        slot3.setLayoutY(24.0);
                        slot3.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            default -> {}
        }

        BuildingData.setNewHandComponent(componentPane);
        handComponentBox.getChildren().add(BuildingData.getHandComponent());
    }

    /**
     * Updates timer
     *
     * @author Alessandro
     */
    public void updateTimer(int timer) {
        int minutes = timer / 60;
        int seconds = timer % 60;

        String timeText = String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(timeText);
    }

    /**
     * Updates the spaceship matrix
     *
     * @author Alessandro
     */
    public void updateSpaceship(Spaceship spaceship) {

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        for (Node node : this.spaceshipMatrix.getChildren()) {
            if (node instanceof Pane cell) {
                // Check if the cell is already occupied by a Pane (component)
                Integer rowIndex = GridPane.getRowIndex(cell);
                Integer colIndex = GridPane.getColumnIndex(cell);

                Component component = spaceshipMatrix[rowIndex][colIndex];

                // If the cell contains a component and the spaceshipCell is empty
                if (!cell.getChildren().isEmpty() && component == null)
                    cell.getChildren().clear();

                if (component != null){
                    Pane componentPane = generateComponentPane(component);
                    componentPane.setRotate(90 * component.getRotation());
                    cell.getChildren().add(componentPane);
                }
            }
        }
    }

    /**
     * Updates the booked components
     *
     * @author Alessandro
     */
    public void updateBookedComponents(Component[] bookedComponents) {

        for (int i = 0; i < 2; i++) {
            Component component = bookedComponents[i];

            if(component == null)
                continue;

            Pane componentPane = generateComponentPane(component);
            DragAndDrop.enableDragAndDropComponent(componentPane);
            DragAndDrop.setOnMousePressedForBookedComponent(componentPane);

            Pane bookedCell = (Pane) bookedArray.getChildren().get(i);
            bookedCell.getChildren().add(componentPane);
        }
    }

    /**
     * Rotates the paneComponent
     *
     * @author Alessandro
     */
    public void rotateComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if(BuildingData.getHandComponent() != null)
            BuildingData.rotateComponent();

        else
            Alerts.showError("You have no component in your hand!", true);
    }

    /**
     * Ask to reset timer
     *
     * @author Alessandro
     */
    public void resetTimer() {
        GameData.getSender().resetTimer();
    }

    /**
     * Sets the ready button to disabled
     *
     * @author Gabriele
     */
    public void setReadyButtonDisabled() {
        buildingReadyButton.setDisable(true);
    }
}
