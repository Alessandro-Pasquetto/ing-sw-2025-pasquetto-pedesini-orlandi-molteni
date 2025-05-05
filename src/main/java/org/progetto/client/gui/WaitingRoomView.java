package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class WaitingRoomView {

    @FXML
    public TableColumn readyCol;

    @FXML
    public TableColumn playerCol;

    @FXML
    private TableView playersTable;

    @FXML
    private Button readyButton;

    @FXML
    private Label waitingStatusLabel;

    /**
     * Metodo chiamato al caricamento della view.
     */
    public void initialize() {

        playersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
        waitingStatusLabel.setText(String.format("Waiting for players...", currentPlayers, maxPlayers));

        // Se tutti i giocatori sono pronti
        if (currentPlayers == maxPlayers) {
            waitingStatusLabel.setText("All players are ready!");
        }
    }
}