package org.progetto.client.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;
import org.progetto.server.model.events.EventCard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class NewEventView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;
    final int OTHER_COMPONENT_SIZE = 35;

    final String HIGHLIGHT_ID = "highlight";

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
    public Label eventMainTitle;

    @FXML
    public Label eventMainDesc;

    @FXML
    public VBox btnContainer;

    @FXML
    public ImageView boardImage;

    @FXML
    public Group cellsGroup;

    @FXML
    public ImageView eventCard;

    @FXML
    public ImageView firePowerSymbol;

    @FXML
    public Label firePowerValue;

    @FXML
    public ImageView enginePowerSymbol;

    @FXML
    public Label enginePowerValue;

    @FXML
    public ImageView destroyedSymbol;

    @FXML
    public Label destroyedValue;

    @FXML
    public ImageView creditsSymbol;

    @FXML
    public Label creditsValue;

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private ScrollPane chatScrollPane;

    private static final Map<String, GridPane> shipGridsByPlayer = new HashMap<>();

    private final List<Rectangle> boardCells = new ArrayList<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
     * Initializes the track
     *
     * @author Gabriele
     * @param levelGame is the game level
     */
    public void initTrack(int levelGame) {
        cellsGroup.getChildren().clear();
        boardCells.clear();

        double[][] cellPositions = null;

        if (levelGame == 1) {

            cellPositions = new double[][] {
                    {88.54, 42.56}, {118.18, 33.82}, {148.58, 30.02}, {179.36, 29.64},
                    {209.0, 33.44}, {239.02, 42.18}, {264.86, 60.8}, {279.68, 94.24},
                    {263.34, 123.5}, {235.98, 140.6}, {205.96, 150.48}, {175.18, 155.42},
                    {145.16, 155.42}, {114.76, 150.1}, {85.5, 141.36}, {58.9, 121.98},
                    {44.84, 87.78}, {60.42, 59.66}
            };

        } else if (levelGame == 2) {

            cellPositions = new double[][] {
                    {74.48, 36.86}, {98.42, 27.36}, {123.5, 22.8}, {147.82, 19.38}, {176.32, 19.38},
                    {203.68, 21.66}, {226.86, 27.74}, {251.18, 36.86}, {272.46, 53.58}, {287.04, 78.66},
                    {287.66, 108.3}, {270.56, 129.96}, {247.38, 144.78}, {222.68, 154.66}, {196.84, 160.74},
                    {170.62, 164.16}, {145.16, 163.4}, {118.94, 160.74}, {93.86, 154.66}, {69.92, 144.4},
                    {48.26, 128.44}, {32.3, 102.6}, {33.44, 72.96}, {50.92, 51.3}
            };

        }

        for (int i = 0; i < Objects.requireNonNull(cellPositions).length; i++) {
            double x = cellPositions[i][0];
            double y = cellPositions[i][1];

            Rectangle cell = new Rectangle(19, 19);
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
     * Updates the track with the current positions of the players
     *
     * @author Lorenzo
     * @param  playersPosition is the current positions of the players
     */
    public void updateMiniTrack(Player[] playersPosition) {
        cellsGroup.getChildren().removeIf(node -> node instanceof ImageView);

        for (int i = 0; i <  playersPosition.length; i++) {
            Player player =  playersPosition[i];
            if (player != null) {
                Rectangle cell = boardCells.get(i);

                String rocketImage = getRocketImagePath(player.getColor());
                Image rocket = new Image(String.valueOf(MainClient.class.getResource(rocketImage)));
                ImageView rocketView = new ImageView(rocket);

                rocketView.setFitWidth(14.25);
                rocketView.setFitHeight(19);
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
     * Initializes the event card
     *
     * @author Gabriele
     * @param card is the event card to initialize
     */
    public void initEventCard(EventCard card) {
        String imgSource = card.getImgSrc();

        Image img = new Image(String.valueOf(MainClient.class.getResource("img/cards/" + imgSource)));
        eventCard.setImage(img);
    }

    /**
     * Initializes the event labels
     *
     * @author Gabriele
     */
    public void initEventLabels() {
        if (GameData.getHasLeft()) {
            eventMainTitle.setText("YOU LEFT THE TRAVEL");
            eventMainDesc.setText("You can no longer interact with the game...");
            btnContainer.getChildren().clear();
        } else {
            eventMainTitle.setText("");
            eventMainDesc.setText("");
            btnContainer.getChildren().clear();
        }
    }

    /**
     * Sets the event labels
     *
     * @author Gabriele
     */
    public void setEventLabels(String title, String description) {
        eventMainTitle.setText(title);
        eventMainDesc.setText(description);
        btnContainer.getChildren().clear();
    }

    /**
     * Resets the event labels
     *
     * @author Gabriele
     */
    public void resetEventLabels() {
        eventMainTitle.setText("");
        eventMainDesc.setText("");
        btnContainer.getChildren().clear();
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

        // Set spaceship stats images
        firePowerSymbol.setImage(new Image(String.valueOf(MainClient.class.getResource("img/icons/fire-power.png"))));
        enginePowerSymbol.setImage(new Image(String.valueOf(MainClient.class.getResource("img/icons/engine-power.png"))));
        destroyedSymbol.setImage(new Image(String.valueOf(MainClient.class.getResource("img/icons/destroyed.png"))));
        creditsSymbol.setImage(new Image(String.valueOf(MainClient.class.getResource("img/icons/credits.png"))));
    }

    /**
     * Updates the spaceship matrix with the current spaceship
     *
     * @author Stefano
     * @param ship is the spaceship to be updated
     */
    public void updateSpaceship(Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();

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

                    if (comp instanceof HousingUnit) {
                        renderHousingUnit(cell, comp);
                    }

                    if (comp instanceof BatteryStorage) {
                        renderBatteryStorage(cell, comp);
                    }

                    if (comp instanceof HousingUnit) {

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

                    } else {

                        switch (comp.getRotation()) {
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

                shipGrid.add(cell, col, row);
            }
        }

        // Update spaceship stats
        float power = ship.getNormalShootingPower();
        if (power == (int) power)
            firePowerValue.setText(String.valueOf((int) power + (ship.getAlienPurple() ? 2 : 0)));
        else
            firePowerValue.setText(String.valueOf(power + (ship.getAlienPurple() ? 2 : 0)));

        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower() + (ship.getAlienOrange() ? 2 : 0)));
        destroyedValue.setText(String.valueOf(ship.getDestroyedCount()));
        creditsValue.setText(String.valueOf(GameData.getCredits()));

        firePowerValue.setStyle("-fx-text-fill: white;");
        enginePowerValue.setStyle("-fx-text-fill: white;");
    }

    /**
     * Renders the housing unit
     *
     * @author Gabriele
     * @param cell is the cell to render
     * @param comp is the component to render
     */
    private void renderHousingUnit(Pane cell, Component comp) {
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

        } else {
            int crewCount = housingUnit.getCrewCount();

            if (crewCount == 0) {
                return;
            }

            Image crewImage = new Image(String.valueOf(MainClient.class.getResource("img/items/CrewMate_icon.png")));
            double imageSize = COMPONENT_SIZE * 0.4;
            double centerY = (COMPONENT_SIZE - (imageSize * 3/2)) / 2;

            if (crewCount == 1) {
                double centerX = (COMPONENT_SIZE - imageSize) / 2;

                ImageView crewImageView = new ImageView(crewImage);
                crewImageView.setFitWidth(imageSize);
                crewImageView.setPreserveRatio(true);
                crewImageView.setLayoutX(centerX);
                crewImageView.setLayoutY(centerY);
                cell.getChildren().add(crewImageView);

            } else if (crewCount == 2) {
                double spacing = 0;
                double totalWidth = imageSize * 2 + spacing;
                double startX = (COMPONENT_SIZE - totalWidth) / 2;

                for (int i = 0; i < 2; i++) {
                    double x = startX + i * (imageSize + spacing);

                    ImageView crewImageView = new ImageView(crewImage);
                    crewImageView.setFitWidth(imageSize);
                    crewImageView.setPreserveRatio(true);
                    crewImageView.setLayoutX(x);
                    crewImageView.setLayoutY(centerY);
                    cell.getChildren().add(crewImageView);
                }
            }
        }
    }

    /**
     * Renders the battery storage
     *
     * @author Gabriele
     * @param cell is the cell to render
     * @param comp is the component to render
     */
    private void renderBatteryStorage(Pane cell, Component comp) {
        BatteryStorage batteryStorage = (BatteryStorage) comp;
        Image batteryImage = new Image(String.valueOf(MainClient.class.getResource("img/items/Battery_icon.png")));

        if (batteryStorage.getCapacity() == 2) {
            int count = batteryStorage.getItemsCount();
            double imageSizeX = 16;
            double imageSizeY = 36;
            double spacing = 0;

            double startX = 24;
            double centerY = 23;

            for (int i = 0; i < 2; i++) {
                double x = startX + i * (imageSizeX + spacing);

                if (i < count) {
                    // Render battery image
                    ImageView batteryImageView = new ImageView(batteryImage);
                    batteryImageView.setFitWidth(imageSizeX);
                    batteryImageView.setFitHeight(imageSizeY);
                    batteryImageView.setLayoutX(x);
                    batteryImageView.setLayoutY(centerY);
                    batteryImageView.setPreserveRatio(false);
                    cell.getChildren().add(batteryImageView);

                } else {
                    // Render empty slot as black rectangle
                    Rectangle placeholder = new Rectangle(imageSizeX, imageSizeY, Color.BLACK);
                    placeholder.setLayoutX(x);
                    placeholder.setLayoutY(centerY);
                    cell.getChildren().add(placeholder);
                }
            }
        }

        if (batteryStorage.getCapacity() == 3) {
            int count = batteryStorage.getItemsCount();
            double imageSizeX = 16;
            double imageSizeY = 36;
            double spacing = 0;

            double startX = 16;
            double centerY = 23;

            for (int i = 0; i < 3; i++) {
                double x = startX + i * (imageSizeX + spacing);

                if (i < count) {
                    // Render battery image
                    ImageView batteryImageView = new ImageView(batteryImage);
                    batteryImageView.setFitWidth(imageSizeX);
                    batteryImageView.setFitHeight(imageSizeY);
                    batteryImageView.setLayoutX(x);
                    batteryImageView.setLayoutY(centerY);
                    batteryImageView.setPreserveRatio(false);
                    cell.getChildren().add(batteryImageView);
                } else {
                    // Render empty slot as black rectangle
                    Rectangle placeholder = new Rectangle(imageSizeX, imageSizeY, Color.BLACK);
                    placeholder.setLayoutX(x);
                    placeholder.setLayoutY(centerY);
                    cell.getChildren().add(placeholder);
                }
            }
        }
    }

    /**
     * Populate the list of travelers
     *
     * @author Gabriele
     * @param travelers is the list of travelers
     */
    public void initTravelersSpaceshipList(ArrayList<Player> travelers) {

        if (travelers.isEmpty()) {
            playerListViewContainer.setVisible(false);
            return;
        }

        playerListViewContainer.setVisible(true);

        ObservableList<Player> travelersList = FXCollections.observableArrayList(travelers);
        playerListView.setItems(travelersList);

        if (travelersList.isEmpty()) {
            playerListViewContainer.getChildren().removeAll(activePlayersLabel, playerListView);
            playerListViewContainer.setMaxHeight(0);
            VBox.setVgrow(playerListViewContainer, Priority.NEVER);
        }

        // Create a blank separator item between each player
        ObservableList<Player> playersWithSeparators = FXCollections.observableArrayList();
        for (int i = 0; i < travelersList.size(); i++) {
            playersWithSeparators.add(travelersList.get(i));

            if (i < travelersList.size() - 1) {
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

                    updateOtherPlayerSpaceship(player.getName(), player.getSpaceship());
                }
            }
        });
    }

    /**
     * Updates other player's spaceship view
     *
     * @author Gabriele
     * @param playerName is the player to update
     * @param ship is the spaceship to show
     */
    public void updateOtherPlayerSpaceship(String playerName, Spaceship ship) {

        GridPane shipGrid = getShipGridByPlayer(playerName);
        if (shipGrid == null){
            System.err.println("Spaceship grid not ready");
            return;
        }

        shipGrid.getChildren().clear();

        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();

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

                    // Add rendering for specific component types
                    if (comp instanceof HousingUnit)
                        renderOtherPlayerHousingUnit(cell, comp);

                    else if (comp instanceof BatteryStorage)
                        renderOtherPlayerBatteryStorage(cell, comp);

                    // Component rotation
                    if (comp instanceof HousingUnit)
                        iv.setRotate(comp.getRotation() * 90);
                    else
                        cell.setRotate(comp.getRotation() * 90);
                }

                shipGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Renders the housing unit for other players (scaled down)
     *
     * @param cell is the cell to render
     * @param comp is the component to render
     */
    private void renderOtherPlayerHousingUnit(Pane cell, Component comp) {
        HousingUnit housingUnit = (HousingUnit) comp;

        if (housingUnit.getHasPurpleAlien()) {
            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/PurpleAlien.png")));
            ImageView alienImageView = new ImageView(alienImage);
            alienImageView.setFitWidth(OTHER_COMPONENT_SIZE * 0.6);
            alienImageView.setFitHeight(OTHER_COMPONENT_SIZE * 0.6);
            alienImageView.setLayoutX((OTHER_COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
            alienImageView.setLayoutY((OTHER_COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
            alienImageView.setPreserveRatio(true);
            cell.getChildren().add(alienImageView);

        } else if (housingUnit.getHasOrangeAlien()) {
            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/OrangeAlien.png")));
            ImageView alienImageView = new ImageView(alienImage);
            alienImageView.setFitWidth(OTHER_COMPONENT_SIZE * 0.6);
            alienImageView.setFitHeight(OTHER_COMPONENT_SIZE * 0.6);
            alienImageView.setLayoutX((OTHER_COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
            alienImageView.setLayoutY((OTHER_COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
            alienImageView.setPreserveRatio(true);
            cell.getChildren().add(alienImageView);

        } else {
            int crewCount = housingUnit.getCrewCount();

            if (crewCount == 0) {
                return;
            }

            Image crewImage = new Image(String.valueOf(MainClient.class.getResource("img/items/CrewMate_icon.png")));
            double imageSize = OTHER_COMPONENT_SIZE * 0.4;
            double centerY = (OTHER_COMPONENT_SIZE - (imageSize * 3/2)) / 2;

            if (crewCount == 1) {
                double centerX = (OTHER_COMPONENT_SIZE - imageSize) / 2;

                ImageView crewImageView = new ImageView(crewImage);
                crewImageView.setFitWidth(imageSize);
                crewImageView.setPreserveRatio(true);
                crewImageView.setLayoutX(centerX);
                crewImageView.setLayoutY(centerY);
                cell.getChildren().add(crewImageView);

            } else if (crewCount == 2) {
                double spacing = 0;
                double totalWidth = imageSize * 2 + spacing;
                double startX = (OTHER_COMPONENT_SIZE - totalWidth) / 2;

                for (int i = 0; i < 2; i++) {
                    double x = startX + i * (imageSize + spacing);

                    ImageView crewImageView = new ImageView(crewImage);
                    crewImageView.setFitWidth(imageSize);
                    crewImageView.setPreserveRatio(true);
                    crewImageView.setLayoutX(x);
                    crewImageView.setLayoutY(centerY);
                    cell.getChildren().add(crewImageView);
                }
            }
        }
    }

    /**
     * Renders the battery storage for other players (scaled down)
     *
     * @param cell is the cell to render
     * @param comp is the component to render
     */
    private void renderOtherPlayerBatteryStorage(Pane cell, Component comp) {
        BatteryStorage batteryStorage = (BatteryStorage) comp;
        Image batteryImage = new Image(String.valueOf(MainClient.class.getResource("img/items/Battery_icon.png")));

        if (batteryStorage.getCapacity() == 2) {
            int count = batteryStorage.getItemsCount();

            double imageSizeX = 7;
            double imageSizeY = 15.75;
            double spacing = 0;
            double startX = 10.5;
            double centerY = 10;

            for (int i = 0; i < 2; i++) {
                double x = startX + i * (imageSizeX + spacing);

                if (i < count) {
                    ImageView batteryImageView = new ImageView(batteryImage);
                    batteryImageView.setFitWidth(imageSizeX);
                    batteryImageView.setFitHeight(imageSizeY);
                    batteryImageView.setLayoutX(x);
                    batteryImageView.setLayoutY(centerY);
                    batteryImageView.setPreserveRatio(false);
                    cell.getChildren().add(batteryImageView);
                } else {
                    Rectangle placeholder = new Rectangle(imageSizeX, imageSizeY, Color.BLACK);
                    placeholder.setLayoutX(x);
                    placeholder.setLayoutY(centerY);
                    cell.getChildren().add(placeholder);
                }
            }
        }

        if (batteryStorage.getCapacity() == 3) {
            int count = batteryStorage.getItemsCount();

            double imageSizeX = 7;
            double imageSizeY = 15.75;
            double spacing = 0;
            double startX = 7;
            double centerY = 10;

            for (int i = 0; i < 3; i++) {
                double x = startX + i * (imageSizeX + spacing);

                if (i < count) {
                    ImageView batteryImageView = new ImageView(batteryImage);
                    batteryImageView.setFitWidth(imageSizeX);
                    batteryImageView.setFitHeight(imageSizeY);
                    batteryImageView.setLayoutX(x);
                    batteryImageView.setLayoutY(centerY);
                    batteryImageView.setPreserveRatio(false);
                    cell.getChildren().add(batteryImageView);
                } else {
                    Rectangle placeholder = new Rectangle(imageSizeX, imageSizeY, Color.BLACK);
                    placeholder.setLayoutX(x);
                    placeholder.setLayoutY(centerY);
                    cell.getChildren().add(placeholder);
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

                if (item instanceof Player player) {
                    VBox graphic = (VBox) listCell.getGraphic();
                    if (graphic != null && !graphic.getChildren().isEmpty()) {
                        Label nameLabel = (Label) graphic.getChildren().get(0);

                        nameLabel.setText(player.getName());

                        if (player.getName().equals(name)) {
                            nameLabel.setText(player.getName() + " (active)");
                        }
                    }
                }
            }
        }

        // Update event labels based on active player
        if (!name.equals(GameData.getNamePlayer()) && !GameData.getHasLeft()) {
            eventMainTitle.setText("WAIT FOR YOUR TURN");
            eventMainDesc.setText("Another player is taking his decisions, please wait...");
            btnContainer.getChildren().clear();
        }
    }

    /**
     * Ask the player to response yes or no
     *
     * @author Gabriele
     * @param title is the title of the question
     * @param description is the description of the question
     * @param onResponse is the callback function to execute when the player responds
     */
    public void askYesNo(String title, String description, Consumer<Boolean> onResponse) {
        resetEventLabels();

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);

        // Yes/No buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            onResponse.accept(true);
        });

        noButton.setOnAction(e -> {
            onResponse.accept(false);
        });

        HBox buttonBox = new HBox(15, yesButton, noButton);
        buttonBox.setStyle("-fx-alignment: center;");

        btnContainer.getChildren().clear();
        btnContainer.getChildren().add(buttonBox);
    }


    /**
     * Asks the player for a quantity
     *
     * @author Gabriele
     * @param title is the title of the request
     * @param description is the description of the request
     * @param maxCount is the maximum number of items to select
     * @param onConfirm is the action to perform when confirming
     */
    public void askForQuantity(String type, String title, String description, int maxCount, IntConsumer onConfirm) {
        resetEventLabels();

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);

        // Counter controls
        Label counterLabel = new Label("0");
        Button minusButton = new Button("-");
        Button plusButton = new Button("+");
        Button confirmButton = new Button("Confirm");

        final int[] counter = {0};

        minusButton.setOnAction(e -> {

            Spaceship ship = GameData.getSpaceship();

            if (counter[0] > 0) {
                counter[0]--;
                counterLabel.setText(String.valueOf(counter[0]));

                switch (type) {
                    case "DoubleEngines":
                        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower() + (ship.getAlienOrange() ? 2 : 0) + 2 * counter[0]));
                        break;

                    case "DoubleCannons":
                        float power = ship.getNormalShootingPower();
                        if (power == (int) power)
                            firePowerValue.setText(String.valueOf((int) power + (ship.getAlienPurple() ? 2 : 0) + 2 * counter[0]));
                        else
                            firePowerValue.setText(String.valueOf(power + (ship.getAlienPurple() ? 2 : 0) + 2 * counter[0]));
                        break;
                }
            }

            if(counter[0] == 0) {
                switch (type) {
                    case "DoubleEngines":
                        enginePowerValue.setStyle("-fx-text-fill: white;");
                        break;
                    case "DoubleCannons":
                        firePowerValue.setStyle("-fx-text-fill: white;");
                        break;
                }
            }
        });

        plusButton.setOnAction(e -> {

            Spaceship ship = GameData.getSpaceship();

            if (counter[0] < maxCount) {
                counter[0]++;
                counterLabel.setText(String.valueOf(counter[0]));

                switch (type) {
                    case "DoubleEngines":
                        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower() + (ship.getAlienOrange() ? 2 : 0) + 2 * counter[0]));
                        enginePowerValue.setStyle("-fx-text-fill: green;");
                        break;
                    case "DoubleCannons":
                        float power = ship.getNormalShootingPower();
                        if (power == (int) power)
                            firePowerValue.setText(String.valueOf((int) power + (ship.getAlienPurple() ? 2 : 0) + 2 * counter[0]));
                        else
                            firePowerValue.setText(String.valueOf(power + (ship.getAlienPurple() ? 2 : 0) + 2 * counter[0]));
                        firePowerValue.setStyle("-fx-text-fill: green;");
                        break;
                }
            }
        });

        confirmButton.setOnAction(e -> {
            onConfirm.accept(counter[0]);
        });

        counterLabel.setStyle("-fx-text-fill: #e2e2e2; -fx-font-size: 20; -fx-font-weight: bold;");
        minusButton.setStyle("-fx-font-size: 18;");
        plusButton.setStyle("-fx-font-size: 18;");

        minusButton.setMinWidth(50);
        plusButton.setMinWidth(50);
        counterLabel.setMinWidth(40);
        counterLabel.setAlignment(Pos.CENTER);

        HBox counterBox = new HBox(10, minusButton, counterLabel, plusButton);
        counterBox.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-background-color: rgba(255,255,255,0.1); " + "-fx-border-color: rgba(43, 50, 57, 0.5); " + "-fx-border-radius: 8; -fx-background-radius: 8;");
        counterBox.setMaxWidth(150.00);

        HBox container = new HBox(30, counterBox, confirmButton);
        container.setStyle("-fx-alignment: center;");

        btnContainer.getChildren().clear();
        btnContainer.getChildren().add(container);
    }

    /**
     * Aks player to select a component on the ship
     *
     * @author Gabriele
     * @param title is the title of the selection
     * @param description is the description of the selection
     * @param typeToHighlight is the type of component to highlight (BATTERY or CREW)
     * @param onClick is the action to perform when clicking on a component
     */
    public void askToSelectShipComponent(String title, String description, String typeToHighlight, BiConsumer<Integer, Integer> onClick) {
        resetEventLabels();
        clearHighlightedCells();

        Component[][] spaceship = GameData.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        if(spaceship == null){
            System.err.println("Null spaceship");
            return;
        }

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);

        ArrayList<ComponentType> types = new ArrayList<>();

        // Defines which components to highlight
        if (typeToHighlight.equals("BATTERY")) {
            types.add(ComponentType.BATTERY_STORAGE);
        }
        else if (typeToHighlight.equals("CREW")) {
            types.add(ComponentType.CENTRAL_UNIT);
            types.add(ComponentType.HOUSING_UNIT);
        }

        for (int row = 0; row < spaceship.length; row++) {
            for (int col = 0; col < spaceship[row].length; col++) {
                Component comp = spaceship[row][col];
                Pane cell = getCellFromSpaceshipMatrix(col, row);

                if (comp != null && types.contains(comp.getType())) {
                    final int finalRow = row;
                    final int finalCol = col;

                    if (typeToHighlight.equals("BATTERY") && ((BatteryStorage) comp).getItemsCount() > 0) {
                        highlightCell(cell, comp, Color.rgb(95, 228, 43, 0.3));

                        cell.setOnMouseClicked(e -> {
                            onClick.accept(finalCol, finalRow);
                        });
                    }
                    else if (typeToHighlight.equals("CREW") && ((HousingUnit) comp).getCrewCount() > 0) {
                        highlightCell(cell, comp, Color.rgb(0, 178, 255, 0.3));

                        cell.setOnMouseClicked(e -> {
                            onClick.accept(finalCol, finalRow);
                        });
                    }
                }
            }
        }
    }

    /**
     * Returns the cell from the spaceship matrix
     *
     * @author Gabriele
     * @param col column index
     * @param row row index
     * @return the Pane at the specified coordinates
     */
    private Pane getCellFromSpaceshipMatrix(int col, int row) {
        for (Node node : spaceshipMatrix.getChildren()) {
            Integer nodeCol = GridPane.getColumnIndex(node);
            Integer nodeRow = GridPane.getRowIndex(node);

            if (nodeCol == col && nodeRow == row) {
                return (Pane) node;
            }
        }
        return null;
    }

    /**
     * Highlights the cell with a color
     *
     * @author Gabriele
     * @param cell is the cell to highlight
     * @param comp is the component to highlight
     * @param color is the color to use for highlighting
     */
    private void highlightCell(Pane cell, Component comp, Color color) {
        cell.getChildren().removeIf(node -> HIGHLIGHT_ID.equals(node.getId()));

        Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
        overlay.setFill(color);
        overlay.setId("highlight");
        overlay.setMouseTransparent(true);

        cell.getChildren().add(overlay);
        overlay.toFront();
        cell.setCursor(Cursor.HAND);
    }

    /**
     * Clears the highlighted cells
     *
     * @author Stefano
     */
    private void clearHighlightedCells() {
        spaceshipMatrix.getChildren().forEach(node -> {
            if (node instanceof Pane) {
                Pane cell = (Pane) node;
                cell.getChildren().removeIf(child -> HIGHLIGHT_ID.equals(child.getId()));
                cell.setStyle("");
            }
        });
    }

    /**
     * Adds a message to the activity chat with timestamp and type styling
     *
     * @author Gabriele
     * @param message The message to display
     * @param messageType Type of message
     */
    public void addChatMessage(String message, String messageType) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String fullMessage = String.format("[%s] %s", timestamp, message);

            // Create the message label
            Label messageLabel = new Label(fullMessage);
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(Double.MAX_VALUE);

            VBox messageContainer = new VBox();
            messageContainer.getChildren().add(messageLabel);
            messageContainer.setMaxWidth(Double.MAX_VALUE);

            messageContainer.setPadding(new Insets(8, 12, 8, 12));

            String baseContainerStyle = "-fx-background-radius: 4; -fx-border-width: 1; -fx-border-radius: 4;";
            String baseTextStyle = "-fx-font-size: 12px;";

            // Color variables
            String backgroundColor = "";
            String borderColor = "";
            String textColor = "";

            switch (messageType.toUpperCase()) {
                case "RED":
                    backgroundColor = "rgba(255, 89, 89, 0.15)";
                    borderColor = "#ff5959";
                    textColor = "#ff4242";
                    break;

                case "ORANGE":
                    backgroundColor = "rgba(255, 165, 0, 0.15)";
                    borderColor = "#ffa500";
                    textColor = "#ff8c00";
                    break;

                case "GREEN":
                    backgroundColor = "rgba(76, 175, 80, 0.15)";
                    borderColor = "#4CAF50";
                    textColor = "#2e7d32";
                    break;

                case "INFO":
                default:
                    backgroundColor = "rgba(240, 240, 245, 0.12)";
                    borderColor = "#9BA1A6";
                    textColor = "#E1E3E6";
                    break;
            }

            String containerStyle = String.format("-fx-background-color: %s; -fx-border-color: %s; %s", backgroundColor, borderColor, baseContainerStyle);
            String textStyle = String.format("-fx-text-fill: %s; %s", textColor, baseTextStyle);

            messageContainer.setStyle(containerStyle);
            messageLabel.setStyle(textStyle);

            chatMessagesContainer.getChildren().add(messageContainer);

            // Auto-scroll to bottom
            Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
        });
    }

    /**
     * Clears all messages from the chat
     *
     * @author Gabriele
     */
    public void clearChatMessages() {
        Platform.runLater(() -> {
            chatMessagesContainer.getChildren().clear();
        });
    }
}