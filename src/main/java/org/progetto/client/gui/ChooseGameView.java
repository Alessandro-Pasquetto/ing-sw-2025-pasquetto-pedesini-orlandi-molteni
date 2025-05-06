package org.progetto.client.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.progetto.client.model.GameData;
import org.progetto.messages.toClient.WaitingGameInfoMessage;
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
    private TableView<WaitingGameInfoMessage> gamesTable;

    @FXML
    private TableColumn<WaitingGameInfoMessage, Integer> gameIdCol;

    @FXML
    private TableColumn<WaitingGameInfoMessage, Integer> levelCol;

    @FXML
    private TableColumn<WaitingGameInfoMessage, Integer> maxPlayersCol;

    @FXML
    private TableColumn<WaitingGameInfoMessage, String> playersCol;

    @FXML
    private TableColumn<WaitingGameInfoMessage, Void> joinCol;

    private int gameLevel = 0;
    private int numMaxPlayers = 0;

    private final ObservableList<WaitingGameInfoMessage> gameData = FXCollections.observableArrayList();

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
                    WaitingGameInfoMessage game = getTableView().getItems().get(getIndex());
                    String username = joinUsernameTextField.getText().trim();

                    // Check if the username is empty
                    if (username.isEmpty()) {
                        Alerts.showPopUp("Please enter your name before joining a game", true);
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
            Alerts.showPopUp("Enter your name before joining a game", true);
            return;
        }

        // Check if the game level is valid
        if (gameLevel == 0) {
            Alerts.showPopUp("Select a game level", true);
            return;
        }

        // Check if the number of players is valid
        if (numMaxPlayers == 0) {
            Alerts.showPopUp("Select the number of players", true);
            return;
        }

        GameData.setNamePlayer(username);
        GameData.getSender().createGame(gameLevel, numMaxPlayers);
    }

    public void generateGameRecordList(ArrayList<WaitingGameInfoMessage> gamesInfo) {
        gameData.clear();
        gameData.addAll(gamesInfo);
    }
}
