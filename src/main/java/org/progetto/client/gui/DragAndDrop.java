package org.progetto.client.gui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.connection.Sender;
import org.progetto.server.model.Game;

public class DragAndDrop {

    // =======================
    // COMPONENT
    // =======================

    /**
     * Enables the drag of a component image when it's clicked
     *
     * @author Alessandro
     * @param componentImage is the dragged component imageView
     * @param event is the MouseEvent to read for the pick
     */
    private static void onMousePressedFunctionComponent(ImageView componentImage, MouseEvent event){

        if (event.getButton() != MouseButton.PRIMARY)
            return;

        componentImage.getProperties().put("dragStartedWithPrimary", true);

        // Store initial scene coordinates to detect drag
        componentImage.getProperties().put("initialSceneX", event.getSceneX());
        componentImage.getProperties().put("initialSceneY", event.getSceneY());

        // Store the original parent and local layout coordinates
        componentImage.getProperties().put("originalParent", componentImage.getParent());
        componentImage.getProperties().put("originalLayoutX", componentImage.getLayoutX());
        componentImage.getProperties().put("originalLayoutY", componentImage.getLayoutY());

        // Calculate the position in the scene's coordinate system
        Bounds boundsInScene = componentImage.localToScene(componentImage.getBoundsInLocal());

        Pane root = (Pane) componentImage.getScene().getRoot();
        componentImage.setManaged(false);

        // If the node is not already in the root, move it there
        if (componentImage.getParent() != root) {
            ((Pane) componentImage.getParent()).getChildren().remove(componentImage);
            root.getChildren().add(componentImage);
        }

        // Convert the scene coordinates to root's local coordinates
        Point2D localPos = root.sceneToLocal(boundsInScene.getMinX(), boundsInScene.getMinY());
        componentImage.setLayoutX(localPos.getX());
        componentImage.setLayoutY(localPos.getY());

        // Store the drag offset (distance from mouse to top-left of the node)
        componentImage.getProperties().put("dragOffsetX", event.getSceneX() - boundsInScene.getMinX());
        componentImage.getProperties().put("dragOffsetY", event.getSceneY() - boundsInScene.getMinY());

        event.consume();
    }


    /**
     * Allows a component pane to be dragged on the left-key clicked
     *
     * @author Alessandro
     * @param componentView is the dragged component imageView
     * @param event is the MouseEvent to read for the drag
     */
    private static void onMouseDraggedFunctionComponent(ImageView componentView, MouseEvent event){

        Boolean startedWithPrimary = (Boolean) componentView.getProperties().get("dragStartedWithPrimary");
        if (startedWithPrimary == null || !startedWithPrimary)
            return;

        // Get the initial offset saved during MousePressed
        double offsetX = (double) componentView.getProperties().get("dragOffsetX");
        double offsetY = (double) componentView.getProperties().get("dragOffsetY");

        // Calculate the new position of the image based on the mouse's current position
        double newX = event.getSceneX() - offsetX;
        double newY = event.getSceneY() - offsetY;

        // Set the new position of the image
        componentView.setLayoutX(newX);
        componentView.setLayoutY(newY);

        event.consume();  // Consume the event to prevent default behavior
    }

    /**
     * Allows a component image to be placed on the left-key released
     *
     * @author Alessandro
     * @param componentImageView is the dragged component imageView
     * @param event is the MouseEvent to read for the release
     */
    private static void onMouseReleasedFunctionComponent(ImageView componentImageView, MouseEvent event){

        if (event.getButton() != MouseButton.PRIMARY)
            return;

        componentImageView.getProperties().put("dragStartedWithPrimary", false);

        boolean isValidDrop = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();
        Pane root = (Pane) componentImageView.getScene().getRoot();

        componentImageView.setManaged(false);

        ImageView trash = PageController.getBuildingView().getTrash();
        if (trash.localToScene(trash.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
            PageController.getBuildingView().discardComponent();
        }

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getBuildingView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {

                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {
                    Integer rowIndex = GridPane.getRowIndex(cell);
                    Integer colIndex = GridPane.getColumnIndex(cell);

                    // Check if the cell already has a child (component)
                    if (!cell.getChildren().isEmpty()) {
                        // The cell is occupied, so prevent dropping the component here
                        System.out.println("Cell already occupied");
                        Alerts.showError("Cell already occupied!", true);
                        break;
                    }

                    if(!BuildingData.getCellMask(colIndex, rowIndex))
                        break;

                    // If dropped inside a cell and the cell is not occupied, move the image into the cell
                    root.getChildren().remove(componentImageView);
                    cell.getChildren().add(componentImageView);

                    // Center the image inside the cell
                    componentImageView.setLayoutX((cell.getWidth() - componentImageView.getFitWidth()) / 2);
                    componentImageView.setLayoutY((cell.getHeight() - componentImageView.getFitHeight()) / 2);

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
                        Alerts.showError("Cell already occupied!", true);

                        break;
                    }

                    // If dropped inside a cell and the cell is not occupied, move the image into the cell
                    root.getChildren().remove(componentImageView);
                    cell.getChildren().add(componentImageView);

                    // Center the image inside the cell
                    componentImageView.setLayoutX((cell.getWidth() - componentImageView.getFitWidth()) / 2);
                    componentImageView.setLayoutY((cell.getHeight() - componentImageView.getFitHeight()) / 2);

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
            componentImageView.setRotate(componentImageView.getRotate() - 90 * BuildingData.getRHandComponent());

            GameData.getSender().bookComponent(BuildingData.getXHandComponent());
        }

        // If the drop was inside a cell
        if (!isValidDrop){

            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = componentImageView.getProperties().get("originalParent");
            if (componentImageView.getParent() != originalParent && originalParent instanceof Pane) {
                root.getChildren().remove(componentImageView);
                ((Pane) originalParent).getChildren().add(componentImageView);
            }
            componentImageView.setLayoutX((double) componentImageView.getProperties().get("originalLayoutX"));
            componentImageView.setLayoutY((double) componentImageView.getProperties().get("originalLayoutY"));
        }

        event.consume();  // Consume the event to prevent default behavior
    }

    /**
     * Allows the drag for booked component ImageView
     *
     * @author Alessandro
     * @param componentImage is the dragged component ImageView
     */
    public static void setOnMousePressedForBookedComponent(ImageView componentImage) {
        // Set his pressFunction
        componentImage.setOnMousePressed(event2 -> {

            if (event2.getButton() != MouseButton.PRIMARY)
                return;

            componentImage.getProperties().put("dragStartedWithPrimary", true);

            BuildingData.setTempPickingBookedComponent(componentImage);
            onMousePressedFunctionComponent(componentImage, event2);

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
        componentImage.setOnMouseDragged(null);
        componentImage.setOnMouseReleased(null);
    }

    /**
     * Makes a component draggable
     *
     * @author Alessandro
     * @param componentImage is the dragged component
     */
    public static void enableDragAndDropComponent(ImageView componentImage) {
        // Make sure the image responds to mouse events
        componentImage.setPickOnBounds(true);

        // MousePressed: save initial coordinates and layout details
        componentImage.setOnMousePressed(event -> {
            onMousePressedFunctionComponent(componentImage, event);
        });

        // MouseDragged: update image position based on drag
        componentImage.setOnMouseDragged(event -> {
            onMouseDraggedFunctionComponent(componentImage, event);
        });

        // MouseReleased: drop the image onto the grid
        componentImage.setOnMouseReleased(event -> {
            onMouseReleasedFunctionComponent(componentImage, event);
        });

        componentImage.getStyleClass().add("draggable");
    }

    /**
     * Disable the drag and drop for component imageView
     *
     * @author Alessandro
     * @param componentImageView is the dragged component imageView
     */
    public static void disableDragAndDropComponent(ImageView componentImageView) {
        componentImageView.setOnMousePressed(null);
        componentImageView.setOnMouseDragged(null);
        componentImageView.setOnMouseReleased(null);
        componentImageView.getStyleClass().remove("draggable");
    }

    // =======================
    // BOXES
    // =======================

    /**
     * Enables the drag of an item ImageView when it's clicked
     *
     * @author Alessandro
     * @param itemImage is the ImageView of the item to be dragged
     * @param event is the MouseEvent to read for the pick
     */
    private static void onMousePressedFunctionItems(ImageView itemImage, MouseEvent event){

        if (event.getButton() != MouseButton.PRIMARY)
            return;

        itemImage.getProperties().put("dragStartedWithPrimary", true);

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


    /**
     * Allows an item ImageView to be dragged on the left-key clicked
     *
     * @author Alessandro
     * @param itemImage is the ImageView of the item to be dragged
     * @param event is the MouseEvent to read for the drag
     */
    private static void onMouseDraggedFunctionItems(ImageView itemImage, MouseEvent event){

        Boolean startedWithPrimary = (Boolean) itemImage.getProperties().get("dragStartedWithPrimary");
        if (startedWithPrimary == null || !startedWithPrimary)
            return;

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


    /**
     * Allows an item ImageView to be placed on the left-key released
     *
     * @author Alessandro
     * @param itemImage is the ImageView of the item to be dragged
     * @param event is the MouseEvent to read for the release
     * @param targetId is the FXML id of the final Object
     */
    private static void onMouseReleasedFunctionItems(ImageView itemImage, MouseEvent event, String targetId){


        if (event.getButton() != MouseButton.PRIMARY)
            return;

        itemImage.getProperties().put("dragStartedWithPrimary", true);

        boolean isValidDrop = false;
        double sceneX = event.getSceneX();
        double sceneY = event.getSceneY();


        Pane root = (Pane) itemImage.getScene().getRoot();
        itemImage.setManaged(false);

        ImageView trash = PageController.getEventView().getBlackHoleContainer();
        if (trash.localToScene(trash.getBoundsInLocal()).contains(event.getSceneX(), event.getSceneY())) {
            if(!(itemImage.getProperties().get("originalParent") instanceof FlowPane)) {
                PageController.getEventView().removeBox(event, itemImage);
                itemImage.getStyleClass().remove("draggable");
                ((Pane) itemImage.getParent()).getChildren().remove(itemImage);
                isValidDrop = true;
            }
        }

        // Check if the drop is inside any cell of the spaceship
        for (Node node : PageController.getEventView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {

                // Check if the drop is inside of that cell
                Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
                if (cellBounds.contains(sceneX, sceneY)) {

                    // Check if the cell contains a component
                    if (!cell.getChildren().isEmpty()) {
                        Integer rowIndex = GridPane.getRowIndex(cell);
                        Integer colIndex = GridPane.getColumnIndex(cell);

                        for (Node node2 : cell.getChildren()) {
                            if(node2 instanceof Pane slot) {

                                // Check if the drop is inside of that slot
                                Bounds slotBounds = slot.localToScene(slot.getBoundsInLocal());
                                if (slotBounds.contains(sceneX, sceneY)) {

                                    if(!slot.getChildren().isEmpty()){
                                        System.out.println("Slot already occupied.");
                                        break;
                                    }

                                    if(targetId.equals("boxSlot") && slot.getId().equals("boxSlot")){

                                        Object originalParent = itemImage.getProperties().get("originalParent");
                                        String storageType = cell.getChildren().get(1).getUserData().toString();
                                        Object[] data = (Object[]) itemImage.getUserData();

                                        if(originalParent instanceof FlowPane) {

                                            int boxIdx = (int) data[0];
                                            int boxVal = (int) data[1];

                                            if (boxVal == 4) {
                                                if (storageType.equals("RED_BOX_STORAGE")) {
                                                    root.getChildren().remove(itemImage);
                                                    slot.getChildren().add(itemImage);
                                                } else {
                                                    break;
                                                }

                                            } else {
                                                root.getChildren().remove(itemImage);
                                                slot.getChildren().add(itemImage);
                                            }

                                            itemImage.setFitWidth(35);
                                            itemImage.setFitHeight(35);

                                            // Center the image inside the slot
                                            itemImage.setLayoutX((slot.getWidth() - itemImage.getFitWidth()) / 2);
                                            itemImage.setLayoutY((slot.getHeight() - itemImage.getFitHeight()) / 2);
                                            itemImage.setRotate(-slot.getParent().getRotate());


                                            GameData.getSender().responseRewardBox(boxIdx, colIndex, rowIndex, (int) slot.getProperties().get("idx"));
                                            System.out.println("Placed box at x:" + colIndex + " y:" + rowIndex + " in slot " + slot.getProperties().get("idx"));

                                            isValidDrop = true;
                                        }

                                        else if(originalParent instanceof Pane originalCell){

                                            int boxVal = (int) data[1];
                                            Integer startingX = GridPane.getColumnIndex(originalCell.getParent());
                                            Integer startingY = GridPane.getRowIndex(originalCell.getParent());
                                            Integer startingIdx = (Integer) originalCell.getProperties().get("idx");
                                            Integer finalIdx = (Integer) slot.getProperties().get("idx");

                                            if (boxVal == 4) {
                                                if (storageType.equals("RED_BOX_STORAGE")) {
                                                    root.getChildren().remove(itemImage);
                                                    slot.getChildren().add(itemImage);
                                                } else {
                                                    break;
                                                }

                                            } else {
                                                root.getChildren().remove(itemImage);
                                                slot.getChildren().add(itemImage);
                                            }

                                            itemImage.setFitWidth(35);
                                            itemImage.setFitHeight(35);

                                            // Center the image inside the slot
                                            itemImage.setLayoutX((slot.getWidth() - itemImage.getFitWidth()) / 2);
                                            itemImage.setLayoutY((slot.getHeight() - itemImage.getFitHeight()) / 2);
                                            itemImage.setRotate(-slot.getParent().getRotate());

                                            GameData.getSender().moveBox(startingX,startingY,startingIdx,colIndex,rowIndex,finalIdx);
                                            System.out.println("Moved box from x: " + startingX + " y: " + startingY + " idx: "+ startingIdx + " to " + "x: "+ colIndex + " y: "+ rowIndex+ " idx: " + finalIdx);

                                            isValidDrop = true;
                                        }
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // If the drop was inside a cell
        if (!isValidDrop) {
            System.out.println("Drop not valid");
            // If the drop was not inside any cell, return the image to its original position
            Object originalParent = itemImage.getProperties().get("originalParent");
            if(originalParent instanceof FlowPane flowPane) {
                System.out.println(itemImage.getProperties());
                flowPane.getChildren().remove(itemImage);
                flowPane.getChildren().add(itemImage);
                itemImage.setLayoutX((double) itemImage.getProperties().get("originalLayoutX"));
                itemImage.setLayoutY((double) itemImage.getProperties().get("originalLayoutY"));
            }
            else if(originalParent instanceof Pane){
                root.getChildren().remove(itemImage);
                ((Pane) originalParent).getChildren().add(itemImage);
                itemImage.setLayoutX((double) itemImage.getProperties().get("originalLayoutX"));
                itemImage.setLayoutY((double) itemImage.getProperties().get("originalLayoutY"));
            }


        }
        event.consume();  // Consume the event to prevent default behavior
    }

    /**
     * Makes an item draggable
     *
     * @author Alessandro
     * @param itemImage is the ImageView of the item to be dragged
     * @param targetSlotId the FXML id of the slot container
     */
    public static void enableDragAndDropItem(ImageView itemImage, String targetSlotId) {

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
            onMouseReleasedFunctionItems(itemImage, event, targetSlotId);
        });

        itemImage.getStyleClass().add("draggable");
    }

    /**
     * Enable drag and drop for all items in the spaceship
     *
     * @author Alessandro
     * @param itemId is the item id
     * @param targetSlotId the FXML id of the slot container
     */
    public static void enableDragAndDropItemsSpaceship(String itemId, String targetSlotId) {
        for (Node node : PageController.getEventView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {

                // Check if the cell contains a component
                if (!cell.getChildren().isEmpty()) {

                    for (Node node2 : cell.getChildren()) {
                        if(node2 instanceof Pane slot) {
                            switch(itemId){
                                case "box":
                                    if(slot.getId().equals("boxSlot")) {

                                        if(slot.getChildren().isEmpty()) continue;

                                        Node node4 = slot.getChildren().get(0);
                                        if(node4 instanceof ImageView itemImage) {
                                            enableDragAndDropItem(itemImage, targetSlotId);
                                        }

                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Disable drag and drop for an Item
     *
     * @author Alessandro
     * @param itemImage is the ImageView of the item to be dragged
     */
    public static void disableDragAndDropItem(ImageView itemImage) {
        itemImage.setOnMousePressed(null);
        itemImage.setOnMouseDragged(null);
        itemImage.setOnMouseReleased(null);

        itemImage.getStyleClass().remove("draggable");
    }

    /**
     * Disable drag and drop for all items in the spaceship
     *
     * @author Alessandro
     * @param itemId is the item id
     */
    public static void disableDragAndDropItemsSpaceship(String itemId) {
        for (Node node : PageController.getEventView().getSpaceshipMatrix().getChildren()) {
            if (node instanceof Pane cell) {

                // Check if the cell contains a component
                if (!cell.getChildren().isEmpty()) {

                    for (Node node2 : cell.getChildren()) {
                        if(node2 instanceof Pane slot) {
                            switch(itemId){
                                case "box":
                                    if(slot.getId().equals("boxSlot")) {

                                        if(slot.getChildren().isEmpty()) continue;

                                        Node node4 = slot.getChildren().get(0);
                                        if(node4 instanceof ImageView itemImage)
                                            disableDragAndDropItem(itemImage);
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