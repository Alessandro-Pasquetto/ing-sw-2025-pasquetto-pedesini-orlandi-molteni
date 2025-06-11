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

    // =======================
    // ATTRIBUTES
    // =======================

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

    // =======================
    // METHODS
    // =======================

    /**
     * Setup method for this view.
     *
     * @author Gabriele
     */
    @FXML
    public void initialize() {
        boxGameLevel.setItems(FXCollections.observableArrayList(1, 2));
        boxGameLevel.setOnAction(event -> gameLevel = boxGameLevel.getValue());

        boxNumMaxPlayers.setItems(FXCollections.observableArrayList( 2, 3, 4));
        boxNumMaxPlayers.setOnAction(event -> numMaxPlayers = boxNumMaxPlayers.getValue());

        gamesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        gamesTable.getColumns().forEach(column -> column.setReorderable(false));
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

    /**
     * This method adds a button to each row of the games table that allows the user to join a game.
     *
     * @author Gabriele
     */
    private void addJoinButtonToTable() {
        joinCol.setCellFactory(col -> new TableCell<>() {
            private final Button joinButton = new Button("Join");

            {
                joinButton.setOnAction(event -> {
                    WaitingGameInfoMessage game = getTableView().getItems().get(getIndex());
                    String username = joinUsernameTextField.getText().trim();

                    // Check if the username is empty
                    if (username.isEmpty()) {
                        Alerts.showError("Please enter your name before joining a game", true);
                        return;
                    }

                    if(username.length() > 16) {
                        Alerts.showError("Your name needs to be at most 16 characters", true);
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

    /**
     * This method is called when the user clicks the create game button.
     * It retrieves the username, game level, and number of players from the text fields and combo boxes, validates them, and attempts to create a new game.
     *
     * @author Gabriele
     */
    public void createNewGame() {
        String username = usernameTextField.getText().trim();

        // Check if the username is empty
        if (username.isEmpty()) {
            Alerts.showError("Enter your name before joining a game", true);
            return;
        }

        // Check if the game level is valid
        if (gameLevel == 0) {
            Alerts.showError("Select a game level", true);
            return;
        }

        // Check if the number of players is valid
        if (numMaxPlayers == 0) {
            Alerts.showError("Select the number of players", true);
            return;
        }

        if(username.length() > 16) {
            Alerts.showError("Your name needs to be at most 16 characters", true);
            return;
        }

        GameData.setNamePlayer(username);
        GameData.getSender().createGame(gameLevel, numMaxPlayers);
    }

    /**
     * This method is called when the user clicks the refresh button.
     * It retrieves the list of available games from the server and updates the games table.
     *
     * @author Gabriele
     */
    public void generateGameRecordList(ArrayList<WaitingGameInfoMessage> gamesInfo) {
        gameData.clear();
        gameData.addAll(gamesInfo);
    }
}
