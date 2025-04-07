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


public class GameView {

    @FXML
    private VBox handComponentBox;

    @FXML
    private GridPane spaceshipMatrix;
    @FXML
    private GridPane bookedArray;

    @FXML
    private Label timerLabel;

    // Initialize the grid when the view is loaded
    public void initialize() {
        setupGrid(5);
    }

    // Setup a grid with a given size
    private void setupGrid(int size) {
        int cellSize = 100;

        for (int i = 0; i < size; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints(cellSize);
            spaceshipMatrix.getColumnConstraints().add(colConstraints);

            RowConstraints rowConstraints = new RowConstraints(cellSize);
            spaceshipMatrix.getRowConstraints().add(rowConstraints);
        }

        // spaceshipMatrix
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Pane cell = new Pane();
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

        if (BuildingData.getHandComponent() == null) {
            GameData.getSender().pickHiddenComponent();
        }
        else if (BuildingData.getxHandComponent() != -1){

            GameData.getSender().placeHandComponentAndPickHiddenComponent(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent());
        }
    }

    public void pickVisibleComponent() {

        if(BuildingData.getIsTimerExpired()){
            System.out.println("Timer expired");
            return;
        }

        if (BuildingData.getHandComponent() == null)
            GameData.getSender().pickHiddenComponent();

        else if(BuildingData.getxHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickVisibleComponent(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent(), -1);
    }

    public void placeLastComponent(){

        if(BuildingData.getHandComponent() == null)
            return;

        if(BuildingData.getxHandComponent() == -1){
            removeHandComponent();
            return;
        }

        GameData.getSender().placeLastComponent(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent());
    }

    public void disableDraggableBookedComponents() {
        for (Node node : PageController.getGameView().getBookedArray().getChildren()) {
            if (node instanceof Pane cell) {
                if (!cell.getChildren().isEmpty()) {
                    Node child = cell.getChildren().get(0);
                    if (child instanceof ImageView imageView) {
                        DragAndDrop.disableDragAndDrop(imageView);
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

        else if(BuildingData.getxHandComponent() != -1)
            GameData.getSender().placeHandComponentAndPickUpEventCardDeck(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent(), idxDeck);
    }

    // Generate a draggable component with an image
    public void generateComponent(String imgComponent) {

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/components/" + imgComponent)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        BuildingData.setNewHandComponent(imageView);

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
