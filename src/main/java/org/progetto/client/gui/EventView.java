package org.progetto.client.gui;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.progetto.client.MainClient;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;


public class EventView {

    @FXML private ImageView eventCardImage;
    @FXML private ListView<String> otherShipsList;
    @FXML private TableView<Player> scoreboardTable;
    @FXML private TextArea terminalOutput;
    @FXML private TextField terminalInput;
    @FXML private GridPane shipGrid;
    @FXML private StackPane shipContainerPane;

    @FXML
    private void handleCommandInput() {
        String input = terminalInput.getText();
        terminalOutput.appendText("> " + input + "\n");
        terminalInput.clear();

        // Logica comando...
    }

    public void initEvent() {

        GameData.getSender().showSpaceship(GameData.getNamePlayer());

    }

    public void showPlayerShip(Player currentPlayer) {
        // Carica immagine di sfondo della nave in base al livello
        int level = GameData.getLevelGame();
        String imgPath = "img/cardboard/spaceship" + level + ".jpg";
        ImageView shipBackgroundImage = new ImageView(
                new Image(MainClient.class.getResourceAsStream(imgPath))
        );
        shipBackgroundImage.setFitWidth(400);
        shipBackgroundImage.setPreserveRatio(true);

        // Crea una nuova griglia per questa visualizzazione
        GridPane dynamicShipGrid = new GridPane();
        dynamicShipGrid.setStyle("-fx-background-color: transparent;");
        dynamicShipGrid.setTranslateX(level == 1 ? 59.0 : 16.0); // Usa translate invece di layoutX
        dynamicShipGrid.setTranslateY(12.0);

        // Popola dinamicamente la griglia
        renderShipComponents(
                currentPlayer.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix(),
                dynamicShipGrid
        );

        // Crea una StackPane con lo sfondo e la griglia della nave
        StackPane shipDisplay = new StackPane(shipBackgroundImage, dynamicShipGrid);

        // Mostra il risultato nella StackPane centrale (fx:id="shipContainerPane")
        shipContainerPane.getChildren().setAll(shipDisplay);
    }

    private void renderShipComponents(Component[][] layout, GridPane grid) {
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                if (layout[row][col] != null) {
                    String component = layout[row][col].getImgSrc();
                    Image img = new Image(String.valueOf(MainClient.class.getResource("img/components/"+component)));
                    ImageView part = new ImageView(img);
                    part.setFitWidth(48);
                    part.setFitHeight(48);
                    grid.add(part, col, row);
                }
            }
        }
    }


}