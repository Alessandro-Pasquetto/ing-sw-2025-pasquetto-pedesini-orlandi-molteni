package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.progetto.client.model.GameData;

import java.util.ArrayList;
import java.util.List;

public class ChooseGameView {

    @FXML
    private TextField usernameTextField;

    @FXML
    private ListView<String> gamesVisual;;

    @FXML
    public ComboBox boxGameLevel;

    @FXML
    public ComboBox boxNumMaxPlayers;

    int gameLevel = 1;
    int numMaxPlayers = 1;

    @FXML
    public void initialize() {
        boxGameLevel.setValue(1);
        boxGameLevel.setItems(FXCollections.observableArrayList(1, 2));

        boxGameLevel.setOnAction(event -> {
            gameLevel = (int) boxGameLevel.getValue();
        });


        boxNumMaxPlayers.setValue(1);
        boxNumMaxPlayers.setItems(FXCollections.observableArrayList(1, 2, 3, 4));

        boxNumMaxPlayers.setOnAction(event -> {
            numMaxPlayers = (int) boxNumMaxPlayers.getValue();
        });
    }

    public void createNewGame() {
        // For now, there is no createGame page, so I'll do it here.
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {
            GameData.setNamePlayer(username);

            GameData.getSender().createGame(gameLevel, numMaxPlayers);
        }
    }

    public void joinToGame(int idGame) {
        String username = usernameTextField.getText();
        if(!username.isEmpty()) {
            GameData.setNamePlayer(username);

            GameData.getSender().tryJoinToGame(idGame);
        }
    }

    public void generateGameRecordList(ArrayList<Integer> idGames){

        Platform.runLater(() -> {
            gamesVisual.getItems().clear();
        });

        for(Integer idGame : idGames){
            gamesVisual.getItems().add(idGame.toString());
            gamesVisual.refresh();
        }



//        for(Integer idGame : idGames){
//            Label messageLabel = new Label("Game " + idGame);
//
//            Button button = new Button("Entra");
//
//            button.setOnAction(e -> {
//                joinToGame(idGame);
//            });
//
//            Platform.runLater(() -> {
//                gamesVisual.getChildren().addAll(messageLabel, button);
//            });
//        }
    }
}
