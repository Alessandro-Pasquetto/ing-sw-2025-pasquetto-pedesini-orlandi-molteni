package org.progetto.client.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Pair;
import org.progetto.client.model.GameData;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.model.Player;

import java.util.ArrayList;

public class WaitingRoomView {

    @FXML
    private TableView<String[]> playersTable;

    @FXML
    public TableColumn<String[], String> readyCol;

    @FXML
    public TableColumn<String[], String> playerCol;

    @FXML
    public Label gameIdLabel;

    @FXML
    public Label gameLevelLabel;

    @FXML
    public Label gameMaxPlayersLabel;

    @FXML
    private Button readyButton;

    @FXML
    public Label valueStatusLabel;

    @FXML
    public Label phraseStatusLabel;

    int numMaxPlayers = 0;

    private final ObservableList<String[]> playersList = FXCollections.observableArrayList();

    public void initialize() {
        playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        playerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        readyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        playersTable.setItems(playersList);

        readyButton.setDisable(true);
    }

    public void init(int gameId, int gameLevel, int numMaxPlayersParam) {

        gameIdLabel.setText(String.valueOf(gameId));
        gameLevelLabel.setText(String.valueOf(gameLevel));
        gameMaxPlayersLabel.setText(String.valueOf(numMaxPlayersParam));
        numMaxPlayers = numMaxPlayersParam;
    }

    @FXML
    private void onReadyPressed() {
        GameData.getSender().readyPlayer();
        readyButton.setDisable(true);

        //TODO: maybe add a feature to remove ready state
    }

    public void updatePlayersList(ArrayList<Player> players) {

        playersList.clear();

        int numReadyPlayers = 0;

        for (Player player : players) {

            String name = player.getName();
            if (name.equals(GameData.getNamePlayer())) {
                name = name + " (You)";
            }

            String status;
            if (player.getIsReady()){
                status = "Ready";
                numReadyPlayers++;
            } else
                status = "Not Ready";

            playersList.add(new String[]{name, status});
        }

        playersTable.setItems(playersList);

        if (players.size() == numMaxPlayers) {
            valueStatusLabel.setText(String.format("%d/%d players are ready.", numReadyPlayers, numMaxPlayers));
            phraseStatusLabel.setText(String.format("Waiting for others to get ready..."));
            readyButton.setDisable(false);
        } else {
            valueStatusLabel.setText(String.format("%d/%d players joined.", players.size(), numMaxPlayers));
            phraseStatusLabel.setText(String.format("Waiting for more players to join..."));
        }
    }
}