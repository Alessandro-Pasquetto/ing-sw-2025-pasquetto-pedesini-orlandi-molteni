package org.progetto.client.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;

import java.util.ArrayList;

public class WaitingRoomView {

    // =======================
    // ATTRIBUTES
    // =======================

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

    // =======================
    // METHODS
    // =======================

    /**
     * Setup method for this view
     *
     * @author Alessandro, Gabriele
     */
    public void initialize() {
        playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        playerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        readyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        playersTable.setItems(playersList);

        readyButton.setDisable(true);
    }

    /**
     * Populates the game information in the waiting room.
     *
     * @author Alessandro, Gabriele
     * @param gameId game ID
     * @param gameLevel game level
     * @param numMaxPlayersParam maximum number of players
     */
    public void populateGameInformation(int gameId, int gameLevel, int numMaxPlayersParam) {
        gameIdLabel.setText(String.valueOf(gameId));
        gameLevelLabel.setText(String.valueOf(gameLevel));
        gameMaxPlayersLabel.setText(String.valueOf(numMaxPlayersParam));
        numMaxPlayers = numMaxPlayersParam;

        readyButton.setDefaultButton(true);
    }

    /**
     * Called when the ready button is pressed.
     * Sends a message to the server to notify that the player is ready.
     * Disables the button after pressing it.
     *
     * @author Alessandro
     */
    @FXML
    private void onReadyPressed() {
        GameData.getSender().readyPlayer();
        readyButton.setDisable(true);
    }

    /**
     * Called when the player is not ready anymore.
     * Enables the ready button.
     *
     * @author Alessandro
     */
    public void disableReadyBtn(boolean disable) {
        readyButton.setDisable(disable);
    }

    /**
     * Updates the players list in the waiting room.
     *
     * @author Alessandro, Gabriele
     * @param players list of players
     */
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
            valueStatusLabel.setText(String.format("%d/%d players are ready", numReadyPlayers, numMaxPlayers));
            phraseStatusLabel.setText(String.format("Waiting for others to get ready..."));
        } else {
            valueStatusLabel.setText(String.format("%d/%d players joined", players.size(), numMaxPlayers));
            phraseStatusLabel.setText(String.format("Waiting for more players to join..."));
        }
    }
}