package org.progetto.client.gui;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.TuiCommandFilter;
import org.progetto.client.tui.TuiPrinters;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.EventCard;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


public class EventView {

    @FXML
    private StackPane shipContainerPane;
    @FXML
    private GridPane spaceshipMatrix;
    @FXML
    private ImageView shipBackgroundImage;
    @FXML
    private BorderPane rootPane;
    @FXML
    private ImageView eventCard;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private TextField consoleInput;
    @FXML
    private FlowPane boxContainer;


    private CompletableFuture<String> inputFuture;
    private CompletableFuture<int[]> componentClickFuture;





    public void initEvent() {

        GameData.getSender().showSpaceship(GameData.getNamePlayer());

        //initialize background
        Image img = null;
        if(GameData.getLevelGame() == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background_1.png")));

        else if(GameData.getLevelGame() == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background_2.png")));

        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,   // ripetizione orizzontale
                BackgroundRepeat.NO_REPEAT,   // ripetizione verticale
                BackgroundPosition.CENTER,    // posizione
                new BackgroundSize(
                        100, 100, true, true, false, true
                )
        );

        Background background = new Background(backgroundImage);
        rootPane.setBackground(background);


    }

    public void initEventCard(String imgSrc) {
        Image img = new Image(String.valueOf(MainClient.class.getResource("img/cards/" + imgSrc)));
        eventCard.setImage(img);
    }

    public void showPlayerShip(Player currentPlayer) {

//        // Imposta immagine di sfondo
//        int level = GameData.getLevelGame();
//        String imgPath = "img/cardboard/spaceship" + level + ".jpg";
//        shipBackgroundImage.setImage(new Image(String.valueOf(MainClient.class.getResource(imgPath))));

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
                    part.setUserData(layout[row][col]);
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

    /**
     * Enable selection of a component by click
     *
     * @author Lorenzo
     */
    private void enableComponentClickSelection() {
        for (Node node : spaceshipMatrix.getChildren()) {
            node.setOnMouseClicked(e -> {
                if (componentClickFuture != null && !componentClickFuture.isDone()) {
                    Component component = (Component) node.getUserData();
                    if (component != null) {
                        componentClickFuture.complete(new int[]{component.getX(), component.getY()});
                    }
                }
            });
        }
    }


    /**
     * Disable the onclick on the component
     *
     */
    private void disableComponentClickSelection() {
        for (Node node : spaceshipMatrix.getChildren()) {
            node.setOnMouseClicked(null);
        }
    }



    /**
     * Prints all the incoming messages
     *
     * @author Lorenzo
     * @param message is the message to print
     */
    public void printToTerminal(String message) {
        consoleOutput.appendText(">: "+message + "\n");
    }

    /**
     * Read the input command from the text terminal
     *
     * @author Lorenzo
     */
    @FXML
    private void handleInput() {
        String userInput = consoleInput.getText().trim();

        if (!userInput.isEmpty()) {
            printToTerminal(userInput);

            if (inputFuture != null && !inputFuture.isDone()) {
                inputFuture.complete(userInput); // Completa il blocco logico
            }
            consoleInput.clear();
        }
    }

    /**
     * Handles player decision on how many double cannons to use
     *
     * @author Lorenzo
     * @param required is the required amount
     * @param max is the maximum amount of double cannons
     * @param shootingPower is the current power
     * @param printed indicates if the text has been already printed
     */
    public void responseHowManyDoubleCannons(int required, int max, float shootingPower,boolean printed) {
        if(!printed)
            printToTerminal("Sometimes violence is the answer,how many double cannons do you want to activate?");


        inputFuture = new CompletableFuture<>();

        inputFuture.thenAccept(response -> {
            try {
                int amount = Integer.parseInt(response);
                if (amount > max) {
                    printToTerminal("You have exceeded the maximum number of double cannons!");
                    responseHowManyDoubleCannons(required,max,shootingPower,true);
                } else {
                    GameData.getSender().responseHowManyDoubleCannons(amount);
                }
            } catch (NumberFormatException e) {
                printToTerminal("You must insert a number!");
                responseHowManyDoubleCannons(required,max,shootingPower,true);
            }
        });
    }


    /**
     * Handles player decision on how many double engines to use
     *
     * @author Lorenzo, Alessandro
     * @param max is the total amount of usable double engines
     * @param printed indicates if the text has been already printed
     */
    public void responseHowManyDoubleEngines(int max, int enginePower,boolean printed) {
        if(!printed)
            printToTerminal("We need to go fast!, how many double engines do you want to activate?");


        inputFuture = new CompletableFuture<>();

        inputFuture.thenAccept(response -> {
            try {
                int amount = Integer.parseInt(response);
                if (amount > max) {
                    printToTerminal("You have exceeded the maximum number of double engines!");
                    responseHowManyDoubleEngines(max,enginePower,true);
                } else {
                    GameData.getSender().responseHowManyDoubleEngines(amount);
                }
            } catch (NumberFormatException e) {
                printToTerminal("You must insert a number!");
                responseHowManyDoubleEngines(max,enginePower,true);
            }
        });
    }

    /**
     * Let the player decide were to discard the batteries
     *
     * @author Lorenzo
     * @param required is the needed amount of batteries
     */
    public  void responseBatteryToDiscard(int required) {

        printToTerminal("Select the battery storage from which to remove the battery");
        printToTerminal("You need to discard " + required + " batteries");

        // Prepara la futura risposta
        componentClickFuture = new CompletableFuture<>();

        // Attiva click su componenti
        enableComponentClickSelection();

        // Quando l'utente risponde
        componentClickFuture.thenAccept(coords -> {
            int x = coords[0];
            int y = coords[1];

            printToTerminal("Selected cell: (" + x + ", " + y + ")");

            GameData.getSender().responseBatteryToDiscard(x,y);

            disableComponentClickSelection();
        });
    }

    /**
     * Let the player decide were to discard the crew members
     *
     * @author Lorenzo
     * @param required is the needed amount of crew members
     */
    public void responseCrewToDiscard(int required) {

        printToTerminal("Select the housing unit from which to remove the battery");
        printToTerminal("You need to discard " + required + " crew members");

        // Prepara la futura risposta
        componentClickFuture = new CompletableFuture<>();

        // Attiva click su componenti
        enableComponentClickSelection();

        // Quando l'utente risponde
        componentClickFuture.thenAccept(coords -> {
            int x = coords[0];
            int y = coords[1];

            printToTerminal("Selected cell: (" + x + ", " + y + ")");

            // Invia le coordinate al server (puoi adattare il metodo)
            GameData.getSender().responseBatteryToDiscard(x,y);

            disableComponentClickSelection();
        });
    }

    /**
     * let the player decide were the discarded boxes will be picked
     *
     * @author Lorenzo
     * @param required is the boxes amount to discard
     */
    public void responseBoxToDiscard(int required) {
        //todo
    }

    /**
     * Handles player decision to use a shield
     *
     * @author Lorenzo
     */
    public void responseChooseToUseShield(boolean printed) {
        if(!printed)
            printToTerminal("Do you want to use a shield? (YES or NO)");


        inputFuture = new CompletableFuture<>();


        inputFuture.thenAccept(response -> {

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
            }else{
                printToTerminal("You must choose between YES or NO");
                responseChooseToUseShield(true);
            }

        });
    }


    /**
     * Handles player decision to accept reward credits and penalty
     *
     * @author Lorenzo
     * @param reward is the credit reward
     * @param penaltyDays are the days of penalty
     * @param penaltyCrew are the crew to discard by penalty
     */
    public void responseAcceptRewardCreditsAndPenalties(int reward, int penaltyDays, int penaltyCrew,boolean printed) {

        if(!printed) {
            printToTerminal("Do you want to accept the reward and penalties? (YES or NO)");
        }


        inputFuture = new CompletableFuture<>();


        inputFuture.thenAccept(response -> {

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
            }else{
                printToTerminal("You must choose between YES or NO");
                responseAcceptRewardCreditsAndPenalties(reward, penaltyDays, penaltyCrew,true);
            }

        });
    }


    /**
     * Handles player decision to accept reward credits and penalty days
     *
     * @author Lorenzo
     * @param reward is the credit reward
     * @param penaltyDays are the days of penalty
     */
    public void responseAcceptRewardCreditsAndPenaltyDays(int reward, int penaltyDays,boolean printed){

        if(!printed) {
            printToTerminal("Do you want to accept the reward and the days penalty? (YES or NO)");
        }


        inputFuture = new CompletableFuture<>();

        inputFuture.thenAccept(response -> {

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
            }else{
                printToTerminal("You must choose between YES or NO");
               responseAcceptRewardCreditsAndPenaltyDays(reward, penaltyDays,true);
            }

        });
    }


    /**
     * Handles player decision to accept reward boxes and penalty days
     *
     * @author Lorenzo
     * @param reward are the reward boxes
     * @param penaltyDays are the days of penalty
     */
    public void responseAcceptRewardBoxesAndPenaltyDays(ArrayList<Box> reward, int penaltyDays,boolean printed){

        if(!printed) {
            printToTerminal("Do you want to accept the reward and the days penalty? (YES or NO)");
        }

        inputFuture = new CompletableFuture<>();

        inputFuture.thenAccept(response -> {

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
            }else{
                printToTerminal("You must choose between YES or NO");
               responseAcceptRewardBoxesAndPenaltyDays(reward, penaltyDays,true);
            }

        });
    }


    public void renderBoxes(ArrayList<Box> availableBoxes){
        boxContainer.getChildren().clear();

        for (Box box : availableBoxes) {
            Image img = switch (box.getValue()) {
                case 1 -> new Image(String.valueOf(MainClient.class.getResource("img/items/BlueBox.png")));
                case 2 -> new Image(String.valueOf(MainClient.class.getResource("img/items/GreenBox.png")));
                case 3 -> new Image(String.valueOf(MainClient.class.getResource("img/items/YellowBox.png")));
                case 4 -> new Image(String.valueOf(MainClient.class.getResource("img/items/RedBox.png")));
                default -> null;
            };

            ImageView boxImage = new ImageView(img);
            boxImage.setFitWidth(100);
            boxImage.setPreserveRatio(true);
            boxImage.setSmooth(true);
            boxImage.setCache(true);

            boxImage.setOnMouseClicked(event -> {

            });

            boxContainer.getChildren().add(boxImage);
        }

    }

    /**
     * Let the player decide from a list of boxes witch one to keep
     *
     * @author Lorenzo
     * @param availableBoxes is the list of all available boxes
     */
    public void responseRewardBox(ArrayList<Box> availableBoxes) {
        //todo
    }


    /**
     * Handles player decision to land on a lost station
     *
     * @author Lorenzo
     */
    public void responseLandRequest(boolean printed) {
        if(!printed) {
            printToTerminal("Capitan, abandoned ship in front of us!");
            printToTerminal("do you want to land? (YES or NO)");
        }

        // Prepara la futura risposta
        inputFuture = new CompletableFuture<>();

        // Quando l'utente risponde
        inputFuture.thenAccept(response -> {

            if (response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")) {
                Sender sender = GameData.getSender();
                sender.responseLandRequest(response);
            } else {
                printToTerminal("You must choose between YES or NO");
                responseLandRequest(true);
            }
        });

    }











}