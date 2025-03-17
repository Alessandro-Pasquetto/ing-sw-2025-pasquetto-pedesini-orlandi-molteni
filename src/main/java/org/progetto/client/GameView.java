package org.progetto.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class GameView {

    @FXML
    private VBox components;


    public void pickComponent(ActionEvent actionEvent) {
        SocketClient.pickComponent();
    }

    public void startGame(ActionEvent actionEvent) {
        SocketClient.startGame();
    }

    public void generateComponent(String imgComponent) {

        Image image = new Image(String.valueOf(Main.class.getResource("img/components/" + imgComponent)));

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        Button button = new Button("Ruota");

        button.setOnAction(e -> {
            imageView.setRotate(imageView.getRotate() + 90);
        });

        Platform.runLater(() -> {
            components.getChildren().addAll(imageView, button);
        });

    }
}
