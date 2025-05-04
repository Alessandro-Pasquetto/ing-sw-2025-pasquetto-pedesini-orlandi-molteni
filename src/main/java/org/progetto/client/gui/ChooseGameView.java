package org.progetto.client.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.progetto.client.model.GameData;
import org.progetto.server.connection.games.WaitingGameInfo;
import org.progetto.server.model.Player;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ChooseGameView {

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField joinUsernameTextField;

    @FXML
    private ComboBox<Integer> boxGameLevel;

    @FXML
    private ComboBox<Integer> boxNumMaxPlayers;

    @FXML
    private TableView<WaitingGameInfo> gamesTable;

    @FXML
    private TableColumn<WaitingGameInfo, Integer> gameIdCol;

    @FXML
    private TableColumn<WaitingGameInfo, Integer> levelCol;

    @FXML
    private TableColumn<WaitingGameInfo, Integer> maxPlayersCol;

    @FXML
    private TableColumn<WaitingGameInfo, String> playersCol;

    @FXML
    private TableColumn<WaitingGameInfo, Void> joinCol;

    private int gameLevel = 0;
    private int numMaxPlayers = 0;

    private final ObservableList<WaitingGameInfo> gameData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boxGameLevel.setItems(FXCollections.observableArrayList(1, 2));
        boxGameLevel.setOnAction(event -> gameLevel = boxGameLevel.getValue());

        boxNumMaxPlayers.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
        boxNumMaxPlayers.setOnAction(event -> numMaxPlayers = boxNumMaxPlayers.getValue());

        gamesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        gameIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("level"));
        maxPlayersCol.setCellValueFactory(new PropertyValueFactory<>("maxPlayers"));
        playersCol.setCellValueFactory(param -> new SimpleStringProperty(
                param.getValue().getPlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "))
        ));

        gamesTable.setItems(gameData);
        gamesTable.setPlaceholder(new Label("No games available..."));
        addJoinButtonToTable();
    }

    private void addJoinButtonToTable() {
        joinCol.setCellFactory(col -> new TableCell<>() {
            private final Button joinButton = new Button("Join");

            {
                joinButton.setOnAction(event -> {
                    WaitingGameInfo game = getTableView().getItems().get(getIndex());
                    String username = joinUsernameTextField.getText().trim();

                    // Check if the username is empty
                    if (username.isEmpty()) {
                        Alerts.showWarning("Please enter your name before joining a game");
                        return;
                    }

                    GameData.setNamePlayer(username);
                    GameData.getSender().tryJoinToGame(game.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : joinButton);
            }
        });
    }

    public void createNewGame() {
        String username = usernameTextField.getText().trim();

        // Check if the username is empty
        if (username.isEmpty()) {
            Alerts.showWarning("Please enter your name before creating a game");
            return;
        }

        // Check if the game level is valid
        if (gameLevel == 0) {
            Alerts.showWarning("Please select a game level");
            return;
        }

        // Check if the number of players is valid
        if (numMaxPlayers == 0) {
            Alerts.showWarning("Please select the number of players");
            return;
        }

        GameData.setNamePlayer(username);
        GameData.getSender().createGame(gameLevel, numMaxPlayers);
    }

    public void generateGameRecordList(ArrayList<WaitingGameInfo> gamesInfo) {
        Platform.runLater(() -> {
            gameData.clear();
            gameData.addAll(gamesInfo);
        });
    }
}
