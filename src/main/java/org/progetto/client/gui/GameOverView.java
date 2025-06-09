package org.progetto.client.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Player;

import java.io.IOException;
import java.util.ArrayList;

public class GameOverView {

    // =======================
    // ATTRIBUTES
    // =======================

    @FXML
    private StackPane gameOverRoomPane;

    @FXML
    private Label endGameTitle;

    @FXML
    public Label endGameDesc;

    @FXML
    private ImageView resultImage;

    @FXML
    private VBox scoreboardContainer;

    // =======================
    // METHODS
    // =======================

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
     * @param resultGame is the result of the game. win (0), lose (1)
     */
    public void initGameOver(int resultGame) {

        Image img = null;

        switch (resultGame){
            case 0:
                endGameTitle.setText("CONGRATULATIONS, YOU WON!");
                endGameDesc.setText("You ended the game with more than 0 credits, so you won!");
                img = new Image(String.valueOf(MainClient.class.getResource("img/win.png")));
                break;

            case 1:
                endGameTitle.setText("OH NO, YOU LOST!");
                endGameDesc.setText("You ended the game with 0 credits, so you lost!");
                img = new Image(String.valueOf(MainClient.class.getResource("img/lost.png")));
                break;
        }

        resultImage.setImage(img);
    }

    /**
     * Initializes the scoreboard
     *
     * @author Gabriele
     * @param scoreboard is the list of players to be displayed in the scoreboard
     */
    public void initScoreboard(ArrayList<Player> scoreboard) {
        scoreboardContainer.getChildren().clear();

        scoreboard.sort((p1, p2) -> Integer.compare(p2.getCredits(), p1.getCredits()));

        for (int i = 0; i < scoreboard.size(); i++) {
            Player p = scoreboard.get(i);

            Label rankLabel = new Label("#" + (i + 1));
            Label nameLabel = new Label(p.getName());
            Label scoreLabel = new Label(p.getCredits() + " credits");

            rankLabel.getStyleClass().add("score-rank");
            nameLabel.getStyleClass().add("score-name");
            scoreLabel.getStyleClass().add("score-points");

            HBox row = new HBox(20, rankLabel, nameLabel, scoreLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.setMaxWidth(600);
            row.getStyleClass().add("score-row");

            HBox.setHgrow(scoreLabel, Priority.ALWAYS);
            scoreLabel.setMaxWidth(Double.MAX_VALUE);
            scoreLabel.setAlignment(Pos.CENTER_RIGHT);

            scoreboardContainer.getChildren().add(row);
        }
    }

    /**
     * Allows the player to return to the lobby page
     *
     * @author Alessandro
     * @throws IOException if the page cannot be loaded
     */
    public void returnToLobby() throws IOException {
        Sender sender = GameData.getSender();
        sender.leaveGame();

        GameData.resetData();

        PageController.loadControllers();

        PageController.switchScene("chooseGame.fxml", "ChooseGame");
        sender.updateGameList();
    }
}