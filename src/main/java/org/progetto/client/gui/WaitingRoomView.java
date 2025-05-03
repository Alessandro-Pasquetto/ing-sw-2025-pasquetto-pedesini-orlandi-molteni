package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class WaitingRoomView {

    @FXML
    private Label gameIdLabel;

    @FXML
    private Label levelLabel;

    @FXML
    private ListView<String> playersListView;

    @FXML
    private Button readyButton;

    @FXML
    private Label waitingStatusLabel;

    /**
     * Metodo chiamato al caricamento della view.
     */
    public void initialize() {
        // Inizializza i dati di default
        gameIdLabel.setText("GameID: 0");
        levelLabel.setText("Level: 2");

        // Aggiungi giocatori alla lista
        playersListView.getItems().addAll("Mario 👑 - ready", "You", "Ale");

        // Imposta lo stato iniziale di attesa
        updateWaitingStatus(3, 4);
    }

    /**
     * Metodo chiamato quando il pulsante READY viene premuto.
     */
    @FXML
    private void onReadyPressed(MouseEvent event) {
        System.out.println("READY pressed!");
        // Aggiorna lo stato del giocatore come "ready"
        playersListView.getItems().set(1, "You - ready");

        // Controlla se tutti i giocatori sono pronti
        updateWaitingStatus(4, 4);
    }

    /**
     * Aggiorna lo stato di attesa.
     *
     * @param currentPlayers Numero di giocatori attuali.
     * @param maxPlayers     Numero massimo di giocatori.
     */
    private void updateWaitingStatus(int currentPlayers, int maxPlayers) {
        waitingStatusLabel.setText(String.format("%d/%d Waiting for players...", currentPlayers, maxPlayers));

        // Se tutti i giocatori sono pronti
        if (currentPlayers == maxPlayers) {
            waitingStatusLabel.setText("All players are ready!");
        }
    }
}