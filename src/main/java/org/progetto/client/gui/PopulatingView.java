package org.progetto.client.gui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;

import java.io.IOException;

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

    @FXML
    private VBox btnContainer;

    @FXML
    public Pane freezePane;

    @FXML
    public Label freezeTitle;

    @FXML
    public Label freezeTimer;

    @FXML
    public Label freezeDesc;

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
        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();

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

        populatingSectionDesc.setText("Select a component to fill it with the " + alienColor + " alien clicking on it...");

        PageController.getPopulatingView().updateSpaceship(ship);
        highlightCellsForAlien(ship, alienColor);

        clearBtnContainer();

        Button btn = new Button("Skip");
        btn.setOnAction(e -> GameData.getSender().responsePlaceAlien(-1, -1, alienColor));
        btnContainer.getChildren().add(btn);
    }

    /**
     * Clears the button container
     *
     * @author Alessandro
     */
    public void clearBtnContainer() {
        btnContainer.getChildren().clear();
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

        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();

        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component component = shipMatrix[row][col];
                Pane cell = getCellFromSpaceshipMatrix(col, row);

                if (cell != null && component instanceof HousingUnit housingUnit) {

                    // Highlight cells for the specific color
                    if (housingUnit.getAllowPurpleAlien() && alienColor.equals("purple")) {
                        highlightCellForAlien(cell, component, Color.rgb(160, 32, 240, 0.3));

                        cell.setOnMouseClicked(event -> {
                            GameData.getSender().responsePlaceAlien(component.getX(), component.getY(), alienColor);
                        });

                    } else if (housingUnit.getAllowOrangeAlien() && alienColor.equals("orange")) {
                        highlightCellForAlien(cell, component, Color.rgb(255, 165, 0, 0.3));

                        cell.setOnMouseClicked(event -> {
                            GameData.getSender().responsePlaceAlien(component.getX(), component.getY(), alienColor);
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
        populatingSectionTitle.setText("YOUR SPACESHIP IS POPULATED");
        populatingSectionDesc.setText("Please wait while the other players do so...");
    }


    /**
     * Shows the freeze pane
     *
     * @author Gabriele
     */
    public void showFreeze() {
        freezePane.setOpacity(0);
        freezePane.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), freezePane);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Hides the freeze pane
     *
     * @author Gabriele
     */
    public void hideFreeze() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), freezePane);
        ft.setToValue(0);
        ft.setOnFinished(e -> freezePane.setVisible(false));
        ft.play();
    }

    /**
     * Updates the freeze timer
     *
     * @author Gabriele
     * @param timeInSeconds is the time in seconds to update the timer
     */
    public void updateFreezeTimer(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;

        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        freezeTimer.setText(formattedTime);
    }

    /**
     * Displays a message when the player wins while being frozen
     *
     * @author Gabriele
     */
    public void winDuringFreeze() {
        freezeTitle.setText("You won!");
        freezePane.getChildren().remove(freezeTimer);
        freezeDesc.setText("You won the game while being frozen, congratulations!");

        freezePane.setMouseTransparent(false);

        // Adds a return to lobby button
        Button returnButton = new Button("Return to lobby");

        returnButton.setOnAction(e -> {
            try {
                returnToLobby();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        freezePane.getChildren().add(returnButton);
    }

    /**
     * Allows the player to return to the lobby page
     *
     * @author Alessandro
     * @throws IOException if the page cannot be loaded
     */
    public void returnToLobby() throws IOException {
        GameData.resetData();

        PageController.loadControllers();

        PageController.switchScene("chooseGame.fxml");
        GameData.getSender().updateGameList();
    }
}