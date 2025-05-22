package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;

import java.io.IOException;

public class GameOverView {

    @FXML
    private StackPane gameOverRoomPane;

    @FXML
    private Label endGameTitle;

    @FXML
    private ImageView resultImage;


    /**
     * Initializes the background
     *
     * @author Lorenzo
     * @param levelGame is the game level
     */
    public void initBackground(int levelGame) {

        // Initialize background
        Image img = null;
        if(levelGame == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-1.png")));

        else if(levelGame == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-2.png")));

        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,   // horizontal repetition
                BackgroundRepeat.NO_REPEAT,   // vertical repetition
                BackgroundPosition.CENTER,    // position
                new BackgroundSize(
                        100, 100, true, true, false, true
                )
        );

        Background background = new Background(backgroundImage);
        gameOverRoomPane.setBackground(background);
    }

    /**
     * Initialize the gameOver page
     *
     * @author Lorenzo
     * @param resultGame is the result of the game. win(1), lose(1)
     */
    public void initGameOver(int resultGame) {

        Image img = null;
        switch (resultGame){
            case 0:
                endGameTitle.setText("CONGRATULATIONS, YOU WIN!");
                img = new Image(String.valueOf(MainClient.class.getResource("img/win.png")));
                break;

            case 1:
                endGameTitle.setText("OH NO, YOU LOST!");
                img = new Image(String.valueOf(MainClient.class.getResource("img/lost.png")));
                break;
        }

        resultImage.setImage(img);
    }

    /**
     * Connected to the button, allows the player to return to the lobby page
     *
     * @author Lorenzo
     * @throws IOException
     */
    public void returnToLobby() throws IOException {
        Sender sender = GameData.getSender();
        sender.leaveGame();

        GameData.resetData();
       // PageController.loadControllers();

        PageController.loadControllers();

        PageController.switchScene("chooseGame.fxml", "ChooseGame");
        sender.updateGameList();
    }
}