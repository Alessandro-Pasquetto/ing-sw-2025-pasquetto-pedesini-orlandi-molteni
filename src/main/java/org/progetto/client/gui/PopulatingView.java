package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

import java.util.Optional;

public class PopulatingView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;
    private boolean orangeAlienAdded = false;
    private boolean purpleAlienAdded = false;
    private boolean alienPlacementStarted = false;

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
                }

                shipGrid.add(cell, col, row);
            }
        }

        // Verifica se si deve avviare il processo di posizionamento degli alieni
        if (!alienPlacementStarted) {
            alienPlacementStarted = true; // Evita chiamate ripetute
            startAlienPlacementProcess(ship);
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

    public void startAlienPlacementProcess(Spaceship ship) {
        // Start the process only once, not on every update
        Platform.runLater(() -> proceedToPurpleAlienPlacement(ship));
    }

    private void proceedToPurpleAlienPlacement(Spaceship ship) {
        if (hasValidCellsForAlien(ship, "purple")) {
            askForAlienPlacement("purple", Color.rgb(160, 32, 240, 0.25), () -> {
                // Highlight purple cells
                highlightCellsForAlien(ship, "purple", () -> {
                    updateValidCellsForOrange(ship);

                    proceedToOrangeAlienPlacement(ship);
                });
            }, () -> {
                // If choose NO, skip to orange
                proceedToOrangeAlienPlacement(ship);
            });
        } else {
            proceedToOrangeAlienPlacement(ship);
        }
    }

    private void proceedToOrangeAlienPlacement(Spaceship ship) {
        if (hasValidCellsForAlien(ship, "orange")) {
            askForAlienPlacement("orange", Color.rgb(255, 165, 0, 0.25), () -> {
                // Highlights orange cells
                highlightCellsForAlien(ship, "orange", this::goToNextPhase);
            }, this::goToNextPhase);
        } else {
            goToNextPhase();
        }
    }

    private boolean hasValidCellsForAlien(Spaceship ship, String alienColor) {
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();

        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                if (comp instanceof HousingUnit) {
                    HousingUnit housingUnit = (HousingUnit) comp;

                    // Finds possible valid cells for aliens
                    if ("purple".equals(alienColor) && housingUnit.getAllowPurpleAlien() && !purpleAlienAdded) {
                        return true;
                    } else if ("orange".equals(alienColor) && housingUnit.getAllowOrangeAlien() && !orangeAlienAdded) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void updateValidCellsForOrange(Spaceship ship) {
        // Update for orange
        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                if (comp instanceof HousingUnit) {
                    HousingUnit housingUnit = (HousingUnit) comp;
                    if (housingUnit.getAllowPurpleAlien() && housingUnit.getAllowOrangeAlien() && !(housingUnit.getHasPurpleAlien())) {
                        // Purple and orange cell
                        housingUnit.setAllowPurpleAlien(false); // Disable purple
                    }
                }
            }
        }
    }

    /**
     * Asks the player if they want to place an alien of a specific color.
     *
     * @author Stefano
     * @param alienColor is the color of the alien ("purple" or "orange").
     * @param highlightColor is the highlight color for the alien.
     * @param yesAction is the action to perform if the player selects "YES".
     * @param noAction is the action to perform if the player selects "NO".
     */
    private void askForAlienPlacement(String alienColor, Color highlightColor, Runnable yesAction, Runnable noAction) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alien Placement");
            alert.setHeaderText("Do you want to place a " + alienColor + " alien?");
            alert.setContentText("Click YES to highlight the cells where you can place the alien, or NO to skip.");

            ButtonType yesButton = new ButtonType("YES");
            ButtonType noButton = new ButtonType("NO");

            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                yesAction.run(); // Esegui l'azione per "YES"
            } else {
                noAction.run(); // Esegui l'azione per "NO"
            }
        });
    }


    /**
     * Highlights the cells where an alien can be placed.
     *
     * @author Stefano
     * @param ship is the spaceship to update.
     * @param alienColor is the color of the alien ("purple" or "orange").
     */
    private void highlightCellsForAlien(Spaceship ship, String alienColor, Runnable onComplete) {
        clearHighlightedCells();

        Component[][] shipMatrix = ship.getBuildingBoard().getCopySpaceshipMatrix();
        boolean hasHighlighted = false;

        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = getCellFromSpaceshipMatrix(col, row);

                if (comp != null && cell != null && comp instanceof HousingUnit) {
                    HousingUnit housingUnit = (HousingUnit) comp;

                    // Highlight cells for the specific color
                    if ("purple".equals(alienColor) && housingUnit.getAllowPurpleAlien() && !purpleAlienAdded) {
                        highlightCellForAlien(cell, comp, Color.rgb(160, 32, 240, 0.25), c -> {
                            purpleAlienFill(c);
                            onComplete.run();
                        });
                    } else if ("orange".equals(alienColor) && housingUnit.getAllowOrangeAlien() && !orangeAlienAdded) {
                        highlightCellForAlien(cell, comp, Color.rgb(255, 165, 0, 0.25), c -> {
                            orangeAlienFill(c);
                            onComplete.run();
                        });
                    }
                }
            }
        }
    }

    private void highlightCellForAlien(Pane cell, Component comp, Color color, java.util.function.Consumer<Component> fillAction) {
        Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
        overlay.setFill(color);
        overlay.setOpacity(0.90);
        cell.getChildren().add(overlay);

        overlay.toFront();

        cell.setStyle("-fx-cursor: hand;");

        // Action for alien placing
        cell.setOnMouseClicked(event -> {
            fillAction.accept(comp);
        });
    }

    private void clearHighlightedCells() {
        // Rimuovi solo gli overlay senza modificare lo stato delle celle
        spaceshipMatrix.getChildren().forEach(node -> {
            if (node instanceof Pane) {
                Pane cell = (Pane) node;
                cell.getChildren().removeIf(child -> child instanceof Rectangle); // Rimuovi tutti gli overlay
                cell.setStyle(""); // Resetta lo stile del cursore
            }
        });
    }

    /**
     * Moves to the next phase of the game.
     */
    private void goToNextPhase() {
        Platform.runLater(() -> {
            populatingSectionTitle.setText("YOUR ALIENS ARE READY!");
            populatingSectionDesc.setText("You have to wait for the other players to populate their spaceship...");
        });
    }
}