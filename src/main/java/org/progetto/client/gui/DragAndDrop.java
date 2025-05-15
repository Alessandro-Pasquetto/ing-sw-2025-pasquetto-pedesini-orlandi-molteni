package org.progetto.client.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

        Pane root = (Pane) componentPane.getScene().getRoot();

        componentPane.setManaged(false);

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
        boolean isValidDrop = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        Pane root = (Pane) componentPane.getScene().getRoot();

        componentPane.setManaged(false);

        //TODO: Check if i'm trying to to discard a booked component, in this case revert its positoin in its booking cell

        ImageView trash = PageController.getBuildingView().getTrash();
        if (trash.localToScene(trash.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
            PageController.getBuildingView().discardComponent();
        }

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getBuildingView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {
                    // Check if the cell is already occupied by a Pane (component)
                    Integer rowIndex = GridPane.getRowIndex(cell);
                    Integer colIndex = GridPane.getColumnIndex(cell);

                    // Check if the cell already has a child (component)
                    if (!cell.getChildren().isEmpty()) {
                        // The cell is occupied, so prevent dropping the component here
                        System.out.println("Cell already occupied");
                        Alerts.showPopUp("Cell already occupied!", true);
                        break;
                    }

                    if(!BuildingData.getCellMask(colIndex, rowIndex))
                        break;

                    // If dropped inside a cell and the cell is not occupied, move the image into the cell
                    root.getChildren().remove(componentPane);
                    cell.getChildren().add(componentPane);

                    // Center the image inside the cell
                    componentPane.setLayoutX((cell.getWidth() - componentPane.getPrefWidth()) / 2);
                    componentPane.setLayoutY((cell.getHeight() - componentPane.getPrefHeight()) / 2);

                    // save the row and column index of the dropped cell
                    BuildingData.setXHandComponent(colIndex);
                    BuildingData.setYHandComponent(rowIndex);

                    isValidDrop = true;
                    break;
                }
            }
        }

        // Check if the drop is inside any cell of the bookedArray
        for (Node node : PageController.getBuildingView().getBookedArray().getChildren()) {
            if (node instanceof Pane cell) {
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {

                    // Check if the cell is already occupied by a Pane (component)
                    Integer colIndex = GridPane.getColumnIndex(cell);

                    // Check if the cell already has a child (component)
                    if (!cell.getChildren().isEmpty()) {
                        // The cell is occupied, so prevent dropping the component here
                        System.out.println("Cell already occupied");
                        Alerts.showPopUp("Cell already occupied!", true);

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

                    isValidDrop = true;
                    break;
                }
            }
        }

        // If the drop was inside a booking cell or the booked component drop is not valid
        if(BuildingData.getYHandComponent() == -1){
            componentPane.setRotate(componentPane.getRotate() - 90 * BuildingData.getRHandComponent());

            GameData.getSender().bookComponent(BuildingData.getXHandComponent());
        }

        // If the drop was inside a cell
        if (!isValidDrop){

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

    public static void setOnMousePressedForBookedComponent(Pane componentPane) {
        // Set his pressFunction
        componentPane.setOnMousePressed(event2 -> {

            BuildingData.setTempPickingBookedComponent(componentPane);
            onMousePressedFunctionComponent(componentPane, event2);

            double sceneX2 = event2.getSceneX();
            double sceneY2 = event2.getSceneY();

            // Check if the drop is inside any cell of the grid
            for (Node node : PageController.getBuildingView().getBookedArray().getChildren()) {
                if (node instanceof Pane cell) {
                    Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                    if (cellBounds.contains(sceneX2, sceneY2)) {

                        Integer colIndex = GridPane.getColumnIndex(cell);
                        BuildingData.setTempXPickingBooked(colIndex);

                        if(BuildingData.getHandComponent() == null)
                            GameData.getSender().pickBookedComponent(colIndex);

                        else if(BuildingData.getXHandComponent() != -1)
                            GameData.getSender().placeHandComponentAndPickBookedComponent(BuildingData.getXHandComponent(), BuildingData.getYHandComponent(), BuildingData.getRHandComponent(), colIndex);

                        break;
                    }
                }
            }

            event2.consume();  // Consume the event to prevent default behavior
        });

        // Disable dragged and released
        componentPane.setOnMouseDragged(null);
        componentPane.setOnMouseReleased(null);
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

        componentPane.getStyleClass().add("draggable");
    }

    // Method to disable drag-and-drop for a Pane
    public static void disableDragAndDropComponent(Pane componentPane) {
        componentPane.setOnMousePressed(null);
        componentPane.setOnMouseDragged(null);
        componentPane.setOnMouseReleased(null);
        componentPane.getStyleClass().remove("draggable");
    }

    // =======================
    // BOXES
    // =======================

    // DragAndDrop functions
    private static void onMousePressedFunctionItems(ImageView itemImage, MouseEvent event){
        // Store initial scene coordinates to detect drag
        itemImage.getProperties().put("initialSceneX", event.getSceneX());
        itemImage.getProperties().put("initialSceneY", event.getSceneY());

        // Store the original parent and local layout coordinates
        itemImage.getProperties().put("originalParent", itemImage.getParent());
        itemImage.getProperties().put("originalLayoutX", itemImage.getLayoutX());
        itemImage.getProperties().put("originalLayoutY", itemImage.getLayoutY());

        // Calculate the position in the scene's coordinate system
        Bounds boundsInScene = itemImage.localToScene(itemImage.getBoundsInLocal());

        Pane root = (Pane) itemImage.getScene().getRoot();

        itemImage.setManaged(false);

        // If the node is not already in the root, move it there
        if (itemImage.getParent() != root) {
            ((Pane) itemImage.getParent()).getChildren().remove(itemImage);
            root.getChildren().add(itemImage);
        }

        // Convert the scene coordinates to root's local coordinates
        Point2D localPos = root.sceneToLocal(boundsInScene.getMinX(), boundsInScene.getMinY());
        itemImage.setLayoutX(localPos.getX());
        itemImage.setLayoutY(localPos.getY());

        // Store the drag offset (distance from mouse to top-left of the node)
        itemImage.getProperties().put("dragOffsetX", event.getSceneX() - boundsInScene.getMinX());
        itemImage.getProperties().put("dragOffsetY", event.getSceneY() - boundsInScene.getMinY());

        event.consume();
    }

    private static void onMouseDraggedFunctionItems(ImageView itemImage, MouseEvent event){
        // Get the initial offset saved during MousePressed
        double offsetX = (double) itemImage.getProperties().get("dragOffsetX");
        double offsetY = (double) itemImage.getProperties().get("dragOffsetY");

        // Calculate the new position of the image based on the mouse's current position
        double newX = event.getSceneX() - offsetX;
        double newY = event.getSceneY() - offsetY;

        // Set the new position of the image
        itemImage.setLayoutX(newX);
        itemImage.setLayoutY(newY);

        event.consume();  // Consume the event to prevent default behavior
    }

    private static void onMouseReleasedFunctionItems(ImageView itemImage, MouseEvent event, String targetId){
        boolean isValidDrop = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        Pane root = (Pane) itemImage.getScene().getRoot();

        itemImage.setManaged(false);

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getBuildingView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {
                // Check if the drop is inside of that cell
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {

                    // Check if the cell contains a component
                    if (!cell.getChildren().isEmpty()) {

                        Integer rowIndex = GridPane.getRowIndex(cell);
                        Integer colIndex = GridPane.getColumnIndex(cell);

                        Node node2 = cell.getChildren().get(0);
                        if(node2 instanceof Pane componentPane) {
                            for (Node node3 : componentPane.getChildren()) {
                                if(node3 instanceof Pane slot) {
                                    // Check if the drop is inside of that slot
                                    Bounds slotBounds = slot.localToScene(slot.getBoundsInLocal());
                                    if (slotBounds.contains(sceneX, sceneY)) {

                                        if(!slot.getChildren().isEmpty()){
                                            System.out.println("Slot already occupied.");
                                            break;
                                        }

                                        if(targetId.equals("boxSlot") && slot.getId().equals("boxSlot")){
                                            // If dropped inside a slot and the slot is not occupied, move the image into the slot
                                            root.getChildren().remove(itemImage);
                                            slot.getChildren().add(itemImage);

                                            // Center the image inside the slot
                                            itemImage.setLayoutX((slot.getWidth() - itemImage.getFitWidth()) / 2);
                                            itemImage.setLayoutY((slot.getHeight() - itemImage.getFitHeight()) / 2);

                                            //todo handle idx
                                            System.out.println("Component x: " + colIndex + " y: " + rowIndex + " released box in slot " + slot.getProperties().get("idx"));

                                            isValidDrop = true;
                                        }
                                        else if(targetId.equals("crewSlot") && slot.getId().equals("crewSlot")){
                                            // If dropped inside a slot and the slot is not occupied, move the image into the slot
                                            root.getChildren().remove(itemImage);
                                            slot.getChildren().add(itemImage);

                                            // Center the image inside the slot
                                            itemImage.setLayoutX((slot.getWidth() - itemImage.getFitWidth()) / 2);
                                            itemImage.setLayoutY((slot.getHeight() - itemImage.getFitHeight()) / 2);

                                            //todo handle idx
                                            System.out.println("Component x: " + colIndex + " y: " + rowIndex + " released crew in slot " + slot.getProperties().get("idx"));

                                            isValidDrop = true;
                                        }
                                        else if(targetId.equals("batterySlot") && slot.getId().equals("batterySlot")){
                                            // If dropped inside a slot and the slot is not occupied, move the image into the slot
                                            root.getChildren().remove(itemImage);
                                            slot.getChildren().add(itemImage);

                                            // Center the image inside the slot
                                            itemImage.setLayoutX((slot.getWidth() - itemImage.getFitWidth()) / 2);
                                            itemImage.setLayoutY((slot.getHeight() - itemImage.getFitHeight()) / 2);

                                            //todo handle idx
                                            System.out.println("Component x: " + colIndex + " y: " + rowIndex + " released battery in slot " + slot.getProperties().get("idx"));

                                            isValidDrop = true;
                                        }

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
        if (!isValidDrop) {
            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = itemImage.getProperties().get("originalParent");
            if (itemImage.getParent() != originalParent && originalParent instanceof Pane) {
                root.getChildren().remove(itemImage);
                ((Pane) originalParent).getChildren().add(itemImage);
            }
            itemImage.setLayoutX((double) itemImage.getProperties().get("originalLayoutX"));
            itemImage.setLayoutY((double) itemImage.getProperties().get("originalLayoutY"));
        }
        event.consume();  // Consume the event to prevent default behavior
    }

    // Make draggable
    public static void enableDragAndDropItems(ImageView itemImage, String targetId) {
        // Make sure the image responds to mouse events
        itemImage.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        itemImage.setOnMousePressed(event -> {
            onMousePressedFunctionItems(itemImage, event);
        });

        // MouseDragged: update image position based on drag
        itemImage.setOnMouseDragged(event -> {
            onMouseDraggedFunctionItems(itemImage, event);
        });

        // MouseReleased: drop the image onto the grid
        itemImage.setOnMouseReleased(event -> {
            onMouseReleasedFunctionItems(itemImage, event, targetId);
        });

        itemImage.getStyleClass().add("draggable");
    }

    // Method to disable drag-and-drop for a Pane
    public static void disableDragAndDropItems(ImageView itemImage) {
        itemImage.setOnMousePressed(null);
        itemImage.setOnMouseDragged(null);
        itemImage.setOnMouseReleased(null);

        itemImage.getStyleClass().remove("draggable");
    }
}