package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

public class PopulatingView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;
    private boolean orangeAlienAdded = false;
    private boolean purpleAlienAdded = false;

    @FXML
    public StackPane populatingPane;

    @FXML
    public ImageView spaceShipImage;

    @FXML
    public Label populatingSectionTitle;

    @FXML
    public Label populatingSectionDesc;

    @FXML
    private GridPane spaceshipMatrix;

    // =======================
    // METHODS
    // =======================

    /**
     * Initializes the background
     *
     * @author Lorenzo
     * @param levelGame is the game level
     */
    public void initBackground(int levelGame) {

        // Initialize background
        Image img = null;
        if(levelGame == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-1.png")));

        else if(levelGame == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-2.png")));

        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,   // horizontal repetition
                BackgroundRepeat.NO_REPEAT,   // vertical repetition
                BackgroundPosition.CENTER,    // position
                new BackgroundSize(
                        100, 100, true, true, false, true
                )
        );

        Background background = new Background(backgroundImage);
        populatingPane.setBackground(background);
    }

    /**
     * Initializes the spaceship matrix
     *
     * @author Gabriele
     * @param levelShip is the spaceship level
     */
    public void initSpaceship(int levelShip) {

        // Spaceship matrix
        int sizeX = 5;
        int sizeY = 5;

        if (levelShip == 2){
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                if(BuildingData.getCellMask(col, row))
                    cell.setId("spaceshipCell");

                spaceshipMatrix.add(cell, col, row);
            }
        }

        Image image = new Image(String.valueOf(MainClient.class.getResource("img/cardboard/spaceship" + levelShip + ".jpg")));
        spaceShipImage.setImage(image);
    }

    /**
     * Updates the spaceship matrix with the current spaceship
     *
     * @author Stefano
     * @param ship is the spaceship to be updated
     */
    public void updateSpaceship(Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();

        GridPane shipGrid = spaceshipMatrix;

        int incorrectlyPlacedCount = 0;

        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(COMPONENT_SIZE);
                    iv.setFitHeight(COMPONENT_SIZE);
                    iv.setPreserveRatio(true);
                    cell.getChildren().add(iv);

                    switch (comp.getRotation()) {
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

                    // Checks if the component can host alien
                    if (comp.getType().equals(ComponentType.HOUSING_UNIT)) {
                        if (((HousingUnit) comp).getAllowOrangeAlien() && !ship.getAlienOrange()){
                            Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
                            overlay.setFill(Color.rgb(255, 165, 0, 0.25));
                            cell.getChildren().add(overlay);

                            cell.getChildren().get(1).toFront();

                            cell.setStyle("-fx-cursor: hand;");

                            cell.setOnMouseClicked(event -> {
                                orangeAlienFill(comp);
                            });
                        }
                        else if (((HousingUnit) comp).getAllowPurpleAlien() && !ship.getAlienPurple()){
                            Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
                            overlay.setFill(Color.rgb(160, 32, 240, 0.25));
                            cell.getChildren().add(overlay);

                            cell.getChildren().get(1).toFront();

                            cell.setStyle("-fx-cursor: hand;");

                            cell.setOnMouseClicked(event -> {
                                purpleAlienFill(comp);
                            });
                        }

                    } else {
                        cell.setStyle("-fx-cursor: default;");
                    }
                }

                shipGrid.add(cell, col, row);
            }
        }

        // Checks if incorrectly placed components are present
        if (incorrectlyPlacedCount == 0) {
            populatingSectionTitle.setText("YOUR ALIENS ARE READY!");
            populatingSectionDesc.setText("You have to wait for the other players to populate their spaceship...");
        }

    }

    public void orangeAlienFill(Component comp) {
        // check if an orange alien was already added
        if (orangeAlienAdded) {
            return;
        }

        ((HousingUnit) comp).setAlienOrange(true);

        Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/OrangeAlien.png")));

        Pane cell = getCellFromSpaceshipMatrix(comp.getX(), comp.getY());

        Pane subCell = new Pane();
        subCell.setPrefSize(COMPONENT_SIZE * 0.8, COMPONENT_SIZE * 0.8);
        subCell.setLayoutX((COMPONENT_SIZE - subCell.getPrefWidth()) / 2);
        subCell.setLayoutY((COMPONENT_SIZE - subCell.getPrefHeight()) / 2);
        subCell.setId("alienSubCell");

        ImageView alienImageView = new ImageView(alienImage);
        alienImageView.setFitWidth(subCell.getPrefWidth());
        alienImageView.setFitHeight(subCell.getPrefHeight());
        alienImageView.setPreserveRatio(true);

        subCell.getChildren().add(alienImageView);

        cell.getChildren().add(subCell);

        // Bring alien image to front
        alienImageView.toFront();

        resetHighlightedCells(Color.rgb(255, 165, 0, 0.25));

        orangeAlienAdded = true;
    }

    public void purpleAlienFill(Component comp) {
        // check if an orange alien was already added
        if (purpleAlienAdded) {
            return;
        }

        ((HousingUnit) comp).setAlienPurple(true);

        Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/PurpleAlien.png")));

        // Find the cell and add the image
        Pane cell = getCellFromSpaceshipMatrix(comp.getX(), comp.getY());

        Pane subCell = new Pane();
        subCell.setPrefSize(COMPONENT_SIZE * 0.8, COMPONENT_SIZE * 0.8);
        subCell.setLayoutX((COMPONENT_SIZE - subCell.getPrefWidth()) / 2);
        subCell.setLayoutY((COMPONENT_SIZE - subCell.getPrefHeight()) / 2);
        subCell.setId("alienSubCell");

        ImageView alienImageView = new ImageView(alienImage);
        alienImageView.setFitWidth(subCell.getPrefWidth());
        alienImageView.setFitHeight(subCell.getPrefHeight());
        alienImageView.setPreserveRatio(true);

        subCell.getChildren().add(alienImageView);

        cell.getChildren().add(subCell);

        // Bring alien image to front
        alienImageView.toFront();

        resetHighlightedCells(Color.rgb(160, 32, 240, 0.25));

        purpleAlienAdded = true;
    }

    private Pane getCellFromSpaceshipMatrix(int x, int y) {
        for (Node node : spaceshipMatrix.getChildren()) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return (Pane) node;
            }
        }
        return null;
    }

    private void resetHighlightedCells(Color highlightColor) {
        for (Node node : spaceshipMatrix.getChildren()) {
            if (node instanceof Pane) {
                Pane cell = (Pane) node;

                cell.getChildren().removeIf(child ->
                        child instanceof Rectangle &&
                                ((Rectangle) child).getFill().equals(highlightColor)
                );
            }
        }
    }
}