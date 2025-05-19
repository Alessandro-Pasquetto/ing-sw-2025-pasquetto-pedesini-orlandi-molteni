package org.progetto.client.gui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.util.Optional;

public class PopulatingView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;

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
                            iv.setRotate(0);
                            break;
                        case 1:
                            iv.setRotate(90);
                            break;
                        case 2:
                            iv.setRotate(180);
                            break;
                        case 3:
                            iv.setRotate(270);
                            break;
                    }

                    // Adds alien if present
                    if (comp instanceof HousingUnit) {
                        HousingUnit housingUnit = (HousingUnit) comp;

                        if (housingUnit.getHasPurpleAlien()) {
                            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/PurpleAlien.png")));
                            ImageView alienImageView = new ImageView(alienImage);
                            alienImageView.setFitWidth(COMPONENT_SIZE * 0.6);
                            alienImageView.setFitHeight(COMPONENT_SIZE * 0.6);
                            alienImageView.setLayoutX((COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
                            alienImageView.setLayoutY((COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
                            alienImageView.setPreserveRatio(true);
                            cell.getChildren().add(alienImageView);

                        } else if (housingUnit.getHasOrangeAlien()) {
                            Image alienImage = new Image(String.valueOf(MainClient.class.getResource("img/items/OrangeAlien.png")));
                            ImageView alienImageView = new ImageView(alienImage);
                            alienImageView.setFitWidth(COMPONENT_SIZE * 0.6);
                            alienImageView.setFitHeight(COMPONENT_SIZE * 0.6);
                            alienImageView.setLayoutX((COMPONENT_SIZE - alienImageView.getFitWidth()) / 2);
                            alienImageView.setLayoutY((COMPONENT_SIZE - alienImageView.getFitHeight()) / 2);
                            alienImageView.setPreserveRatio(true);
                            cell.getChildren().add(alienImageView);
                        }

                        else if (housingUnit.getCrewCount() == 2) {
                            Image crewImage = new Image(String.valueOf(MainClient.class.getResource("img/items/CrewMate_icon.png")));

                            double imageSize = COMPONENT_SIZE * 0.4;
                            double spacing = COMPONENT_SIZE * 0;

                            double totalWidth = imageSize * 2 + spacing;
                            double startX = (COMPONENT_SIZE - totalWidth) / 2;
                            double centerY = (COMPONENT_SIZE - (imageSize * 3/2)) / 2;

                            ImageView crewImageView1 = new ImageView(crewImage);
                            crewImageView1.setFitWidth(imageSize);
                            crewImageView1.setPreserveRatio(true);
                            crewImageView1.setLayoutX(startX);
                            crewImageView1.setLayoutY(centerY);
                            cell.getChildren().add(crewImageView1);

                            ImageView crewImageView2 = new ImageView(crewImage);
                            crewImageView2.setFitWidth(imageSize);
                            crewImageView2.setPreserveRatio(true);
                            crewImageView2.setLayoutX(startX + imageSize + spacing);
                            crewImageView2.setLayoutY(centerY);
                            cell.getChildren().add(crewImageView2);
                        }
                    }
                }

                shipGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Asks the player if they want to place an alien
     *
     * @author Stefano
     * @param alienColor is the color of the alien
     * @param ship is the spaceship where the alien will be placed
     */
    public void askForAlien(String alienColor, Spaceship ship) {
        Alerts.showYesNoPopup(
                populatingPane,
                "Alien Placement",
                "Do you want to place a " + alienColor + " alien?",
                () -> highlightCellsForAlien(ship, alienColor),
                () -> GameData.getSender().responsePlaceAlien(-1, -1, alienColor)
        );
    }

    /**
     * Highlights the cells where the alien can be placed
     *
     * @author Stefano
     * @param ship is the spaceship where the alien will be placed
     * @param alienColor is the color of the alien
     */
    private void highlightCellsForAlien(Spaceship ship, String alienColor) {
        clearHighlightedCells();

        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();

        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = getCellFromSpaceshipMatrix(col, row);

                if (comp != null && cell != null && comp instanceof HousingUnit) {
                    HousingUnit housingUnit = (HousingUnit) comp;

                    // Highlight cells for the specific color
                    if (alienColor.equals("purple") && housingUnit.getAllowPurpleAlien()) {
                        highlightCellForAlien(cell, comp, Color.rgb(160, 32, 240, 0.3));

                        cell.setOnMouseClicked(event -> {
                            GameData.getSender().responsePlaceAlien(comp.getX(), comp.getY(), alienColor);
                        });

                    } else if (alienColor.equals("orange") && housingUnit.getAllowOrangeAlien()) {
                        highlightCellForAlien(cell, comp, Color.rgb(255, 165, 0, 0.3));

                        cell.setOnMouseClicked(event -> {
                            GameData.getSender().responsePlaceAlien(comp.getX(), comp.getY(), alienColor);
                        });
                    }
                }
            }
        }
    }

    /**
     * Gets the cell from the spaceship matrix
     *
     * @author Stefano
     * @param x is the x coordinate of the cell
     * @param y is the y coordinate of the cell
     * @return the cell from the spaceship matrix
     */
    private Pane getCellFromSpaceshipMatrix(int x, int y) {
        for (Node node : spaceshipMatrix.getChildren()) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return (Pane) node;
            }
        }
        return null;
    }

    /**
     * Highlights the cell for the alien
     *
     * @author Stefano
     * @param cell is the cell to be highlighted
     * @param comp is the component of the cell
     * @param color is the color of the highlight
     */
    private void highlightCellForAlien(Pane cell, Component comp, Color color) {
        Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
        overlay.setFill(color);
        cell.getChildren().add(overlay);

        overlay.toFront();

        cell.setStyle("-fx-cursor: hand;");
    }

    /**
     * Clears the highlighted cells
     *
     * @author Stefano
     */
    private void clearHighlightedCells() {
        spaceshipMatrix.getChildren().forEach(node -> {
            if (node instanceof Pane) {
                Pane cell = (Pane) node;
                cell.getChildren().removeIf(child -> child instanceof Rectangle);
                cell.setStyle("");
            }
        });
    }

    /**
     * Updates scene labels
     *
     * @author Gabriele
     */
    public void updateLabels() {
        Platform.runLater(() -> {
            populatingSectionTitle.setText("YOUR SPACESHIP IS POPULATED");
            populatingSectionDesc.setText("Please wait while the other players do so...");
        });
    }
}