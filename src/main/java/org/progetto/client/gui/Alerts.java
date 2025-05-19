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


public class Alerts {

    // =======================
    // OTHER METHODS
    // =======================

    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }

    /**
     * Create a popup message to display an error
     *
     * @author Alessandro
     * @param message is the String to display
     * @param onMouseEvent allows to display the message on the mouse pointer position
     */
    public static void showError(String message, Boolean onMouseEvent) {
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

    /**
     * Show a yes/no popup with a title and message
     *
     * @author Gabriele
     * @param titleText the title text
     * @param messageText the message text
     * @param onYes the action to perform when "YES" is clicked
     * @param onNo the action to perform when "NO" is clicked
     */
    public static void showYesNoPopup(Pane targetPane, String titleText, String messageText, Runnable onYes, Runnable onNo) {
        Platform.runLater(() -> {
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
            overlay.setPickOnBounds(true);

            // Popup container
            VBox popup = new VBox(20);
            popup.setMaxWidth(400);
            popup.setMaxHeight(200);
            popup.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #a3aea7, #535e5d);" +
                    "-fx-padding: 20;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-color: rgba(43, 50, 57, 0.5);" +
                    "-fx-border-width: 2;"
            );
            popup.setAlignment(Pos.CENTER);

            // Title
            Label title = new Label(titleText);
            title.setStyle(
                    "-fx-font-size: 24px;" +
                    "-fx-text-fill: #15357b;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-family: 'Orgovan';"
            );

            // Message
            Label message = new Label(messageText);
            message.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: black;" +
                    "-fx-wrap-text: true;"
            );

            // Buttons
            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);

            Button yesButton = new Button("YES");
            Button noButton = new Button("NO");

            buttonBox.getChildren().addAll(yesButton, noButton);

            popup.getChildren().addAll(title, message, buttonBox);
            overlay.getChildren().add(popup);
            targetPane.getChildren().add(overlay);

            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // YES button action
            yesButton.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    targetPane.getChildren().remove(overlay);
                    if (onYes != null) onYes.run();
                });
                fadeOut.play();
            });

            // NO button action
            noButton.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    targetPane.getChildren().remove(overlay);
                    if (onNo != null) onNo.run();
                });
                fadeOut.play();
            });
        });
    }
}


