package org.progetto.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.progetto.client.connection.HandlerMessage;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.server.connection.rmi.RmiServer;

public class ChooseGameView {

    @FXML
    private TextField usernameTextField;
    @FXML
    private VBox chooseGameLayout;

    public void createNewGame() {
        // For now, there is no createGame page, so I'll do it here.
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {

            if(HandlerMessage.getIsSocket())
                SocketClient.createNewGame(username);
            else
                RmiClientSender.createGame(username);
        }
    }

    public void joinToGame(int idGame) {
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {

            if(HandlerMessage.getIsSocket())
                SocketClient.tryJoinToGame(username, idGame);
            else
                RmiClientSender.tryJoinToGame(username, idGame);
        }
        GameData.setNamePlayer(username);
    }


    public void generateGameList(int idGame){

        Label messageLabel = new Label("Game " + idGame);

        Button button = new Button("Entra");

        button.setOnAction(e -> {
            joinToGame(idGame);
        });

        Platform.runLater(() -> {
            chooseGameLayout.getChildren().addAll(messageLabel, button);
        });
    }
}
