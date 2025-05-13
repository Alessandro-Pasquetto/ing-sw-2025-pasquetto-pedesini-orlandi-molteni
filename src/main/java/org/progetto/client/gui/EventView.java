package org.progetto.client.gui;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.progetto.client.MainClient;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;


public class EventView {

    @FXML
    private TextArea terminalOutput;
    @FXML
    private TextField terminalInput;
    @FXML
    private StackPane shipContainerPane;
    @FXML
    private GridPane spaceshipMatrix;
    @FXML
    private ImageView shipBackgroundImage;



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

        // Imposta immagine di sfondo
        int level = GameData.getLevelGame();
        String imgPath = "img/cardboard/spaceship" + level + ".jpg";
        shipBackgroundImage.setImage(new Image(String.valueOf(MainClient.class.getResource(imgPath))));

        // Pulisce e riempie la griglia
        spaceshipMatrix.getChildren().clear();

        int sizeX = 5;
        int sizeY = 5;

        if (currentPlayer.getSpaceship().getLevelShip() == 2){
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        // Spaceship matrix
        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(110, 110);

                if(BuildingData.getCellMask(col, row))
                    cell.setId("spaceshipCell");

                spaceshipMatrix.add(cell, col, row);
            }
        }

        renderShipComponents(currentPlayer.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix(), spaceshipMatrix);
    }


    private void renderShipComponents(Component[][] layout, GridPane grid) {
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                Component component = layout[row][col];
                if (component != null) {
                    String componentImg = component.getImgSrc();
                    ImageView part = new ImageView(new Image(String.valueOf(MainClient.class.getResource("img/components/" + componentImg))));
                    part.setFitWidth(120);
                    part.setFitHeight(120);

                    // Inserisci il componente sopra la cella esistente
                    StackPane cellWrapper = new StackPane();
                    cellWrapper.setPrefSize(64, 64);
                    cellWrapper.getChildren().add(part);

                    grid.add(cellWrapper, col, row);
                }
            }
        }
    }


}