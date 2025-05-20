package org.progetto.client.gui;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.progetto.client.MainClient;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.BuildingData;
import org.progetto.client.model.GameData;
import org.progetto.client.tui.TuiCommandFilter;
import org.progetto.client.tui.TuiPrinters;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.CardType;
import org.progetto.server.model.events.EventCard;
import org.progetto.server.model.events.Planets;

import java.io.IOException;
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
    private BorderPane eventPane;

    @FXML
    private ImageView eventCard;

    @FXML
    private TextArea consoleOutput;

    @FXML
    private TextField consoleInput;

    @FXML
    private FlowPane boxContainer;

    @FXML
    private VBox overlayContainer;


    final int COMPONENT_SIZE = 110;
    final int BOX_SLOT_SIZE = 35;
    final int CREW_SLOT_SIZE = 35;
    final int BATTERY_SLOT_WIDTH = 20;
    final int BATTERY_SLOT_HEIGHT = 50;


    private CompletableFuture<String> inputFuture;
    private CompletableFuture<int[]> componentClickFuture;
    private CompletableFuture<Integer> planetClickFuture;

    public void initEvent() {
        GameData.getSender().showSpaceship(GameData.getNamePlayer());

        //initialize background
        Image img = null;
        if (GameData.getLevelGame() == 1)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-1.png")));

        else if (GameData.getLevelGame() == 2)
            img = new Image(String.valueOf(MainClient.class.getResource("img/space-background-2.png")));

        BackgroundImage backgroundImage = new BackgroundImage(
                img,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(
                        100, 100, true, true, false, true
                )
        );

        Background background = new Background(backgroundImage);
        eventPane.setBackground(background);
    }

    /**
     * Initialize the event card picked
     *
     * @param imgSrc id the image path to the card
     * @author Lorenzo
     */
    public void initEventCard(String imgSrc) {

        EventCard card = GameData.getActiveCard();

        Image img = new Image(String.valueOf(MainClient.class.getResource("img/cards/" + imgSrc)));
        eventCard.setImage(img);

        overlayContainer.getChildren().clear();

        switch (card.getType()) {

            case PLANETS:
                Planets planets = (Planets) card;
                double cardHeight = 450;
                double rowHeight = cardHeight / planets.getRewardsForPlanets().size();

                for (int i = 0; i < planets.getRewardsForPlanets().size(); i++) {
                    StackPane stack_zone = new StackPane();
                    stack_zone.setPrefSize(eventCard.getFitWidth(), rowHeight);
                    stack_zone.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    int planetIndex = i;
                    stack_zone.setUserData(planetIndex);
                    stack_zone.setOnMouseClicked(e -> {
                        if (planetClickFuture != null && !planetClickFuture.isDone()) {
                            int planetIdx = (int) stack_zone.getUserData();
                            planetClickFuture.complete(planetIdx);

                        }
                    });

                    ImageView pawnView = new ImageView();
                    pawnView.setFitHeight(60);
                    pawnView.setFitWidth(60);
                    pawnView.setMouseTransparent(true);

                    StackPane.setAlignment(pawnView, Pos.BOTTOM_LEFT);
                    StackPane.setMargin(pawnView, new Insets(5));

                    stack_zone.getChildren().add(pawnView);
                    overlayContainer.getChildren().add(stack_zone);
                }

        }


    }


    public void showGameTrack() throws IOException {

        Sender sender = GameData.getSender();

        PageController.initTravel(GameData.getLevelGame());
        PageController.switchScene("travelPage.fxml", "Travel");

        sender.showTrack();
    }



    /**
     * Enables live view of the player spaceship
     *
     * @param currentPlayer is the current player
     * @author Lorenzo
     */
    public void showPlayerShip(Player currentPlayer) {


        spaceshipMatrix.getChildren().clear();

        int sizeX = 5;
        int sizeY = 5;

        if (currentPlayer.getSpaceship().getLevelShip() == 2) {
            spaceshipMatrix.setLayoutX(190.0);
            sizeX = 7;
        }

        // Spaceship matrix
        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(110, 110);

                if (BuildingData.getCellMask(col, row))
                    cell.setId("spaceshipCell");

                spaceshipMatrix.add(cell, col, row);
            }
        }

        renderShipComponents(currentPlayer.getSpaceship().getBuildingBoard().getCopySpaceshipMatrix());
    }

    private void renderShipComponents(Component[][] layout) {

        for (Node node : this.spaceshipMatrix.getChildren()) {
            if (node instanceof Pane cell) {
                Integer rowIndex = GridPane.getRowIndex(cell);
                Integer colIndex = GridPane.getColumnIndex(cell);

                Component component = layout[rowIndex][colIndex];

                if (!cell.getChildren().isEmpty() && component == null)
                    cell.getChildren().clear();

                if (component != null){
                    Pane componentPane = generateComponentPane(component);
                    componentPane.setRotate(90 * component.getRotation());
                    cell.getChildren().add(componentPane);
                }
            }
        }

    }

    private Pane generateComponentPane(Component component) {
        String componentImg = component.getImgSrc();
        ImageView part = new ImageView(new Image(String.valueOf(MainClient.class.getResource("img/components/" + componentImg))));
        part.setUserData(component);
        part.setFitWidth(COMPONENT_SIZE);
        part.setFitHeight(COMPONENT_SIZE);

        Pane componentPane = new Pane();
        componentPane.setPrefSize(COMPONENT_SIZE, COMPONENT_SIZE);

        componentPane.getChildren().add(part);

        switch (component) {
            case BoxStorage boxStorage -> {

                switch (boxStorage.getCapacity()) {
                    case 1:
                        Pane slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(36);
                        slot1.setLayoutY(36);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        componentPane.getChildren().add(slot1);
                        break;

                    case 2:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(37);
                        slot1.setLayoutY(17);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(38);
                        slot2.setLayoutY(58);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        break;

                    case 3:
                        slot1 = new Pane();
                        slot1.setId("boxSlot");
                        slot1.setLayoutX(18);
                        slot1.setLayoutY(35);
                        slot1.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("boxSlot");
                        slot2.setLayoutX(59);
                        slot2.setLayoutY(18);
                        slot2.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("boxSlot");
                        slot3.setLayoutX(59);
                        slot3.setLayoutY(59);
                        slot3.setPrefSize(BOX_SLOT_SIZE, BOX_SLOT_SIZE);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            case HousingUnit housingUnit -> {
                Pane slot1 = new Pane();
                slot1.setId("crewSlot");
                slot1.setLayoutX(12);
                slot1.setLayoutY(37);
                slot1.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot1.getProperties().put("idx", 0);

                Pane slot2 = new Pane();
                slot2.setId("crewSlot");
                slot2.setLayoutX(66);
                slot2.setLayoutY(38);
                slot2.setPrefSize(CREW_SLOT_SIZE, CREW_SLOT_SIZE);
                slot2.getProperties().put("idx", 1);

                componentPane.getChildren().add(slot1);
                componentPane.getChildren().add(slot2);
            }
            case BatteryStorage batteryStorage -> {
                switch (batteryStorage.getCapacity()) {
                    case 2:
                        Pane slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(33);
                        slot1.setLayoutY(30);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        Pane slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(54);
                        slot2.setLayoutY(30);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);

                        break;
                    case 3:
                        slot1 = new Pane();
                        slot1.setId("batterySlot");
                        slot1.setLayoutX(25);
                        slot1.setLayoutY(30);
                        slot1.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot1.getProperties().put("idx", 0);

                        slot2 = new Pane();
                        slot2.setId("batterySlot");
                        slot2.setLayoutX(43);
                        slot2.setLayoutY(30);
                        slot2.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot2.getProperties().put("idx", 1);

                        Pane slot3 = new Pane();
                        slot3.setId("batterySlot");
                        slot3.setLayoutX(66);
                        slot3.setLayoutY(30);
                        slot3.setPrefSize(BATTERY_SLOT_WIDTH, BATTERY_SLOT_HEIGHT);
                        slot3.getProperties().put("idx", 2);

                        componentPane.getChildren().add(slot1);
                        componentPane.getChildren().add(slot2);
                        componentPane.getChildren().add(slot3);
                        break;
                }
            }
            default -> {
            }
        }

        switch (component.getRotation()) {
            case 0:
                componentPane.setRotate(0);
                break;
            case 1:
                componentPane.setRotate(90);
                break;
            case 2:
                componentPane.setRotate(180);
                break;
            case 3:
                componentPane.setRotate(270);
                break;
        }

        return componentPane;
    }


    /**
     * Gets the spaceship matrix
     *
     * @author Alessandro
     */
    public GridPane getSpaceshipMatrix() {
        return spaceshipMatrix;
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

        componentClickFuture = new CompletableFuture<>();

        enableComponentClickSelection();

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

        componentClickFuture = new CompletableFuture<>();

        enableComponentClickSelection();

        componentClickFuture.thenAccept(coords -> {
            int x = coords[0];
            int y = coords[1];

            printToTerminal("Selected cell: (" + x + ", " + y + ")");

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
     * @param printed indicates if the text has been already printed
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
     * @param printed indicates if the text has been already printed
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
     * @param printed indicates if the text has been already printed
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
     * @param printed indicates if the text has been already printed
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

    /**
     * Render the available boxes on the view
     *
     * @author Lorenzo
     * @param availableBoxes
     */
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

            boxContainer.getChildren().add(boxImage);

            for(Node node: boxContainer.getChildren()){
                ImageView frame = (ImageView) node;
                DragAndDrop.enableDragAndDropItems(frame,"boxSlot");
            }

        }
    }

    /**
     * Let the player decide from a list of boxes witch one to keep
     *
     * @author Lorenzo
     * @param availableBoxes is the list of all available boxes
     */
    public void responseRewardBox(ArrayList<Box> availableBoxes) {//todo

    }

    /**
     * Handles player decision to land on a lost station
     *
     * @author Lorenzo
     * @param printed indicates if the text has been already printed
     */
    public void responseLandRequest(boolean printed) {
        if(!printed) {
            printToTerminal("Capitan, abandoned ship in front of us!");
            printToTerminal("do you want to land? (YES or NO)");
        }

        // Prepares future answer
        inputFuture = new CompletableFuture<>();

        // When user responds
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

    /**
     * Handles player decision to land on a planet
     *
     * @author Lorenzo
     * @author Alessandro
     * @param planets
     * @param planetsTaken is the array of available planets
     * @param printed indicates if the text has been already printed
     */
    public void responsePlanetLandRequest(ArrayList<ArrayList<Box>> planets, boolean[] planetsTaken,boolean printed) {

        if (!printed)
            printToTerminal("Do you want to land? (YES or NO)");

        // Prepares future answer
        inputFuture = new CompletableFuture<>();

        // When user responds
        inputFuture.thenAccept(response -> {

            if (response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")) {
                Sender sender = GameData.getSender();

                if (response.equalsIgnoreCase("NO")) {
                    sender.responsePlanetLandRequest(-1);
                    return;
                }

                sender.responseLandRequest(response);
                responsePlanetIndex(planets,planetsTaken,false);
            } else {
                printToTerminal("You must choose between YES or NO");
                responsePlanetLandRequest(planets, planetsTaken, true);
            }
        });


    }

    /**
     *Handles planet decision
     *
     * @author Lorenzo
     * @param planets
     * @param planetsTaken
     * @param printed indicates if the text has been already printed
     */
    public void responsePlanetIndex(ArrayList<ArrayList<Box>> planets, boolean[] planetsTaken,boolean printed){
        if(!printed)
            printToTerminal("Click on the planet where you want to land");

        //place player's pawn to the planet taken
        for(int i = 0; i<planetsTaken.length; i++) {
            if(planetsTaken[i]){
                StackPane targetStack = (StackPane) overlayContainer.getChildren().get(i);
                ImageView pawnView = (ImageView) targetStack.getChildren();
                switch(GameData.getColor()){
                    case 0 ->  pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/blue_pawn.png"))));
                    case 1 ->  pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/green_pawn.png"))));
                    case 2 ->  pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/red_pawn.png"))));
                    case 3 ->  pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/yellow_pawn.png"))));
                }
            }
        }

        planetClickFuture = new CompletableFuture<>();

        planetClickFuture.thenAccept(response -> {

            Sender sender = GameData.getSender();

            int planet_idx = Integer.parseInt(String.valueOf(response));

            if(planet_idx < 0 || planet_idx >= planetsTaken.length){
                printToTerminal("Invalid planet index");
                responsePlanetIndex(planets, planetsTaken, true);
            }

            if(planetsTaken[planet_idx]){
                printToTerminal("Planet already taken");
                responsePlanetIndex(planets, planetsTaken, true);
            }

            else {
                sender.responsePlanetLandRequest(planet_idx);
                printToTerminal("Landed on planet number " + response.toString());
                StackPane targetStack = (StackPane) overlayContainer.getChildren().get(response);
                ImageView pawnView = (ImageView) targetStack.getChildren().getFirst();
                System.out.println(pawnView.getParent());
                System.out.println(GameData.getColor());
                switch (GameData.getColor()) {
                    case 0 ->
                            pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/green_pawn.png"))));
                    case 1 ->
                            pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/red_pawn.png"))));
                    case 2 ->
                            pawnView.setImage(new Image(String.valueOf(MainClient.class.getResource("img/items/yellow_pawn.png"))));
                }


            }



        });


    }




}