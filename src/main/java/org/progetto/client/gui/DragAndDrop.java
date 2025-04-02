package org.progetto.client.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.progetto.client.model.BuildingData;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;

public class DragAndDrop {

    // DragAndDrop functions
    private static void onMousePressedFunction(ImageView imageView, MouseEvent event){
        // Save initial scene coordinates for drag detection
        imageView.getProperties().put("initialSceneX", event.getSceneX());
        imageView.getProperties().put("initialSceneY", event.getSceneY());

        // Save the original parent and layout coordinates
        imageView.getProperties().put("originalParent", imageView.getParent());
        imageView.getProperties().put("originalLayoutX", imageView.getLayoutX());
        imageView.getProperties().put("originalLayoutY", imageView.getLayoutY());

        Point2D scenePos = imageView.getParent().localToScene(imageView.getLayoutX(), imageView.getLayoutY()); // Convert local position to scene coordinates
        AnchorPane root = (AnchorPane) imageView.getScene().getRoot();


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
    }

    private static void onMouseDraggedFunction(ImageView imageView, MouseEvent event){
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
    }

    private static void onMouseReleasedFunction(ImageView imageView, MouseEvent event){
        boolean droppedInCell = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        AnchorPane root = (AnchorPane) imageView.getScene().getRoot();

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getGameView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {
                    // Check if the cell is already occupied by an ImageView (component)
                    Integer rowIndex = GridPane.getRowIndex(cell);
                    Integer colIndex = GridPane.getColumnIndex(cell);

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

                    // save the row and column index of the dropped cell
                    BuildingData.setXHandComponent(colIndex);
                    BuildingData.setYHandComponent(rowIndex);

                    droppedInCell = true;
                    break;
                }
            }
        }

        // Check if the drop is inside any cell of the bookedArray
        for (Node node : PageController.getGameView().getBookedArray().getChildren()) {
            if (node instanceof Pane cell) {
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {
                    // Check if the cell is already occupied by an ImageView (component)
                    Integer colIndex = GridPane.getColumnIndex(cell);

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

                    // save the row and column index of the dropped cell
                    BuildingData.setXHandComponent(colIndex);
                    BuildingData.setYHandComponent(-1);

                    droppedInCell = true;
                    break;
                }
            }
        }

        // If the drop was inside a cell
        if (droppedInCell) {
            System.out.println("x: " + BuildingData.getxHandComponent() + " y: " + BuildingData.getyHandComponent());

            // If the drop was inside a booking cell
            if(BuildingData.getyHandComponent() == -1){
                imageView.setRotate(imageView.getRotate() - 90 * BuildingData.getrHandComponent());

                if(HandlerMessage.getIsSocket())
                    SocketClient.bookComponent(BuildingData.getxHandComponent());
                else
                    RmiClientSender.bookComponent(BuildingData.getxHandComponent());

                // Set his pressFunction
                imageView.setOnMousePressed(event2 -> {

                    BuildingData.setTempBookedComponent(imageView);
                    onMousePressedFunction(imageView, event2);

                    double sceneX2 = event2.getSceneX();
                    double sceneY2 = event2.getSceneY();

                    // Check if the drop is inside any cell of the grid
                    for (Node node : PageController.getGameView().getBookedArray().getChildren()) {
                        if (node instanceof Pane cell) {
                            Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                            if (cellBounds.contains(sceneX2, sceneY2)) {

                                Integer colIndex = GridPane.getColumnIndex(cell);

                                if(BuildingData.getHandComponent() == null){
                                    if(HandlerMessage.getIsSocket())
                                        SocketClient.pickBookedComponent(colIndex);
                                    else
                                        RmiClientSender.pickBookedComponent(colIndex);;

                                }else if(BuildingData.getxHandComponent() != -1) {
                                    if(HandlerMessage.getIsSocket())
                                        SocketClient.placeHandComponentAndPickBookedComponent(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent(), colIndex);
                                    else
                                        RmiClientSender.placeHandComponentAndPickBookedComponent(BuildingData.getxHandComponent(), BuildingData.getyHandComponent(), BuildingData.getrHandComponent(), colIndex);
                                }

                                break;
                            }
                        }
                    }

                    event2.consume();  // Consume the event to prevent default behavior
                });

                // Disable dragged and released
                imageView.setOnMouseDragged(null);
                imageView.setOnMouseReleased(null);
                BuildingData.resetHandComponent();
            }
        }else{
            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = imageView.getProperties().get("originalParent");
            if (imageView.getParent() != originalParent && originalParent instanceof Pane) {
                root.getChildren().remove(imageView);
                ((Pane) originalParent).getChildren().add(imageView);
            }
            imageView.setLayoutX((double) imageView.getProperties().get("originalLayoutX"));
            imageView.setLayoutY((double) imageView.getProperties().get("originalLayoutY"));
        }
        event.consume();  // Consume the event to prevent default behavior
    }

    // Make draggable
    public static void enableDragAndDrop(ImageView imageView) {
        // Make sure the image responds to mouse events
        imageView.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        imageView.setOnMousePressed(event -> {
            onMousePressedFunction(imageView, event);
        });

        // MouseDragged: update image position based on drag
        imageView.setOnMouseDragged(event -> {
            onMouseDraggedFunction(imageView, event);
        });

        // MouseReleased: drop the image onto the grid
        imageView.setOnMouseReleased(event -> {
            onMouseReleasedFunction(imageView, event);
        });
    }

    // Method to disable drag-and-drop for an ImageView
    public static void disableDragAndDrop(ImageView imageView) {
        imageView.setOnMousePressed(null);
        imageView.setOnMouseDragged(null);
        imageView.setOnMouseReleased(null);
    }
}