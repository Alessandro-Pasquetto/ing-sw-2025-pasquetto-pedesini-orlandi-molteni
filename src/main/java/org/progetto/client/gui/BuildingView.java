package org.progetto.client.gui;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BuildingView {

    final int COMPONENT_SIZE = 100;
    final int BOX_SLOT_SIZE = 35;
    final int CREW_SLOT_SIZE = 35;
    final int BATTERY_SLOT_WIDTH = 18;
    final int BATTERY_SLOT_HEIGHT = 45;

    @FXML
    public ImageView spaceShipImage;

    @FXML
    public ImageView CrewImage;

    @FXML
    public ImageView AlienImage;

    @FXML
    public ImageView BatteryImage;

    @FXML
    public HBox eventCardBox;

    @FXML
    public VBox playerListViewContainer;

    @FXML
    public Label activePlayersLabel;

    @FXML
    private Pane handComponentBox;

    @FXML
    private ImageView BoxImage;

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
    private StackPane centerPane;

    @FXML
    private ImageView deck0Image;

    @FXML
    private ImageView deck1Image;

    @FXML
    private ImageView deck2Image;

    private static final Map<String, GridPane> shipGridsByPlayer = new HashMap<>();

    public static void registerPlayerShipGrid(String playerName, GridPane grid) {
        shipGridsByPlayer.put(playerName, grid);
    }

    public static GridPane getShipGridByPlayer(String playerName) {
        return shipGridsByPlayer.get(playerName);
    }

    // Initialize the grid when the view is loaded
    public void initialize() {
        DragAndDrop.enableDragAndDropItems(BoxImage, "boxSlot");
        DragAndDrop.enableDragAndDropItems(CrewImage, "crewSlot");
        DragAndDrop.enableDragAndDropItems(AlienImage, "crewSlot");
        DragAndDrop.enableDragAndDropItems(BatteryImage, "batterySlot");
    }

    /**
     * Allows to set_up the building matrix with a given size
     *
     * @author Alessandro,Lorenzo
     * @param levelShip is the game level
     * @param color is the player color
     */
    public void initSpaceship(int levelShip, int color) {

        int sizeX = 5;
        int sizeY = 5;

        if (levelShip == 2){
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        // Spaceship matrix
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

        // Init event card decks
        if (levelShip == 2){
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
            deck2Image.setImage(new Image(String.valueOf(MainClient.class.getResource("img/cards/card-back-lv1.jpg"))));
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
     * @author Lorenzo
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
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(true);

            imageView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    pickVisibleComponent(idx);
                }
            });

            discardedComponentsList.add(imageView, col, row);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }
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
     * @author Lorenzo
     */
    public void initPlayersList(ArrayList<Player> players) {
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
            // Add null separator between players (but not after the last one)
            if (i < playersList.size() - 1) {
                playersWithSeparators.add(null);
            }
        }

        // Use the new list with separators
        playerListView.setItems(playersWithSeparators);

        playerListView.setCellFactory(listView -> new ListCell<>() {
            private final VBox content = new VBox(5);
            private final Label nameLabel = new Label();
            private final GridPane shipGrid = new GridPane();
            private final ImageView shipBackgroundImage = new ImageView();
            private final StackPane shipDisplay = new StackPane();
            private final Pane overlayPane = new Pane();

            // To store whether we've already rendered the spaceship for this player
            private boolean isShipRendered = false;

            {
                shipBackgroundImage.setFitWidth(330);
                shipBackgroundImage.setPreserveRatio(true);

                shipGrid.setStyle("-fx-background-color: transparent;");
                shipGrid.setLayoutX(GameData.getLevelGame() == 1 ? 59.0 : 16.0);
                shipGrid.setLayoutY(12.0);

                overlayPane.setPrefSize(330, 240);
                overlayPane.getChildren().add(shipGrid);

                // Style name label
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
                nameLabel.setAlignment(Pos.CENTER);
                nameLabel.setMaxWidth(Double.MAX_VALUE);

                shipDisplay.getChildren().addAll(shipBackgroundImage, overlayPane);
                content.getChildren().addAll(nameLabel, shipDisplay);

                // Add padding to the content VBox
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
                        case 0 -> "-fx-background-color: rgba(0,0,178,0.25); -fx-background-radius: 8;";
                        case 1 -> "-fx-background-color: rgba(30,164,0,0.25); -fx-background-radius: 8;";
                        case 2 -> "-fx-background-color: rgba(178,0,0,0.25); -fx-background-radius: 8;";
                        case 3 -> "-fx-background-color: rgba(255,221,0,0.25); -fx-background-radius: 8;";
                        default -> throw new IllegalStateException("Unexpected value: " + color);
                    };

                    // Apply styling to content VBox instead of the cell
                    content.setStyle(colorStyle);

                    // Keep cell background in sync
                    setStyle(colorStyle);

                    // Only clear the grid if it's the first time rendering or new data is needed
                    if (!isShipRendered) {
                        shipGrid.getChildren().clear();

                        int level = GameData.getLevelGame();
                        String imgPath = "img/cardboard/spaceship" + level + ".jpg";
                        Image image = new Image(String.valueOf(MainClient.class.getResource(imgPath)));
                        shipBackgroundImage.setImage(image);

                        // Mark the ship as rendered
                        isShipRendered = true;
                    }

                    setGraphic(content);
                    registerPlayerShipGrid(player.getName(), shipGrid);  // Register the grid for later use
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

    public void updateOtherPlayerSpaceship(Player player, Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();

        GridPane shipGrid = getShipGridByPlayer(player.getName());
        if (shipGrid == null) return;

        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(43, 43);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(43);
                    iv.setFitHeight(43);
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
    }

    /**
     * Allows a player to show spaceships of other players
     *
     * @author Lorenzo
     * @param player is the clicked player
     */
    public void showPlayerSpaceship(Player player, Spaceship ship) {
        try {
            FXMLLoader loader = new FXMLLoader(MainClient.class.getResource("otherPlayerPage.fxml"));
            Parent root = loader.load();

            OtherPlayerSpaceshipView controller = loader.getController();
            controller.drawShip(ship.getBuildingBoard().getCopySpaceshipMatrix());

            Stage stage = new Stage();
            stage.setTitle("Nave di " + player.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
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
    }

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

    public String getImgSrcCentralUnitFromColor(int color) {
        return switch (color) {
            case 0 -> "base-unit-blue.jpg";
            case 1 -> "base-unit-green.jpg";
            case 2 -> "base-unit-red.jpg";
            case 3 -> "base-unit-yellow.jpg";
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

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
                    slot1.setLayoutX(10.0);
                    slot1.setLayoutY(30.0);
                    slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                    slot1.getProperties().put("idx", 0);

                    Pane slot2 = new Pane();
                    slot2.setId("crewSlot");
                    slot2.setLayoutX(50.0);
                    slot2.setLayoutY(30.0);
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

    public GridPane getSpaceshipMatrix() {
        return spaceshipMatrix;
    }

    public GridPane getBookedArray() {
        return bookedArray;
    }

    public void pickHiddenComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickHiddenComponent();

        else if (BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickHiddenComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());
    }

    public void pickVisibleComponent(int idx) {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickVisibleComponent(idx);

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickVisibleComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), idx);

        GameData.getSender().showVisibleComponents();
    }

    public void placeHandComponentAndReady(){

        if(BuildingData.getHandComponent() == null)
            GameData.getSender().readyPlayer();

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndReady(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());
    }

    /**
     * After the timer expires, try to place the last component
     *
     * @author Alessandro
     */
    public void placeLastComponent(){

        if(BuildingData.getHandComponent() == null)
            return;

        if(BuildingData.getXHandComponent() == -1){
            removeHandComponent();
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
        for (Node node : PageController.getGameView().getBookedArray().getChildren()) {
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
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        componentPane.getChildren().add(slot1);
                        break;

                    case 2:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(10.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(30.0);
                        slot2.setLayoutY(50.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(10.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(50.0);
                        slot2.setLayoutY(10.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("boxSlot");
                        slot3.setLayoutX(50.0);
                        slot3.setLayoutY(50.0);
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
                slot1.setLayoutX(10.0);
                slot1.setLayoutY(30.0);
                slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                Pane slot2 = new Pane();
                slot2.setId("crewSlot");
                slot2.setLayoutX(50.0);
                slot2.setLayoutY(30.0);
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
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(50.0);
                        slot2.setLayoutY(30.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);

                        break;
                    case 3:
                        slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(20.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(30.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("batterySlot");
                        slot3.setLayoutX(60.0);
                        slot3.setLayoutY(30.0);
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
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        componentPane.getChildren().add(slot1);
                        break;

                    case 2:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(10.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(30.0);
                        slot2.setLayoutY(50.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(10.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(50.0);
                        slot2.setLayoutY(10.0);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("boxSlot");
                        slot3.setLayoutX(50.0);
                        slot3.setLayoutY(50.0);
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
                slot1.setLayoutX(10.0);
                slot1.setLayoutY(30.0);
                slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                Pane slot2 = new Pane();
                slot2.setId("crewSlot");
                slot2.setLayoutX(50.0);
                slot2.setLayoutY(30.0);
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
                        slot1.setLayoutX(30.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(50.0);
                        slot2.setLayoutY(30.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);

                        break;
                    case 3:
                        slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(20.0);
                        slot1.setLayoutY(30.0);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(40.0);
                        slot2.setLayoutY(30.0);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("batterySlot");
                        slot3.setLayoutX(60.0);
                        slot3.setLayoutY(30.0);
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

    public void updateSpaceship(Spaceship spaceship) {

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getCopySpaceshipMatrix();

        for (Node node : PageController.getGameView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                // Check if the cell is already occupied by a Pane (component)
                Integer rowIndex = GridPane.getRowIndex(cell);
                Integer colIndex = GridPane.getColumnIndex(cell);

                // If the cell contains a component and the spaceshipCell is empty
                if (!cell.getChildren().isEmpty() && spaceshipMatrix[rowIndex][colIndex] == null)
                    cell.getChildren().clear();

                if (spaceshipMatrix[rowIndex][colIndex] != null){
                    Pane componentPane = generateComponentPane(spaceshipMatrix[rowIndex][colIndex]);
                    cell.getChildren().add(componentPane);
                }
            }
        }
    }

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
    }

    /**
     * Ask to reset timer
     *
     * @author Alessandro
     */
    public void resetTimer() {
        GameData.getSender().resetTimer();
    }
}
