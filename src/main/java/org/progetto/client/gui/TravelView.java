package org.progetto.client.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXML;

public class TravelView {

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label statusLabel;

    public void initialize() {
        int rows = 5;
        int cols = 12;

        // Genera la griglia di celle del percorso
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                StackPane cell = createBoardCell(row, col);
                boardGrid.add(cell, col, row);
            }
        }
    }

    private StackPane createBoardCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(60, 60);
        cell.setStyle("-fx-border-color: gray; -fx-background-color: #1e1e1e;");
        Label label = new Label(row + "," + col);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 10;");
        cell.getChildren().add(label);
        return cell;
    }

    @FXML
    private void handleAdvance() {
        statusLabel.setText("Avanzato di un passo...");
    }

    @FXML
    private void handleEndTurn() {
        statusLabel.setText("Turno terminato.");
    }



}
