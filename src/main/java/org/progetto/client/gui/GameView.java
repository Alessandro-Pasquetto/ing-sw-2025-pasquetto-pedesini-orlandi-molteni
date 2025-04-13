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


public class GameView {

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
        setupGrid(5);
        DragAndDrop.enableDragAndDropBoxes(provaBoxImage);
    }

    // Setup a grid with a given size
    private void setupGrid(int size) {
        int cellSize = 100;

        // spaceshipMatrix
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(cellSize, cellSize);
                cell.setStyle("-fx-border-color: black; -fx-background-color: rgba(255,255,255,0.3);");
                spaceshipMatrix.add(cell, col, row);
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

    public void placeLastComponent(){

        if(BuildingData.getHandComponent() == null)
            return;

        if(BuildingData.getXHandComponent() == -1){
            removeHandComponent();
            return;
        }

        GameData.getSender().placeLastComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent());
    }

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

        if(BuildingData.getHandComponent() != null){
            GameData.getSender().discardComponent();
            removeHandComponent();
        }
    }

    public void removeHandComponent() {
        Node parent = BuildingData.getHandComponent().getParent();

        if (parent instanceof VBox)
            handComponentBox.getChildren().remove(BuildingData.getHandComponent());

        else if (parent instanceof Pane pane)
            pane.getChildren().remove(BuildingData.getHandComponent());

        BuildingData.resetHandComponent();
    }

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

    // Generate a draggable component with an image
    public void generateComponent(Component component) {

        final int componentSize = 100;
        final int boxSize = 35;

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + component.getImgSrc())));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(componentSize);
        imageView.setFitHeight(componentSize);

        Pane componentPane = new Pane();
        componentPane.setPrefWidth(componentSize);
        componentPane.setPrefHeight(componentSize);

        componentPane.getChildren().add(imageView);

        //todo: inserire gli slot in base al componente
        if(component instanceof BoxStorage boxStorage){

            switch (boxStorage.getCapacity()){
                case 1:
                    Pane slot1 = new Pane();
                    slot1.setId("boxSlot1");
                    slot1.setLayoutX(30.0);
                    slot1.setLayoutY(30.0);
                    slot1.setPrefWidth(boxSize);
                    slot1.setPrefHeight(boxSize);
                    slot1.setStyle("-fx-border-color: gray;");

                    componentPane.getChildren().add(slot1);
                    break;

                case 2:
                    slot1 = new Pane();
                    slot1.setId("boxSlot1");
                    slot1.setLayoutX(30.0);
                    slot1.setLayoutY(10.0);
                    slot1.setPrefWidth(boxSize);
                    slot1.setPrefHeight(boxSize);
                    slot1.setStyle("-fx-border-color: gray;");

                    Pane slot2 = new Pane();
                    slot2.setId("boxSlot2");
                    slot2.setLayoutX(30.0);
                    slot2.setLayoutY(50.0);
                    slot2.setPrefWidth(boxSize);
                    slot2.setPrefHeight(boxSize);
                    slot2.setStyle("-fx-border-color: gray;");

                    componentPane.getChildren().add(slot1);
                    componentPane.getChildren().add(slot2);
                    break;

                case 3:
                    slot1 = new Pane();
                    slot1.setId("boxSlot1");
                    slot1.setLayoutX(10.0);
                    slot1.setLayoutY(30.0);
                    slot1.setPrefWidth(boxSize);
                    slot1.setPrefHeight(boxSize);
                    slot1.setStyle("-fx-border-color: gray;");

                    slot2 = new Pane();
                    slot2.setId("boxSlot2");
                    slot2.setLayoutX(50.0);
                    slot2.setLayoutY(10.0);
                    slot2.setPrefWidth(boxSize);
                    slot2.setPrefHeight(boxSize);
                    slot2.setStyle("-fx-border-color: gray;");

                    Pane slot3 = new Pane();
                    slot3.setId("boxSlot3");
                    slot3.setLayoutX(50.0);
                    slot3.setLayoutY(50.0);
                    slot3.setPrefWidth(boxSize);
                    slot3.setPrefHeight(boxSize);
                    slot3.setStyle("-fx-border-color: gray;");

                    componentPane.getChildren().add(slot1);
                    componentPane.getChildren().add(slot2);
                    componentPane.getChildren().add(slot3);
                    break;
            }
        }

        BuildingData.setNewHandComponent(componentPane);

        Platform.runLater(() -> {
            handComponentBox.getChildren().add(BuildingData.getHandComponent());
        });
    }

    public void updateTimer(int timer) {
        int minutes = timer / 60;
        int seconds = timer % 60;

        String timeText = String.format("%02d:%02d", minutes, seconds);

        timerLabel.setText(timeText);
    }

    public void rotateComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if(BuildingData.getHandComponent() != null)
            BuildingData.rotateComponent();
    }

    public void insertCentralUnitComponent(String imgSrcCentralUnit, int levelShip) {
        int x = 0, y = 0;
        if(levelShip == 1){
            x = 2;
            y = 2;
        } else if (levelShip == 2) {
            x = 3;
            y = 2;
        }

        for (Node node : spaceshipMatrix.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            if (rowIndex == null) rowIndex = 0;
            if (colIndex == null) colIndex = 0;
            if (rowIndex == y && colIndex == x) {
                Pane cell = (Pane) node;
                Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + imgSrcCentralUnit)));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                cell.getChildren().add(imageView);

                // Center the image inside the cell
                imageView.setLayoutX((cell.getWidth() - imageView.getFitWidth()) / 2);
                imageView.setLayoutY((cell.getHeight() - imageView.getFitHeight()) / 2);
                break;
            }
        }
    }

    public void resetTimer() {
        GameData.getSender().resetTimer();
    }
}
