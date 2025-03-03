package org.progetto.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ChooseGameController {

    @FXML
    private TextField usernameTextField;

    @FXML
    public void createNewGame() {
        // Per ora non c'è la pagina createGame quindi faccio qui
        String username = usernameTextField.getText();
        SocketClient.createNewGame(username);
    }

    public void joinToGame() {
        String username = usernameTextField.getText();

        SocketClient.JoinToGame(username);
    }
}
