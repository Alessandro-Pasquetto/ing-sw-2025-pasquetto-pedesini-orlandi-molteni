package org.progetto.client;

import javafx.event.ActionEvent;

public class GameView {
    public void pickComponent(ActionEvent actionEvent) {
        SocketClient.pickComponent();
    }

    public void startGame(ActionEvent actionEvent) {
        SocketClient.startGame();
    }
}
