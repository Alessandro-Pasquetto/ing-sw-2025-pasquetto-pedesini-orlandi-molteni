package org.progetto.client.gui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.EventCard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class EventView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;
    final int OTHER_COMPONENT_SIZE = 35;
    final int BOX_SLOT_SIZE = 28;
    final int OTHER_BOX_SLOT_SIZE = 12;
    final int BOX_IMAGE_SIZE = 35;
    final int OTHER_BOX_IMAGE_SIZE = 14;

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
    public ImageView crewSymbol;

    @FXML
    public Label crewValue;

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox overlayContainer;

    @FXML
    private VBox boxesViewContainer;

    @FXML
    private Button eventButton;


    private static final Map<String, GridPane> shipGridsByPlayer = new HashMap<>();

    private final List<Rectangle> boardCells = new ArrayList<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public GridPane getSpaceshipMatrix(){
        return spaceshipMatrix;
    }

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
     * Sets the event title
     *
     * @author Gabriele
     * @param title is the title to set
     */
    public void setEventTitle(String title) {
        eventMainTitle.setText(title);
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
        crewSymbol.setImage(new Image(String.valueOf(MainClient.class.getResource("img/icons/crew.png"))));
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

                    if (comp instanceof HousingUnit housingUnit)
                        renderHousingUnit(cell, housingUnit);

                    else if (comp instanceof BatteryStorage batteryStorage)
                        renderBatteryStorage(cell, batteryStorage);

                    else if(comp instanceof BoxStorage boxStorage)
                        renderBoxStorage(cell, boxStorage);

                    // Component rotation
                    if (comp instanceof HousingUnit)
                        iv.setRotate(comp.getRotation() * 90);
                    else
                        cell.setRotate(comp.getRotation() * 90);
                }

                shipGrid.add(cell, col, row);
            }
        }

        // Update spaceship stats
        float power = ship.getNormalShootingPower();
        if (power == (int) power)
            firePowerValue.setText(String.valueOf((int) power));
        else
            firePowerValue.setText(String.valueOf(power));

        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower()));
        crewValue.setText(String.valueOf(ship.getCrewCount()));
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
     * @param housingUnit is the component to render
     */
    private void renderHousingUnit(Pane cell, HousingUnit housingUnit) {

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
     * @param batteryStorage is the component to render
     */
    private void renderBatteryStorage(Pane cell, BatteryStorage batteryStorage) {
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
     * Renders the box storage
     *
     * @author Alessandro
     * @param cell is the cell to render
     * @param boxStorage is the component to render
     */
    private void renderBoxStorage(Pane cell, BoxStorage boxStorage) {

        switch (boxStorage.getCapacity()) {
            case 1:
                Pane slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(24.0);
                slot1.setLayoutY(24.0);
                slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                renderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                slot1.setUserData(boxStorage.getType());

                cell.getChildren().add(slot1);
                break;

            case 2:
                slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(24.0);
                slot1.setLayoutY(8.0);
                slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                renderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                Pane slot2 = new Pane();
                slot2.setId("boxSlot");
                slot2.setLayoutX(24.0);
                slot2.setLayoutY(40.0);
                slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                renderBox(slot2, boxStorage.getBoxes()[1], boxStorage.getRotation());

                slot1.setUserData(boxStorage.getType());
                slot2.setUserData(boxStorage.getType());
                cell.getChildren().add(slot1);
                cell.getChildren().add(slot2);
                break;

            case 3:
                slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(8.0);
                slot1.setLayoutY(24.0);
                slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                renderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                slot2 = new Pane();
                slot2.setId("boxSlot");
                slot2.setLayoutX(40.0);
                slot2.setLayoutY(8.0);
                slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                renderBox(slot2, boxStorage.getBoxes()[1], boxStorage.getRotation());

                Pane slot3 = new Pane();
                slot3.setId("boxSlot");
                slot3.setLayoutX(40.0);
                slot3.setLayoutY(40.0);
                slot3.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                slot3.getProperties().put("idx", 2);

                renderBox(slot3, boxStorage.getBoxes()[2], boxStorage.getRotation());

                slot1.setUserData(boxStorage.getType());
                slot2.setUserData(boxStorage.getType());
                slot3.setUserData(boxStorage.getType());

                cell.getChildren().add(slot1);
                cell.getChildren().add(slot2);
                cell.getChildren().add(slot3);
                break;
        }
    }

    /**
     * Renders the box in the slot
     *
     * @author Alessandro
     * @param boxSlot is the slot
     * @param box is the box to render
     * @param componentRotation is the rotation to adjust
     */
    private void renderBox(Pane boxSlot, Box box, int componentRotation){

        if(box == null) return;

        Image boxImage = switch (box) {
            case BLUE -> new Image(String.valueOf(MainClient.class.getResource("img/items/BlueBox.png")));
            case GREEN -> new Image(String.valueOf(MainClient.class.getResource("img/items/GreenBox.png")));
            case YELLOW -> new Image(String.valueOf(MainClient.class.getResource("img/items/YellowBox.png")));
            case RED -> new Image(String.valueOf(MainClient.class.getResource("img/items/RedBox.png")));
        };

        ImageView boxImageView = new ImageView(boxImage);
        boxImageView.setFitWidth(BOX_IMAGE_SIZE);
        boxImageView.setFitHeight(BOX_IMAGE_SIZE);
        boxImageView.setLayoutX((BOX_SLOT_SIZE - boxImageView.getFitWidth()) / 2);
        boxImageView.setLayoutY((BOX_SLOT_SIZE - boxImageView.getFitHeight()) / 2);
        boxImageView.setPreserveRatio(false);

        boxImageView.setRotate(-90 * componentRotation);

        boxImageView.setOnMouseClicked(event -> {removeBox(event,boxSlot);});
        boxSlot.getChildren().add(boxImageView);
    }

    public void enableDragAndDropBoxesSpaceship(){
        DragAndDrop.enableDragAndDropItemsSpaceship("box", "boxSlot");
    }

    public void disableDragAndDropBoxesSpaceship(){
        DragAndDrop.disableDragAndDropItemsSpaceship("box");
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

                    else if(comp instanceof BoxStorage boxStorage)
                        renderOtherPlayerBoxStorage(cell, boxStorage);

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
     * @author Gabriele
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
     * @author Gabriele
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
     * Renders the box storage for other players (scaled down)
     *
     * @author Alessandro
     * @param cell is the cell to render
     * @param boxStorage is the component to render
     */
    private void renderOtherPlayerBoxStorage(Pane cell, BoxStorage boxStorage) {

        switch (boxStorage.getCapacity()) {
            case 1:
                Pane slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(11.0);
                slot1.setLayoutY(11.0);
                slot1.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                otherPlayerRenderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                cell.getChildren().add(slot1);
                break;

            case 2:
                slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(11.0);
                slot1.setLayoutY(4.0);
                slot1.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                otherPlayerRenderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                Pane slot2 = new Pane();
                slot2.setId("boxSlot");
                slot2.setLayoutX(11.0);
                slot2.setLayoutY(17.0);
                slot2.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                otherPlayerRenderBox(slot2, boxStorage.getBoxes()[1], boxStorage.getRotation());

                cell.getChildren().add(slot1);
                cell.getChildren().add(slot2);

                break;

            case 3:
                slot1 = new Pane();
                slot1.setId("boxSlot");
                slot1.setLayoutX(5.0);
                slot1.setLayoutY(11.0);
                slot1.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                otherPlayerRenderBox(slot1, boxStorage.getBoxes()[0], boxStorage.getRotation());

                slot2 = new Pane();
                slot2.setId("boxSlot");
                slot2.setLayoutX(17.0);
                slot2.setLayoutY(4.0);
                slot2.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                otherPlayerRenderBox(slot2, boxStorage.getBoxes()[1], boxStorage.getRotation());

                Pane slot3 = new Pane();
                slot3.setId("boxSlot");
                slot3.setLayoutX(17.0);
                slot3.setLayoutY(16.0);
                slot3.setPrefSize(OTHER_BOX_SLOT_SIZE, OTHER_BOX_SLOT_SIZE);
                slot3.getProperties().put("idx", 2);

                otherPlayerRenderBox(slot3, boxStorage.getBoxes()[2], boxStorage.getRotation());

                cell.getChildren().add(slot1);
                cell.getChildren().add(slot2);
                cell.getChildren().add(slot3);
                break;
        }
    }

    /**
     * Renders the box in the slot for other players (scaled down)
     *
     * @author Alessandro
     * @param boxSlot is the slot
     * @param box is the box to render
     * @param componentRotation is the rotation to adjust
     */
    private void otherPlayerRenderBox(Pane boxSlot, Box box, int componentRotation){

        if(box == null) return;

        Image boxImage = switch (box) {
            case BLUE -> new Image(String.valueOf(MainClient.class.getResource("img/items/BlueBox.png")));
            case GREEN -> new Image(String.valueOf(MainClient.class.getResource("img/items/GreenBox.png")));
            case YELLOW -> new Image(String.valueOf(MainClient.class.getResource("img/items/YellowBox.png")));
            case RED -> new Image(String.valueOf(MainClient.class.getResource("img/items/RedBox.png")));
        };

        ImageView boxImageView = new ImageView(boxImage);
        boxImageView.setFitWidth(OTHER_BOX_IMAGE_SIZE);
        boxImageView.setFitHeight(OTHER_BOX_IMAGE_SIZE);
        boxImageView.setLayoutX((OTHER_BOX_SLOT_SIZE - boxImageView.getFitWidth()) / 2);
        boxImageView.setLayoutY((OTHER_BOX_SLOT_SIZE - boxImageView.getFitHeight()) / 2);
        boxImageView.setPreserveRatio(false);

        boxImageView.setRotate(-90 * componentRotation);

        boxSlot.getChildren().add(boxImageView);
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
                        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower() + 2 * counter[0]));
                        enginePowerValue.setStyle("-fx-text-fill: green;");
                        break;

                    case "DoubleCannons":
                        float power;

                        if (counter[0] <= ship.getFullDoubleCannonCount())
                            power = ship.getNormalShootingPower() + 2 * counter[0];
                        else
                            power = ship.getNormalShootingPower() + ship.getFullDoubleCannonCount() + counter[0];

                        if (power == (int) power)
                            firePowerValue.setText(String.valueOf((int) power));
                        else
                            firePowerValue.setText(String.valueOf(power));

                        firePowerValue.setStyle("-fx-text-fill: green;");
                        break;
                }
            }

            if (counter[0] == 0) {
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
                        enginePowerValue.setText(String.valueOf(ship.getNormalEnginePower() + 2 * counter[0]));
                        enginePowerValue.setStyle("-fx-text-fill: green;");
                        break;

                    case "DoubleCannons":
                        float power;

                        if (counter[0] <= ship.getFullDoubleCannonCount())
                            power = ship.getNormalShootingPower() + 2 * counter[0];
                        else
                            power = ship.getNormalShootingPower() + ship.getFullDoubleCannonCount() + counter[0];

                        if (power == (int) power)
                            firePowerValue.setText(String.valueOf((int) power));
                        else
                            firePowerValue.setText(String.valueOf(power));

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

                if (comp != null) {
                    final int finalRow = row;
                    final int finalCol = col;

                    if (typeToHighlight.equals("BATTERY") && types.contains(comp.getType()) && ((BatteryStorage) comp).getItemsCount() > 0) {
                        highlightCell(cell, Color.rgb(95, 228, 43, 0.3));
                        cell.setCursor(Cursor.HAND);
                        cell.setOnMouseClicked(e -> onClick.accept(finalCol, finalRow));
                    }
                    else if (typeToHighlight.equals("CREW") && types.contains(comp.getType()) && ((HousingUnit) comp).getCrewCount() > 0) {
                        highlightCell(cell, Color.rgb(0, 178, 255, 0.3));
                        cell.setCursor(Cursor.HAND);
                        cell.setOnMouseClicked(e -> onClick.accept(finalCol, finalRow));
                    }
                    else if (typeToHighlight.equals("ANY")) {
                        cell.setCursor(Cursor.HAND);
                        cell.setOnMouseClicked(e -> onClick.accept(finalCol, finalRow));
                    }
                }
            }
        }

        if (typeToHighlight.equals("ANY")) {
            colorUnconnectedShipParts();
        }
    }

    /**
     * Highlights with different colors groups of connected ship parts
     *
     * @author Lorenzo
     */
    private void colorUnconnectedShipParts() {
        Spaceship spaceship = GameData.getSpaceship();
        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();
        int[][] visited = new int[spaceshipMatrix.length][spaceshipMatrix[0].length];
        int currentColor = 1;

        // Finds groups of connected ship parts
        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {
                if (spaceshipMatrix[row][col] != null && visited[row][col] == 0) {
                    dfsFindUnconnectedShipParts(spaceship, visited, row, col, currentColor, null);
                    currentColor++;
                }
            }
        }

        // Highlights the groups with different colors
        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {
                if (visited[row][col] != 0) {
                    Pane cell = getCellFromSpaceshipMatrix(col, row);
                    int colorIndex = visited[row][col] % 5;

                    Color color = switch (colorIndex) {
                        case 1 -> Color.rgb(0, 122, 255, 0.3);
                        case 2 -> Color.rgb(52, 199, 89, 0.3);
                        case 3 -> Color.rgb(255, 149, 0, 0.3);
                        case 4 -> Color.rgb(175, 82, 222, 0.3);
                        default -> Color.rgb(90, 200, 250, 0.3);
                    };

                    highlightCell(cell, color);
                }
            }
        }
    }

    /**
     * DFS to find group of connected components
     *
     * @author Gabriele
     * @param spaceship is the spaceship to analyze
     * @param visited is the visited matrix to keep track of visited cells
     * @param row is the current row in the spaceship matrix
     * @param col is the current column in the spaceship matrix
     * @param currentColor is the current color to assign to the group
     * @param prevComponent is the previous component to check connection
     */
    private void dfsFindUnconnectedShipParts(Spaceship spaceship, int[][] visited, int row, int col, int currentColor, Component prevComponent) {
        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        // Check bounds
        if (row < 0 || row >= spaceshipMatrix.length || col < 0 || col >= spaceshipMatrix[row].length) {
            return;
        }

        // Check if the cell is already visited or has no component
        if (visited[row][col] != 0 || spaceshipMatrix[row][col] == null) {
            return;
        }

        // Check if the component is of the same type as the previous one
        if (prevComponent == null) {
            visited[row][col] = currentColor;
        }

        // Check if the component is connected to the previous one
        if (prevComponent != null) {
            if (!spaceship.getBuildingBoard().areConnected(spaceshipMatrix[row][col], prevComponent)) {
                return;
            }
        }

        // Sets group color
        visited[row][col] = currentColor;

        dfsFindUnconnectedShipParts(spaceship, visited, row - 1, col, currentColor, spaceshipMatrix[row][col]); // Up
        dfsFindUnconnectedShipParts(spaceship, visited, row + 1, col, currentColor, spaceshipMatrix[row][col]); // Down
        dfsFindUnconnectedShipParts(spaceship, visited, row, col - 1, currentColor, spaceshipMatrix[row][col]); // Left
        dfsFindUnconnectedShipParts(spaceship, visited, row, col + 1, currentColor, spaceshipMatrix[row][col]); // Right
    }

    /**
     * Asks on which planet to land to player
     *
     * @author Gabriele
     * @param title is the title of the request
     * @param description is the description of the request
     * @param planets is an array of booleans indicating which planets are occupied
     * @param onResponse is the action to perform when the player selects a planet
     */
    public void askPlanetSelection(String title, String description, boolean[] planets, Consumer<Integer> onResponse) {
        resetEventLabels();

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);

        VBox mainContainer = new VBox(15);
        mainContainer.setStyle("-fx-alignment: center;");

        // Buttons for each planet
        HBox planetButtonBox = new HBox(10);
        planetButtonBox.setStyle("-fx-alignment: center;");

        for (int i = 0; i < planets.length; i++) {
            final int planetIndex = i;
            Button planetButton = new Button("Planet " + (i + 1));

            if (planets[i]) {
                planetButton.setDisable(true);
                planetButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #cd2929, #ab1b1b);" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-font-size: 14;" +
                                "-fx-font-weight: 700;" +
                                "-fx-border-color: #c92a2a;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-opacity: 1;" +
                                "-fx-cursor: default;"
                );
            } else {
                planetButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #1ba431, #0d7923);" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-font-size: 14;" +
                                "-fx-font-weight: 700;" +
                                "-fx-border-color: #2f9e44;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;"
                );
                planetButton.setOnAction(e -> {
                    onResponse.accept(planetIndex);
                });
            }

            planetButtonBox.getChildren().add(planetButton);
        }

        // "Don't Land" button (uses default button style)
        Button dontLandButton = new Button("Don't Land");
        dontLandButton.setOnAction(e -> {
            onResponse.accept(-1);
        });

        mainContainer.getChildren().addAll(planetButtonBox, dontLandButton);

        btnContainer.getChildren().clear();
        btnContainer.getChildren().add(mainContainer);
    }

    /**
     * Render the available boxes on the view
     *
     * @author Lorenzo
     * @param availableBoxes is the list of available boxes to render
     */
    public void renderRewardBoxes(String title, String description, ArrayList<Box> availableBoxes) {
        resetEventLabels();

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);
        btnContainer.getChildren().clear();

        VBox mainContainer = new VBox(15);
        mainContainer.setStyle("-fx-alignment: center;");

        // Create boxes container
        FlowPane boxContainer = new FlowPane(10, 10);
        ScrollPane boxScrollPane = new ScrollPane(boxContainer);
        boxScrollPane.setId("boxScrollPane");
        boxScrollPane.setMaxWidth(420);
        boxScrollPane.setMinHeight(110);

        boxContainer.getChildren().clear();

        int idx = 0;
        for (Box box : availableBoxes) {
            Image img = switch (box.getValue()) {
                case 1 -> new Image(String.valueOf(MainClient.class.getResource("img/items/BlueBox.png")));
                case 2 -> new Image(String.valueOf(MainClient.class.getResource("img/items/GreenBox.png")));
                case 3 -> new Image(String.valueOf(MainClient.class.getResource("img/items/YellowBox.png")));
                case 4 -> new Image(String.valueOf(MainClient.class.getResource("img/items/RedBox.png")));
                default -> null;
            };

            ImageView boxImage = new ImageView(img);
            Object[] data = {idx,box.getValue()};
            boxImage.setUserData(data);
            boxImage.setFitWidth(60);
            boxImage.setPreserveRatio(true);
            boxImage.setSmooth(true);
            boxImage.setCache(true);

            boxContainer.getChildren().add(boxImage);

            for(Node node: boxContainer.getChildren()){
                ImageView frame = (ImageView) node;
                DragAndDrop.enableDragAndDropItem(frame,"boxSlot");
            }

            idx++;
        }

        // Leave button
        Button leaveButton = new Button("Leave");
        leaveButton.setOnAction(e -> {
            Sender sender = GameData.getSender();
            sender.responseRewardBox(-1, -1, -1, -1);
        });

        mainContainer.getChildren().addAll(boxScrollPane, leaveButton);

        btnContainer.getChildren().add(mainContainer);
    }

    /**
     * Allows a player to remove a box from the spaceship
     *
     * @author Lorenzo
     * @param event is the click event
     * @param boxSlot is the slot were the box is contained
     */
    public void removeBox(MouseEvent event, Pane boxSlot){
        if(event.getSource() instanceof ImageView){
            if(event.getClickCount() == 2){

                double sceneX = event.getSceneX();
                double sceneY = event.getSceneY();

                for (Node node : PageController.getEventView().getSpaceshipMatrix().getChildren()) {
                    if (node instanceof Pane cell) {

                        Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                        if (cellBounds.contains(sceneX, sceneY)) {

                            if (!cell.getChildren().isEmpty()) {
                                Integer rowIndex = GridPane.getRowIndex(cell);
                                Integer colIndex = GridPane.getColumnIndex(cell);
                                Integer slotIndex = (Integer) boxSlot.getProperties().get("idx");
                                System.out.println("Row: "+ rowIndex + " Coll: "+ colIndex+ " Idx: "+ slotIndex);
                                Sender sender = GameData.getSender();
                                sender.removeBox(colIndex,rowIndex,slotIndex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Asks the player to roll the dice
     *
     * @author Gabriele
     * @param title is the title of the request
     * @param description is the description of the request
     */
    public void askToRollDice(String title, String description) {
        eventMainDesc.setText(description);

        // Roll button
        Button rollButton = new Button("Roll Dice");

        rollButton.setOnAction(e -> {
            GameData.getSender().responseRollDice();
        });

        HBox buttonBox = new HBox(15, rollButton);
        buttonBox.setStyle("-fx-alignment: center;");

        btnContainer.getChildren().clear();
        btnContainer.getChildren().add(buttonBox);
    }

    /**
     * Updates the dice result in the event view
     *
     * @author Gabriele
     * @param result is the result of the dice roll
     * @param name is the name of the player who rolled the dice (can be null for self)
     */
    public void updateDiceResult(int result, String name) {
        eventMainTitle.setText("Dice result");

        if (name != null) {
            eventMainDesc.setText(name + " rolled...");
        } else {
            eventMainDesc.setText("You rolled...");
        }

        btnContainer.getChildren().clear();

        // Display dice result via images
        int firstDice;
        int secondDice;

        switch (result) {
            case 2 -> {
                firstDice = 1;
                secondDice = 1;
            }
            case 3 -> {
                firstDice = 1;
                secondDice = 2;
            }
            case 4 -> {
                firstDice = 1;
                secondDice = 3;
            }
            case 5 -> {
                firstDice = 2;
                secondDice = 3;
            }
            case 6 -> {
                firstDice = 2;
                secondDice = 4;
            }
            case 7 -> {
                firstDice = 3;
                secondDice = 4;
            }
            case 8 -> {
                firstDice = 4;
                secondDice = 4;
            }
            case 9 -> {
                firstDice = 5;
                secondDice = 4;
            }
            case 10 -> {
                firstDice = 5;
                secondDice = 5;
            }
            case 11 -> {
                firstDice = 6;
                secondDice = 5;
            }
            case 12 -> {
                firstDice = 6;
                secondDice = 6;
            }
            default -> throw new IllegalStateException("Unexpected value: " + result);
        }

        Image firstDiceImage = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/dice" + firstDice + ".png")));
        Image secondDiceImage = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/dice" + secondDice + ".png")));

        ImageView firstDiceView = new ImageView(firstDiceImage);
        firstDiceView.setFitWidth(80);
        firstDiceView.setFitHeight(80);
        firstDiceView.setPreserveRatio(true);

        ImageView secondDiceView = new ImageView(secondDiceImage);
        secondDiceView.setFitWidth(80);
        secondDiceView.setFitHeight(80);
        secondDiceView.setPreserveRatio(true);

        HBox diceBox = new HBox(10, firstDiceView, secondDiceView);
        diceBox.setStyle("-fx-alignment: center;");

        btnContainer.getChildren().add(diceBox);

        animateDiceEntrance(firstDiceView, secondDiceView);
    }

    /**
     * Animates the entrance of the dice with a bounce effect
     *
     * @author Lorenzo
     * @param firstDice is the first dice ImageView
     * @param secondDice is the second dice ImageView
     */
    private void animateDiceEntrance(ImageView firstDice, ImageView secondDice) {
        firstDice.setOpacity(0);
        firstDice.setScaleX(0.1);
        firstDice.setScaleY(0.1);
        firstDice.setRotate(-180);

        secondDice.setOpacity(0);
        secondDice.setScaleX(0.1);
        secondDice.setScaleY(0.1);
        secondDice.setRotate(180);

        FadeTransition fadeIn1 = new FadeTransition(Duration.millis(800), firstDice);
        fadeIn1.setFromValue(0);
        fadeIn1.setToValue(1);

        FadeTransition fadeIn2 = new FadeTransition(Duration.millis(800), secondDice);
        fadeIn2.setFromValue(0);
        fadeIn2.setToValue(1);

        ScaleTransition scale1 = new ScaleTransition(Duration.millis(800), firstDice);
        scale1.setFromX(0.1);
        scale1.setFromY(0.1);
        scale1.setToX(1.2);
        scale1.setToY(1.2);
        scale1.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale2 = new ScaleTransition(Duration.millis(800), secondDice);
        scale2.setFromX(0.1);
        scale2.setFromY(0.1);
        scale2.setToX(1.2);
        scale2.setToY(1.2);
        scale2.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition rotate1 = new RotateTransition(Duration.millis(800), firstDice);
        rotate1.setFromAngle(-180);
        rotate1.setToAngle(0);
        rotate1.setInterpolator(Interpolator.EASE_OUT);

        RotateTransition rotate2 = new RotateTransition(Duration.millis(800), secondDice);
        rotate2.setFromAngle(180);
        rotate2.setToAngle(0);
        rotate2.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition bounceBack1 = new ScaleTransition(Duration.millis(200), firstDice);
        bounceBack1.setFromX(1.2);
        bounceBack1.setFromY(1.2);
        bounceBack1.setToX(1.0);
        bounceBack1.setToY(1.0);

        ScaleTransition bounceBack2 = new ScaleTransition(Duration.millis(200), secondDice);
        bounceBack2.setFromX(1.2);
        bounceBack2.setFromY(1.2);
        bounceBack2.setToX(1.0);
        bounceBack2.setToY(1.0);

        ParallelTransition firstDiceAnim = new ParallelTransition(fadeIn1, scale1, rotate1);
        SequentialTransition firstDiceSequence = new SequentialTransition(firstDiceAnim, bounceBack1);

        ParallelTransition secondDiceAnim = new ParallelTransition(fadeIn2, scale2, rotate2);
        SequentialTransition secondDiceSequence = new SequentialTransition(secondDiceAnim, bounceBack2);

        firstDiceSequence.play();

        Timeline delayTimeline = new Timeline(new KeyFrame(Duration.millis(150), e -> secondDiceSequence.play()));
        delayTimeline.play();
    }

    /**
     * Asks the player to select a box to discard
     *
     * @author Gabriele
     * @param title the title to display during selection
     * @param description the description to display during selection
     * @param onClick the function to call when a component is clicked
     */
    public void askToSelectBoxToDiscard(String title, String description, Consumer<int[]> onClick) {
        resetEventLabels();

        eventMainTitle.setText(title);
        eventMainDesc.setText(description);

        Component[][] shipMatrix = GameData.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        spaceshipMatrix.getChildren().forEach(node -> {
            if (node instanceof Pane cell) {

                Integer col = GridPane.getColumnIndex(cell);
                Integer row = GridPane.getRowIndex(cell);

                if (col != null && row != null && row < shipMatrix.length && col < shipMatrix[row].length) {
                    Component comp = shipMatrix[row][col];

                    if (comp instanceof BoxStorage) {
                        cell.getChildren().forEach(childNode -> {
                            if (childNode instanceof Pane boxSlot && "boxSlot".equals(boxSlot.getId())) {

                                // Check if the slot contains a box
                                if (!boxSlot.getChildren().isEmpty()) {
                                    Integer slotIndex = (Integer) boxSlot.getProperties().get("idx");

                                    if (slotIndex != null) {
                                        boxSlot.setStyle("-fx-cursor: hand;");

                                        boxSlot.setOnMouseClicked(event -> {
                                            onClick.accept(new int[]{col, row, slotIndex});
                                            event.consume();
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Returns the cell from the spaceship matrix
     *
     * @author Gabriele
     * @param col column index
     * @param row row index
     * @return the Pane at the specified coordinates
     */
    public Pane getCellFromSpaceshipMatrix(int col, int row) {
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
     * @param color is the color to use for highlighting
     */
    public void highlightCell(Pane cell, Color color) {
        cell.getChildren().removeIf(node -> HIGHLIGHT_ID.equals(node.getId()));

        Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
        overlay.setFill(color);
        overlay.setId("highlight");
        overlay.setMouseTransparent(true);

        cell.getChildren().add(overlay);
        overlay.toFront();
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

//    public void placeStackPanes(boolean[] chosen){
//
//        double cardHeight = 292;
//        double rowHeight = cardHeight / chosen.length ;
//
//        overlayContainer.getChildren().clear();
//
//        for (int i = 0; i < chosen.length; i++) {
//            StackPane stack_zone = new StackPane();
//            stack_zone.setPrefSize(eventCard.getFitWidth(), rowHeight);
//            stack_zone.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
//            int planetIndex = i;
//            stack_zone.setUserData(planetIndex);
//            stack_zone.setOnMouseClicked(e -> {
//                eventButton.setVisible(false);
//                askYesNo("Landing",
//                        "Do you really want to land on planet "+(planetIndex+1),
//                        response -> {
//                            Sender sender = GameData.getSender();
//                            if(response)
//                                sender.responsePlanetLandRequest(planetIndex);
//                            else
//                                eventButton.setVisible(true);
//                            resetEventLabels();
//                            });
//            });
//
//            ImageView pawnView = new ImageView();
//            pawnView.setFitHeight(60);
//            pawnView.setFitWidth(60);
//            pawnView.setMouseTransparent(true);
//
//            StackPane.setAlignment(pawnView, Pos.BOTTOM_LEFT);
//            StackPane.setMargin(pawnView, new Insets(5));
//
//            stack_zone.getChildren().add(pawnView);
//            overlayContainer.getChildren().add(stack_zone);
//        }
//
//
//        eventButton.setText("Leve site");
//        eventButton.setOnAction(e -> {
//            Sender sender = GameData.getSender();
//            sender.responsePlanetLandRequest(-1);
//            eventButton.setVisible(false);
//        });
//        eventButton.setVisible(true);
//
//        resetEventLabels();
//        setEventLabels("Planets in sight!","Click on the planet were you want to to land");
//
//    }
//
//    public void removeStackPanes(){
//        overlayContainer.getChildren().clear();
//    }
}