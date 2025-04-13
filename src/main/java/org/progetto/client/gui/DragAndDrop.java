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
import org.progetto.client.model.GameData;

public class DragAndDrop {

    // =======================
    // COMPONENT
    // =======================

    // DragAndDrop functions
    private static void onMousePressedFunctionComponent(Pane componentPane, MouseEvent event){
        // Store initial scene coordinates to detect drag
        componentPane.getProperties().put("initialSceneX", event.getSceneX());
        componentPane.getProperties().put("initialSceneY", event.getSceneY());

        // Store the original parent and local layout coordinates
        componentPane.getProperties().put("originalParent", componentPane.getParent());
        componentPane.getProperties().put("originalLayoutX", componentPane.getLayoutX());
        componentPane.getProperties().put("originalLayoutY", componentPane.getLayoutY());

        // Calculate the position in the scene's coordinate system
        Bounds boundsInScene = componentPane.localToScene(componentPane.getBoundsInLocal());

        AnchorPane root = (AnchorPane) componentPane.getScene().getRoot();

        // If the node is not already in the root, move it there
        if (componentPane.getParent() != root) {
            ((Pane) componentPane.getParent()).getChildren().remove(componentPane);
            root.getChildren().add(componentPane);
        }

        // Convert the scene coordinates to root's local coordinates
        Point2D localPos = root.sceneToLocal(boundsInScene.getMinX(), boundsInScene.getMinY());
        componentPane.setLayoutX(localPos.getX());
        componentPane.setLayoutY(localPos.getY());

        // Store the drag offset (distance from mouse to top-left of the node)
        componentPane.getProperties().put("dragOffsetX", event.getSceneX() - boundsInScene.getMinX());
        componentPane.getProperties().put("dragOffsetY", event.getSceneY() - boundsInScene.getMinY());

        event.consume();
    }

    private static void onMouseDraggedFunctionComponent(Pane componentPane, MouseEvent event){
        // Get the initial offset saved during MousePressed
        double offsetX = (double) componentPane.getProperties().get("dragOffsetX");
        double offsetY = (double) componentPane.getProperties().get("dragOffsetY");

        // Calculate the new position of the image based on the mouse's current position
        double newX = event.getSceneX() - offsetX;
        double newY = event.getSceneY() - offsetY;

        // Set the new position of the image
        componentPane.setLayoutX(newX);
        componentPane.setLayoutY(newY);

        event.consume();  // Consume the event to prevent default behavior
    }

    private static void onMouseReleasedFunctionComponent(Pane componentPane, MouseEvent event){
        boolean droppedInCell = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        AnchorPane root = (AnchorPane) componentPane.getScene().getRoot();

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getGameView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {
                    // Check if the cell is already occupied by an Pane (component)
                    Integer rowIndex = GridPane.getRowIndex(cell);
                    Integer colIndex = GridPane.getColumnIndex(cell);

                    // Check if the cell already has a child (component)
                    if (!cell.getChildren().isEmpty()) {
                        // The cell is occupied, so prevent dropping the component here
                        System.out.println("Cell already occupied.");
                        break;
                    }

                    // If dropped inside a cell and the cell is not occupied, move the image into the cell
                    root.getChildren().remove(componentPane);
                    cell.getChildren().add(componentPane);

                    // Center the image inside the cell
                    componentPane.setLayoutX((cell.getWidth() - componentPane.getPrefWidth()) / 2);
                    componentPane.setLayoutY((cell.getHeight() - componentPane.getPrefHeight()) / 2);

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
                    // Check if the cell is already occupied by an Pane (component)
                    Integer colIndex = GridPane.getColumnIndex(cell);

                    // Check if the cell already has a child (component)
                    if (!cell.getChildren().isEmpty()) {
                        // The cell is occupied, so prevent dropping the component here
                        System.out.println("Cell already occupied.");
                        break;
                    }

                    // If dropped inside a cell and the cell is not occupied, move the image into the cell
                    root.getChildren().remove(componentPane);
                    cell.getChildren().add(componentPane);

                    // Center the image inside the cell
                    componentPane.setLayoutX((cell.getWidth() - componentPane.getPrefWidth()) / 2);
                    componentPane.setLayoutY((cell.getHeight() - componentPane.getPrefHeight()) / 2);

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

            // If the drop was inside a booking cell
            if(BuildingData.getYHandComponent() == -1){
                componentPane.setRotate(componentPane.getRotate() - 90 * BuildingData.getRHandComponent());

                GameData.getSender().bookComponent(BuildingData.getXHandComponent());

                // Set his pressFunction
                componentPane.setOnMousePressed(event2 -> {

                    BuildingData.setTempBookedComponent(componentPane);
                    onMousePressedFunctionComponent(componentPane, event2);

                    double sceneX2 = event2.getSceneX();
                    double sceneY2 = event2.getSceneY();

                    // Check if the drop is inside any cell of the grid
                    for (Node node : PageController.getGameView().getBookedArray().getChildren()) {
                        if (node instanceof Pane cell) {
                            Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                            if (cellBounds.contains(sceneX2, sceneY2)) {

                                Integer colIndex = GridPane.getColumnIndex(cell);

                                if(BuildingData.getHandComponent() == null){
                                    GameData.getSender().pickBookedComponent(colIndex);

                                }else if(BuildingData.getXHandComponent() != -1) {
                                    GameData.getSender().placeHandComponentAndPickBookedComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), colIndex);
                                }

                                break;
                            }
                        }
                    }

                    event2.consume();  // Consume the event to prevent default behavior
                });

                // Disable dragged and released
                componentPane.setOnMouseDragged(null);
                componentPane.setOnMouseReleased(null);
                BuildingData.resetHandComponent();
            }
        }else{
            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = componentPane.getProperties().get("originalParent");
            if (componentPane.getParent() != originalParent && originalParent instanceof Pane) {
                root.getChildren().remove(componentPane);
                ((Pane) originalParent).getChildren().add(componentPane);
            }
            componentPane.setLayoutX((double) componentPane.getProperties().get("originalLayoutX"));
            componentPane.setLayoutY((double) componentPane.getProperties().get("originalLayoutY"));
        }
        event.consume();  // Consume the event to prevent default behavior
    }

    // Make draggable
    public static void enableDragAndDropComponent(Pane componentPane) {
        // Make sure the image responds to mouse events
        componentPane.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        componentPane.setOnMousePressed(event -> {
            onMousePressedFunctionComponent(componentPane, event);
        });

        // MouseDragged: update image position based on drag
        componentPane.setOnMouseDragged(event -> {
            onMouseDraggedFunctionComponent(componentPane, event);
        });

        // MouseReleased: drop the image onto the grid
        componentPane.setOnMouseReleased(event -> {
            onMouseReleasedFunctionComponent(componentPane, event);
        });
    }

    // Method to disable drag-and-drop for an Pane
    public static void disableDragAndDropComponent(Pane componentPane) {
        componentPane.setOnMousePressed(null);
        componentPane.setOnMouseDragged(null);
        componentPane.setOnMouseReleased(null);
    }


    // =======================
    // BOXES
    // =======================

    // DragAndDrop functions
    private static void onMousePressedFunctionBoxes(ImageView boxImage, MouseEvent event){
        // Store initial scene coordinates to detect drag
        boxImage.getProperties().put("initialSceneX", event.getSceneX());
        boxImage.getProperties().put("initialSceneY", event.getSceneY());

        // Store the original parent and local layout coordinates
        boxImage.getProperties().put("originalParent", boxImage.getParent());
        boxImage.getProperties().put("originalLayoutX", boxImage.getLayoutX());
        boxImage.getProperties().put("originalLayoutY", boxImage.getLayoutY());

        // Calculate the position in the scene's coordinate system
        Bounds boundsInScene = boxImage.localToScene(boxImage.getBoundsInLocal());

        AnchorPane root = (AnchorPane) boxImage.getScene().getRoot();

        // If the node is not already in the root, move it there
        if (boxImage.getParent() != root) {
            ((Pane) boxImage.getParent()).getChildren().remove(boxImage);
            root.getChildren().add(boxImage);
        }

        // Convert the scene coordinates to root's local coordinates
        Point2D localPos = root.sceneToLocal(boundsInScene.getMinX(), boundsInScene.getMinY());
        boxImage.setLayoutX(localPos.getX());
        boxImage.setLayoutY(localPos.getY());

        // Store the drag offset (distance from mouse to top-left of the node)
        boxImage.getProperties().put("dragOffsetX", event.getSceneX() - boundsInScene.getMinX());
        boxImage.getProperties().put("dragOffsetY", event.getSceneY() - boundsInScene.getMinY());

        event.consume();
    }

    private static void onMouseDraggedFunctionBoxes(ImageView boxImage, MouseEvent event){
        // Get the initial offset saved during MousePressed
        double offsetX = (double) boxImage.getProperties().get("dragOffsetX");
        double offsetY = (double) boxImage.getProperties().get("dragOffsetY");

        // Calculate the new position of the image based on the mouse's current position
        double newX = event.getSceneX() - offsetX;
        double newY = event.getSceneY() - offsetY;

        // Set the new position of the image
        boxImage.setLayoutX(newX);
        boxImage.setLayoutY(newY);

        event.consume();  // Consume the event to prevent default behavior
    }

    private static void onMouseReleasedFunctionBoxes(ImageView boxImage, MouseEvent event){
        boolean droppedInCell = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        AnchorPane root = (AnchorPane) boxImage.getScene().getRoot();

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getGameView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                // Check if the drop is inside of that cell
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {

                    // Check if the cell contains a component
                    if (!cell.getChildren().isEmpty()) {
                        Node node2 = cell.getChildren().get(0);
                        if(node2 instanceof Pane componentPane) {
                            for (Node node3 : componentPane.getChildren()) {
                                if(node3 instanceof Pane slot) {
                                    // Check if the drop is inside of that slot
                                    Bounds slotBounds = slot.localToScene(slot.getBoundsInLocal());
                                    if (slotBounds.contains(sceneX, sceneY)) {

                                        if(!slot.getChildren().isEmpty()){
                                            System.out.println("Slot already occupied.");
                                            System.out.println(slot.getChildren().toString());
                                            break;
                                        }

                                        // If dropped inside a slot and the slot is not occupied, move the image into the slot
                                        root.getChildren().remove(boxImage);
                                        slot.getChildren().add(boxImage);

                                        // Center the image inside the slot
                                        boxImage.setLayoutX((slot.getWidth() - boxImage.getFitWidth()) /2);
                                        boxImage.setLayoutY((slot.getHeight() - boxImage.getFitHeight()) /2);

                                        //todo handle idx
                                        System.out.println("Released box in slot " + slot.getProperties().get("idx"));

                                        droppedInCell = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // If the drop was inside a cell
        if (!droppedInCell) {
            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = boxImage.getProperties().get("originalParent");
            if (boxImage.getParent() != originalParent && originalParent instanceof Pane) {
                root.getChildren().remove(boxImage);
                ((Pane) originalParent).getChildren().add(boxImage);
            }
            boxImage.setLayoutX((double) boxImage.getProperties().get("originalLayoutX"));
            boxImage.setLayoutY((double) boxImage.getProperties().get("originalLayoutY"));
        }
        event.consume();  // Consume the event to prevent default behavior
    }

    // Make draggable
    public static void enableDragAndDropBoxes(ImageView boxImage) {
        // Make sure the image responds to mouse events
        boxImage.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        boxImage.setOnMousePressed(event -> {
            onMousePressedFunctionBoxes(boxImage, event);
        });

        // MouseDragged: update image position based on drag
        boxImage.setOnMouseDragged(event -> {
            onMouseDraggedFunctionBoxes(boxImage, event);
        });

        // MouseReleased: drop the image onto the grid
        boxImage.setOnMouseReleased(event -> {
            onMouseReleasedFunctionBoxes(boxImage, event);
        });
    }

    // Method to disable drag-and-drop for an Pane
    public static void disableDragAndDropBoxes(ImageView boxImage) {
        boxImage.setOnMousePressed(null);
        boxImage.setOnMouseDragged(null);
        boxImage.setOnMouseReleased(null);
    }
}