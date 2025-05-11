package org.progetto.client.gui;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.progetto.server.model.Player;


public class EventView {

    @FXML private ImageView eventCardImage;
    @FXML private ListView<String> otherShipsList;
    @FXML private TableView<Player> scoreboardTable;
    @FXML private TextArea terminalOutput;
    @FXML private TextField terminalInput;

    @FXML
    private void handleCommandInput() {
        String input = terminalInput.getText();
        terminalOutput.appendText("> " + input + "\n");
        terminalInput.clear();

        // Logica comando...
    }
}