package org.progetto.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.progetto.client.connection.socket.SocketClient;


public class GameView {

    @FXML
    private VBox volatileComponent;

    @FXML
    private GridPane grid;

    @FXML
    private Label timerLabel;

    private ImageView handComponent = null;

    // Initialize the grid when the view is loaded
    public void initialize() {
        setupGrid(5);
    }

    // Setup a grid with a given size
    private void setupGrid(int size) {
        int cellSize = 100;

        // Add column and row constraints to define cell size
        for (int i = 0; i < size; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints(cellSize);
            grid.getColumnConstraints().add(colConstraints);
            RowConstraints rowConstraints = new RowConstraints(cellSize);
            grid.getRowConstraints().add(rowConstraints);
        }

        // Add cells to the grid
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Pane cell = new Pane();
                cell.setStyle("-fx-border-color: black; -fx-background-color: rgba(255,255,255,0.3);");
                grid.add(cell, col, row);
            }
        }
    }

    public GridPane getGrid() {
        return grid;
    }

    // Method to start the game
    public void startGame(ActionEvent event) {
        SocketClient.startGame();
    }

    public void pickHiddenComponent(ActionEvent event) {

        if(handComponent == null)
            SocketClient.pickHiddenComponent();
        else if(GameData.getxHandComponent() != -1)
            SocketClient.placeHandComponentAndPickHiddenComponent(GameData.getxHandComponent(), GameData.getyHandComponent(), GameData.getrHandComponent());
    }

    public void discardComponent(ActionEvent event) {
        if(handComponent != null)
            SocketClient.discardComponent();
    }

    public void removeHandComponent() {

        Node parent = handComponent.getParent();

        if (parent instanceof VBox)
            volatileComponent.getChildren().remove(handComponent);

        else if (parent instanceof Pane pane)
            pane.getChildren().remove(handComponent);

        handComponent = null;
    }

    public void showEventCardDeck(ActionEvent event) {
        int idxDeck = 0;
        Button clickedButton = (Button) event.getSource();

        idxDeck = switch (clickedButton.getId()) {
            case "deck0" -> 0;
            case "deck1" -> 1;
            case "deck2" -> 2;
            case "deck3" -> 3;
            default -> -1;
        };

        if(handComponent == null)
            SocketClient.showEventCardDeck(idxDeck);
        else
            SocketClient.placeHandComponentAndShowEventCardDeck(GameData.getxHandComponent(), GameData.getyHandComponent(), GameData.getrHandComponent(), idxDeck);
    }

    // Generate a draggable component with an image
    public void generateComponent(String imgComponent) {
        GameData.resetHandComponent();

        Image image = new Image(String.valueOf(Main.class.getResource("img/components/" + imgComponent)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        if (handComponent != null) {
            DragAndDrop.disableDragAndDrop(handComponent);
        }

        DragAndDrop.makeDraggable(imageView);
        handComponent = imageView;

        Platform.runLater(() -> {
            volatileComponent.getChildren().add(imageView);
        });
    }

    public void updateTimer(int timer) {
        int minutes = timer / 60;
        int seconds = timer % 60;

        String timeText = String.format("%02d:%02d", minutes, seconds);

        Platform.runLater(() -> {
            timerLabel.setText(timeText);
        });
    }

    public void rotateComponent() {
        handComponent.setRotate(handComponent.getRotate() + 90);
        GameData.rotateComponent();
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

        for (Node node : grid.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            if (rowIndex == null) rowIndex = 0;
            if (colIndex == null) colIndex = 0;
            if (rowIndex == y && colIndex == x) {
                Pane cell = (Pane) node;
                Image image = new Image(String.valueOf(Main.class.getResource("img/components/" + imgSrcCentralUnit)));
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
}
