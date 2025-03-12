package org.progetto.client;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class ConnetionView {

    @FXML
    private TextField serverIpTextField;
    @FXML
    private TextField serverPortTextField;
    @FXML
    private RadioButton rmiOption;
    @FXML
    private RadioButton socketOption;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        rmiOption.setToggleGroup(toggleGroup);
        socketOption.setToggleGroup(toggleGroup);

        serverIpTextField.setText("127.0.0.1");
        serverPortTextField.setText("8080");
        socketOption.setSelected(true);
    }

    @FXML
    private void connectToServer() {
        String serverIp = serverIpTextField.getText();
        String serverPortString = serverPortTextField.getText();

        if (serverIp.isEmpty() || serverPortString.isEmpty()) {
            System.out.println("Errore: Tutti i campi devono essere compilati.");
            return;
        }

        try {
            int serverPort = Integer.parseInt(serverPortString);

            if (socketOption.isSelected()) {
                SocketClient.connect(serverIp, serverPort);
            } else if (rmiOption.isSelected()) {
                // RMI connection
            }

        }catch (NumberFormatException e) {
            System.out.println("Errore: Porta non valida.");
        }
    }
}
