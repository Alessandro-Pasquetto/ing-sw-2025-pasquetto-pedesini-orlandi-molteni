package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.progetto.client.model.GameData;
import org.progetto.messages.toClient.ShowWaitingGamesMessage;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseGameView {

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField joinUsernameTextField;

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

        gamesVisual.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // doppio click
                String selectedItem = gamesVisual.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String[] game = selectedItem.split(" ");
                    joinToGame(Integer.parseInt(game[2]));
                }
            }
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
        String username = joinUsernameTextField.getText();
        if(!username.isEmpty()) {
            GameData.setNamePlayer(username);

            GameData.getSender().tryJoinToGame(idGame);
        }
    }

    public void generateGameRecordList(ShowWaitingGamesMessage games){

        Platform.runLater(() -> {
            gamesVisual.getItems().clear();
        });

        for(WaitingGameInfo info : games.getWaitingGames()){
            ArrayList<String> players = new ArrayList<>();
            for(Player player : info.getPlayers()){
                players.add(player.getName());
            }

            String line = "  "+info.getId() +"                        "+
                          info.getLevel()+"                         "+
                          info.getMaxPlayers()+"                         "+
                          " "+players;

            Platform.runLater(() -> {
                gamesVisual.getItems().add(line);
            });
        }
    }
}
