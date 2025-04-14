package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.progetto.client.model.BuildingData;
import org.progetto.client.MainClient;
import org.progetto.client.model.GameData;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;


public class GameView {

    final int COMPONENT_SIZE = 100;
    final int BOX_SLOT_SIZE = 35;

    @FXML
    public ImageView spaceShipImage;
    @FXML
    public ImageView provaCrewImage;

    @FXML
    private Pane handComponentBox;

    @FXML
    private ImageView provaBoxImage;

    @FXML
    private GridPane spaceshipMatrix;

    @FXML
    private GridPane bookedArray;

    @FXML
    private Label timerLabel;

    // Initialize the grid when the view is loaded
    public void initialize() {
        DragAndDrop.enableDragAndDropItems(provaBoxImage, "boxSlot");
        DragAndDrop.enableDragAndDropItems(provaCrewImage, "crewSlot");
    }

    // Setup a grid with a given size
    public void initSpaceship(int levelShip, String imgSrcCentralUnit) {

        int sizeX = 5;
        int sizeY = 5;

        if (levelShip == 2){
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        // spaceshipMatrix
        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                if(BuildingData.getCellMask(col, row))
                    cell.setId("spaceshipCell");

                spaceshipMatrix.add(cell, col, row);
            }
        }

        insertCentralUnitComponent(levelShip, imgSrcCentralUnit);
        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/spaceship" + levelShip + ".jpg")));
        spaceShipImage.setImage(image);
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
                    slot1.setLayoutX(30.0);
                    slot1.setLayoutY(30.0);
                    slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot1.setStyle("-fx-border-color: gray;");
                    slot1.getProperties().put("idx", 0);

                    componentPane.getChildren().add(imageView);
                    componentPane.getChildren().add(slot1);

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

    // Method to start the game
    public void ready() {
        GameData.getSender().readyPlayer();
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

    public void pickVisibleComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickHiddenComponent();

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickVisibleComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), -1);
    }

    public void placeHandComponentAndReady(){
        if(BuildingData.getHandComponent() == null){
            GameData.getSender().readyPlayer();
            return;
        }

        if(BuildingData.getXHandComponent() != -1)
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
     * Disables drag&drop for bookComponents
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

        if(BuildingData.getHandComponent() != null)
            GameData.getSender().discardComponent();
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
     * Shows an event card deck
     *
     * @author Alessandro
     */
    public void showEventCardDeck(ActionEvent event) {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        int idxDeck = 0;
        Button clickedButton = (Button) event.getSource();

        idxDeck = switch (clickedButton.getId()) {
            case "deck0" -> 0;
            case "deck1" -> 1;
            case "deck2" -> 2;
            case "deck3" -> 3;
            default -> -1;
        };

        if(BuildingData.getHandComponent() == null)
            GameData.getSender().pickUpEventCardDeck(idxDeck);

        else if(BuildingData.getXHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickUpEventCardDeck(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), idxDeck);
    }

    /**
     * Generates a draggable paneComponent
     *
     * @author Alessandro
     */
    public void generateComponent(Component component) {

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(COMPONENT_SIZE);
        imageView.setFitHeight(COMPONENT_SIZE);

        Pane componentPane = new Pane();
        componentPane.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

        componentPane.getChildren().add(imageView);

        //todo: batterySlots, crewSlots
        if(component instanceof BoxStorage boxStorage){

            switch (boxStorage.getCapacity()){
                case 1:
                    Pane slot1 = new Pane();
                    slot1.setId("boxSlot");
                    slot1.setLayoutX(30.0);
                    slot1.setLayoutY(30.0);
                    slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot1.setStyle("-fx-border-color: gray;");
                    slot1.getProperties().put("idx", 0);

                    componentPane.getChildren().add(slot1);
                    break;

                case 2:
                    slot1 = new Pane();
                    slot1.setId("boxSlot");
                    slot1.setLayoutX(30.0);
                    slot1.setLayoutY(10.0);
                    slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot1.setStyle("-fx-border-color: gray;");
                    slot1.getProperties().put("idx", 0);

                    Pane slot2 = new Pane();
                    slot2.setId("boxSlot");
                    slot2.setLayoutX(30.0);
                    slot2.setLayoutY(50.0);
                    slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot2.setStyle("-fx-border-color: gray;");
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
                    slot1.setStyle("-fx-border-color: gray;");
                    slot1.getProperties().put("idx", 0);

                    slot2 = new Pane();
                    slot2.setId("boxSlot");
                    slot2.setLayoutX(50.0);
                    slot2.setLayoutY(10.0);
                    slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot2.setStyle("-fx-border-color: gray;");
                    slot2.getProperties().put("idx", 1);

                    Pane slot3 = new Pane();
                    slot3.setId("boxSlot");
                    slot3.setLayoutX(50.0);
                    slot3.setLayoutY(50.0);
                    slot3.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                    slot3.setStyle("-fx-border-color: gray;");
                    slot3.getProperties().put("idx", 2);

                    componentPane.getChildren().add(slot1);
                    componentPane.getChildren().add(slot2);
                    componentPane.getChildren().add(slot3);
                    break;
            }
        }
        else if (component instanceof HousingUnit housingUnit){
            Pane slot1 = new Pane();
            slot1.setId("crewSlot");
            slot1.setLayoutX(30.0);
            slot1.setLayoutY(30.0);
            slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
            slot1.setStyle("-fx-border-color: gray;");
            slot1.getProperties().put("idx", 0);

            componentPane.getChildren().add(slot1);
        }

        BuildingData.setNewHandComponent(componentPane);

        Platform.runLater(() -> {
            handComponentBox.getChildren().add(BuildingData.getHandComponent());
        });
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
