package org.progetto.client.gui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
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

import java.io.IOException;

public class AdjustingView {

    // =======================
    // ATTRIBUTES
    // =======================

    final int COMPONENT_SIZE = 80;

    @FXML
    public StackPane adjustingPane;

    @FXML
    public ImageView spaceShipImage;

    @FXML
    public Label adjustingSectionTitle;

    @FXML
    public Label adjustingSectionDesc;

    @FXML
    private GridPane spaceshipMatrix;

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
        adjustingPane.setBackground(background);
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
     * @author Gabriele
     * @param ship is the spaceship to be updated
     */
    public void updateSpaceship(Spaceship ship) {
        Component[][] shipMatrix = ship.getBuildingBoard().getSpaceshipMatrixCopy();

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

                    // Checks if the component is incorrectly placed
                    if (comp.getIncorrectlyPlaced()) {
                        Rectangle overlay = new Rectangle(COMPONENT_SIZE, COMPONENT_SIZE);
                        overlay.setFill(Color.rgb(255, 0, 0, 0.25));
                        cell.getChildren().add(overlay);

                        cell.getChildren().get(1).toFront();

                        cell.setStyle("-fx-cursor: hand;");

                        cell.setOnMouseClicked(event -> {
                            deleteComponent(comp);
                        });

                        incorrectlyPlacedCount++;
                    } else {
                        cell.setStyle("-fx-cursor: default;");
                    }
                }

                shipGrid.add(cell, col, row);
            }
        }

        // Checks if incorrectly placed components are present
        if (incorrectlyPlacedCount == 0) {
            adjustingSectionTitle.setText("YOUR SPACESHIP IS READY!");
            adjustingSectionDesc.setText("You have to wait for the other players to finish adjusting their spaceships...");
        }
    }

    /**
     * Deletes the component from the spaceship matrix
     *
     * @author Gabriele
     * @param comp is the component to be deleted
     */
    public void deleteComponent(Component comp) {
        GameData.getSender().destroyComponent(comp.getX(), comp.getY());
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