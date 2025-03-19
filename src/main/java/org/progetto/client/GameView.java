package org.progetto.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class GameView {

    @FXML
    private VBox components;

    @FXML
    private GridPane grid;

    private ImageView lastGeneratedComponent = null;

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

    // Method to simulate picking a component
    public void pickComponent(ActionEvent actionEvent) {
        SocketClient.pickComponent();
    }

    // Method to start the game
    public void startGame(ActionEvent actionEvent) {
        SocketClient.startGame();
    }

    // Generate a draggable component with an image
    public void generateComponent(String imgComponent) {
        Image image = new Image(String.valueOf(Main.class.getResource("img/components/" + imgComponent)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        if (lastGeneratedComponent != null) {
            disableDragAndDrop(lastGeneratedComponent);
        }

        makeDraggable(imageView);
        lastGeneratedComponent = imageView;

        Platform.runLater(() -> {
            components.getChildren().add(imageView);
        });
    }

    public void rotateComponent() {
        lastGeneratedComponent.setRotate(lastGeneratedComponent.getRotate() + 90);
    }

    // Method to disable drag-and-drop for an ImageView
    private void disableDragAndDrop(ImageView imageView) {
        imageView.setOnMousePressed(null);
        imageView.setOnMouseDragged(null);
        imageView.setOnMouseReleased(null);
    }

    private void makeDraggable(ImageView imageView) {
        // Make sure the image responds to mouse events
        imageView.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        imageView.setOnMousePressed(event -> {
            // Save initial scene coordinates for drag detection
            imageView.getProperties().put("initialSceneX", event.getSceneX());
            imageView.getProperties().put("initialSceneY", event.getSceneY());

            // Save the original parent and layout coordinates
            imageView.getProperties().put("originalParent", imageView.getParent());
            imageView.getProperties().put("originalLayoutX", imageView.getLayoutX());
            imageView.getProperties().put("originalLayoutY", imageView.getLayoutY());

            // Get the root of the scene (AnchorPane)
            AnchorPane root = (AnchorPane) imageView.getScene().getRoot();
            Point2D scenePos = imageView.getParent().localToScene(imageView.getLayoutX(), imageView.getLayoutY()); // Convert local position to scene coordinates

            // Move the image to the root if it isn't already there
            if (imageView.getParent() != root) {
                ((Pane) imageView.getParent()).getChildren().remove(imageView);
                root.getChildren().add(imageView);
            }

            // Convert scene coordinates back to local root coordinates
            Point2D localPos = root.sceneToLocal(scenePos);
            imageView.setLayoutX(localPos.getX());
            imageView.setLayoutY(localPos.getY());

            // Save the offset for later (used during dragging)
            imageView.getProperties().put("dragOffsetX", event.getSceneX() - imageView.getLayoutX());
            imageView.getProperties().put("dragOffsetY", event.getSceneY() - imageView.getLayoutY());
            event.consume();  // Consume the event to prevent default behavior
        });

        // MouseDragged: update image position based on drag
        imageView.setOnMouseDragged(event -> {
            // Get the initial offset saved during MousePressed
            double offsetX = (double) imageView.getProperties().get("dragOffsetX");
            double offsetY = (double) imageView.getProperties().get("dragOffsetY");

            // Calculate the new position of the image based on the mouse's current position
            double newX = event.getSceneX() - offsetX;
            double newY = event.getSceneY() - offsetY;

            // Set the new position of the image
            imageView.setLayoutX(newX);
            imageView.setLayoutY(newY);

            event.consume();  // Consume the event to prevent default behavior
        });

        // MouseReleased: drop the image onto the grid
        imageView.setOnMouseReleased(event -> {
            boolean droppedInCell = false;
            double sceneX = event.getSceneX();
            double sceneY = event.getSceneY();
            AnchorPane root = (AnchorPane) imageView.getScene().getRoot();

            // Check if the drop is inside any cell of the grid
            for (Node node : grid.getChildren()) {
                if (node instanceof Pane cell) {
                    Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                    if (cellBounds.contains(sceneX, sceneY)) {
                        // Check if the cell is already occupied by an ImageView (component)
                        Integer rowIndex = GridPane.getRowIndex(cell);
                        Integer colIndex = GridPane.getColumnIndex(cell);
                        if (rowIndex == null) rowIndex = 0;  // Default if row index is not set
                        if (colIndex == null) colIndex = 0;  // Default if column index is not set

                        // Check if the cell already has a child (component)
                        if (!cell.getChildren().isEmpty()) {
                            // The cell is occupied, so prevent dropping the component here
                            System.out.println("Cell already occupied.");
                            break;
                        }

                        // If dropped inside a cell and the cell is not occupied, move the image into the cell
                        root.getChildren().remove(imageView);
                        cell.getChildren().add(imageView);

                        // Center the image inside the cell
                        imageView.setLayoutX((cell.getWidth() - imageView.getFitWidth()) / 2);
                        imageView.setLayoutY((cell.getHeight() - imageView.getFitHeight()) / 2);

                        // Print the row and column index of the dropped cell
                        System.out.println("Component dropped in cell: (" + rowIndex + ", " + colIndex + ")");

                        droppedInCell = true;
                        break;
                    }
                }
            }

            // If the drop was not inside any cell, return the image to its original position
            if (!droppedInCell) {
                Object originalParent = imageView.getProperties().get("originalParent");
                if (imageView.getParent() != originalParent && originalParent instanceof Pane) {
                    root.getChildren().remove(imageView);
                    ((Pane) originalParent).getChildren().add(imageView);
                }
                imageView.setLayoutX((double) imageView.getProperties().get("originalLayoutX"));
                imageView.setLayoutY((double) imageView.getProperties().get("originalLayoutY"));
            }
            event.consume();  // Consume the event to prevent default behavior
        });
    }

    public void placeAndPickNewComponent() {
        generateComponent("single-cannon1.jpg");
    }
}
