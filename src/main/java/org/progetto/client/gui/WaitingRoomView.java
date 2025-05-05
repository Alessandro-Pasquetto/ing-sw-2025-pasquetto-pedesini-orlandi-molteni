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
    private Label waitingStatusLabel;

    int numMaxPlayers = 0;

    private final ObservableList<String[]> playersList = FXCollections.observableArrayList();

    /**
     * Metodo chiamato al caricamento della view.
     */
    public void initialize() {

        playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        playerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        readyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        playersTable.setItems(playersList);
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
    }

    public void updatePlayersList(ArrayList<Player> players) {

        waitingStatusLabel.setText(String.format("%d/%d Waiting for players...", players.size(), numMaxPlayers));
        playersList.clear();

        for (Player player : players) {
            String status = player.getIsReady() ? "Ready" : "Not Ready";

            playersList.add(new String[]{player.getName(), status});
        }

        playersTable.setItems(playersList);
    }
}