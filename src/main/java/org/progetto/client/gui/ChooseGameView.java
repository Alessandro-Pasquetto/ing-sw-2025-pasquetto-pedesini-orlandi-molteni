package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.progetto.client.model.GameData;

import java.util.ArrayList;

public class ChooseGameView {

    @FXML
    private TextField usernameTextField;
    @FXML
    private VBox chooseGameLayout;

    public void createNewGame() {
        // For now, there is no createGame page, so I'll do it here.
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {
            GameData.setNamePlayer(username);

            GameData.getSender().createGame(1, 1);// todo selection levelGame and numMaxPlayers in gui
        }
    }

    public void joinToGame(int idGame) {
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {
            GameData.setNamePlayer(username);

            GameData.getSender().tryJoinToGame(idGame);
        }
    }

    public void updateGameList() {
        GameData.getSender().updateGameList();
    }

    public void generateGameRecordList(ArrayList<Integer> idGames){

        Platform.runLater(() -> {
            chooseGameLayout.getChildren().clear();
        });

        for(Integer idGame : idGames){
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
}
