package org.progetto.client.gui;
import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
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
    /*
    public static void showPopup(String message, Boolean onMouseEvent) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.8);
            -fx-text-fill: white;
            -fx-padding: 15px;
            -fx-background-radius: 10;
            -fx-font-size: 14px;
            """);
        messageLabel.setFont(Font.font("Arial"));

        StackPane overlay = new StackPane(messageLabel);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMouseTransparent(true);


        Stage stage = PageController.getStage();
        Scene scene = stage.getScene();

        Popup popup = new Popup();
        popup.getContent().add(overlay);
        popup.setAutoHide(true);


        // First show the popup off-screen to compute its actual dimensions
        popup.show(stage, -10000, -10000);

        // Now we can get the actual width and height of the popup
        double popupWidth = overlay.getWidth();
        double popupHeight = overlay.getHeight();

        // Calculate the proper position to center the popup
        double centerX;
        double centerY;

        if(onMouseEvent) {
            Robot robot = new Robot();
            centerX = robot.getMousePosition().getX() - (popupWidth / 2);
            centerY = robot.getMousePosition().getY() - (popupHeight / 2);
        } else {
            centerX = stage.getX() + (stage.getWidth() / 2) - (popupWidth / 2);
            centerY = stage.getY() + (stage.getHeight() / 2) - (popupHeight / 2);
        }

        // Hide and reshow at the correct position
        popup.hide();
        popup.show(stage, centerX, centerY);

        FadeTransition ft = new FadeTransition(Duration.seconds(3), overlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> popup.hide());
        ft.play();
    }
    */

    public static void showPopup(String message, Boolean onMouseEvent) {
        Stage stage = PageController.getStage();
        Scene scene = stage.getScene();
        Parent root = scene.getRoot();

        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
        -fx-background-color: rgba(0, 0, 0, 0.8);
        -fx-text-fill: white;
        -fx-padding: 15px;
        -fx-background-radius: 10;
        -fx-font-size: 14px;
        """);
        messageLabel.setFont(Font.font("Arial"));

        Pane overlay = new Pane(messageLabel);
        overlay.setPickOnBounds(true);
        overlay.setMouseTransparent(true);

        overlay.setPrefWidth(scene.getWidth());
        overlay.setPrefHeight(scene.getHeight());

        if (root instanceof Pane paneRoot) {
            paneRoot.getChildren().add(overlay);
        } else {
            System.err.println("Root is not a Pane.");
            return;
        }

        Robot robot = new Robot();
        Point2D mousePosScreen = new Point2D(robot.getMousePosition().getX(), robot.getMousePosition().getY());
        Point2D mousePosInScene = scene.getWindow().getScene().getRoot().screenToLocal(mousePosScreen);

        double centerX;
        double centerY;

        if(onMouseEvent) {
            centerX = mousePosInScene.getX();
            centerY = mousePosInScene.getY();
        } else {
            centerX = stage.getWidth() / 2;
            centerY = stage.getHeight() / 2;
        }

        messageLabel.setLayoutX(centerX);
        messageLabel.setLayoutY(centerY);

        FadeTransition ft = new FadeTransition(Duration.seconds(3), overlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> paneRoot.getChildren().remove(overlay));
        ft.play();

        scene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setPrefWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setPrefHeight(newVal.doubleValue()));
    }
}


