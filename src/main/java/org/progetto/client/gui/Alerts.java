package org.progetto.client.gui;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Alerts {

    // =======================
    // OTHER METHODS
    // =======================

    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Create a popup message to display
     *
     * @author Lorenzo
     * @param message is the String to display
     * @param onMouseEvent allows to display the message on the mouse pointer position
     */

    public static void showPopUp(String message, Boolean onMouseEvent) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.8);
            -fx-text-fill: white;
            -fx-padding: 15px;
            -fx-background-radius: 10;
            -fx-font-size: 16px;
        """);
        messageLabel.setFont(Font.font("Arial"));

        StackPane content = new StackPane(messageLabel);
        content.setAlignment(Pos.CENTER);

        Popup popup = new Popup();
        popup.getContent().add(content);
        popup.setAutoHide(true);

        double centerX;
        double centerY;
        Stage stage = PageController.getStage();

        if(onMouseEvent) {
            Robot robot = new Robot();

            centerX = robot.getMousePosition().getX();
            centerY = robot.getMousePosition().getY();

        }
        else {
            centerX = stage.getX() + stage.getWidth() / 2 - 100;
            centerY = stage.getY() + stage.getHeight() / 2 - 50;
        }

        popup.show(stage, centerX , centerY );

        FadeTransition ft = new FadeTransition(Duration.seconds(4), content);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> popup.hide());
        ft.play();
    }

}


