package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;

public class AdjustingPageView {

    @FXML
    public ImageView spaceShipImage;

    @FXML
    private GridPane spaceshipMatrix;

    public void discardedComponent() {

    }

    public void incorrectlyPlacedComponent(Spaceship spaceship) {
        Component[][] shipMatrix = spaceship.getBuildingBoard().getCopySpaceshipMatrix();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                if(comp.getIncorrectlyPlaced()){
                    for (Node node : spaceshipMatrix.getChildren()) {
                        if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null && GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                            node.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            break;
                        }
                    }
                }
            }
        }
    }
}