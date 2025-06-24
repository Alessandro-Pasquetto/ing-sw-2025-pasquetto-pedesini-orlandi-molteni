package org.progetto.client.gui;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;


public class Alerts {

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Creates and displays a visual warnings
     *
     * @author Lorenzo
     * @param message is the warning message
     */
    public static void showWarning(String message) {
        Stage stage = PageController.getStage();
        Scene scene = stage.getScene();

        Label label = new Label(message);
        label.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.9);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 15px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 18px;"
        );
        label.setFont(Font.font("Inter 18pt"));

        StackPane popupContent = new StackPane(label);
        popupContent.setPadding(new Insets(20));
        popupContent.setStyle("-fx-background-radius: 10;");
        popupContent.setOpacity(1.0);

        Popup popup = new Popup();
        popup.getContent().add(popupContent);
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        popup.show(stage, -1000, -1000);

        Platform.runLater(() -> {
            double contentWidth = popupContent.getWidth();
            double contentHeight = popupContent.getHeight();

            double x = stage.getX() + (stage.getWidth() - contentWidth) / 2;
            double y = stage.getY() + (stage.getHeight() - contentHeight) / 2;

            popup.setX(x);
            popup.setY(y);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(2), popupContent);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setOnFinished(ev -> popup.hide());
                ft.play();
            });
            pause.play();
        });
    }

    /**
     * Create a popup message to display an error
     *
     * @author Lorenzo,Alessandro
     * @param message is the String to display
     * @param onMouseEvent allows to display the message on the mouse pointer position
     */
    public static void showError(String message, Boolean onMouseEvent) {
        Stage stage = PageController.getStage();
        Scene scene = stage.getScene();
        Parent root = scene.getRoot();

        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
        -fx-background-color: rgba(0, 0, 0, 0.9);
        -fx-text-fill: white;
        -fx-padding: 15px;
        -fx-background-radius: 8;
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


