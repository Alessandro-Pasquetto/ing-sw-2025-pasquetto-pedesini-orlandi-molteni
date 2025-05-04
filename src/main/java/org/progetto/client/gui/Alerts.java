package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts {

    // =======================
    // OTHER METHODS
    // =======================

    public static void showWarning(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
            alert.showAndWait();
        });
    }
}
