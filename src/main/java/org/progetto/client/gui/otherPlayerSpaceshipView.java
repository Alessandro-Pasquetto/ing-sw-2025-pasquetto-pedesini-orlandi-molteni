package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.progetto.client.MainClient;
import org.progetto.server.model.components.Component;

public class otherPlayerSpaceshipView {

    @FXML
    private GridPane shipGrid;
    @FXML
    private HBox bookedComponentsBox;
    @FXML
    private HBox handBox;


    public void drawShip(Component[][] shipMatrix) {
        shipGrid.getChildren().clear();
        for (int row = 0; row < shipMatrix.length; row++) {
            for (int col = 0; col < shipMatrix[row].length; col++) {
                Component comp = shipMatrix[row][col];
                Pane cell = new Pane();
                cell.setPrefSize(50, 50);

                if (comp != null) {
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/" + comp.getImgSrc())));
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(50);
                    iv.setFitHeight(50);
                    iv.setPreserveRatio(true);
                    cell.getChildren().add(iv);
                }

                shipGrid.add(cell, col, row);
            }
        }
    }

}


