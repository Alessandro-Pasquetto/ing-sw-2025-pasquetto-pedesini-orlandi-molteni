package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.model.GameData;
import java.io.IOException;
import java.rmi.NotBoundException;

public class ConnectionView {

    @FXML
    private TextField serverIpTextField;

    @FXML
    private TextField serverPortTextField;

    @FXML
    private RadioButton rmiOption;

    @FXML
    private RadioButton socketOption;

    @FXML
    public Button connectBtn;

    private ToggleGroup toggleGroup;

    @FXML
    public void initialize() {
        toggleGroup = new ToggleGroup();
        rmiOption.setToggleGroup(toggleGroup);
        socketOption.setToggleGroup(toggleGroup);

        serverIpTextField.setText("localhost");
        serverPortTextField.setText("8080");
        socketOption.setSelected(true);

        connectBtn.setDefaultButton(true);

        socketOption.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectBtn.fire();
            }
        });

        rmiOption.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectBtn.fire();
            }
        });
    }

    @FXML
    private void connectToServer() {
        String serverIp = serverIpTextField.getText();
        String serverPortString = serverPortTextField.getText();

        if (serverIp.isEmpty() || serverPortString.isEmpty()) {
            Alerts.showPopUp("Errore: Tutti i campi devono essere compilati.",true);
            System.out.println("Errore: Tutti i campi devono essere compilati.");
            return;
        }

        int serverPort;
        try{
            serverPort = Integer.parseInt(serverPortString);

        }catch (NumberFormatException e){
            System.out.println("Server port must be a number!");
            return;
        }

        if (serverPort < 0) {
            System.out.println("Port cannot be negative!");
            return;
        }

        try {
            if (socketOption.isSelected())
                GameData.setSender(new SocketClient());
            else if (rmiOption.isSelected())
                GameData.setSender(new RmiClientSender());

            GameData.getSender().connect(serverIp, serverPort);

            PageController.switchScene("chooseGame.fxml", "ChooseGame");
            GameData.getSender().updateGameList();

        } catch (IOException | NotBoundException e) {
            System.out.println("Error connecting to " + serverIp + ":" + serverPort);
        }
    }
}
