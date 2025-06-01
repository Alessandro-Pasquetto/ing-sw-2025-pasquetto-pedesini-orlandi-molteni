package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.events.*;

import java.util.ArrayList;
import java.util.Scanner;

public class EventCommands {

    // =======================
    // COMMANDS
    // =======================

    /**
     * Handles player decision on how many double cannons to use
     *
     * @author Lorenzo
     * @param required is the firepower required
     * @param max is the total amount of usable double cannons
     */
    public static void responseHowManyDoubleCannons(int required, int max, float shootingPower) {

        while(true){
            System.out.println("How many double cannons do you want to activate?");
            if(required == 0)
                System.out.println("You have " + max + " double cannons");
            else
                System.out.println("Firepower required is " + required + ", you have " + shootingPower + " shooting power and " + max + " double cannons");

            String response = TuiCommandFilter.waitResponse();

            Sender sender = GameData.getSender();
            try{
                int amount = Integer.parseInt(response);
                if (amount > max)
                    System.err.println("You have exceeded the maximum number of double cannons!");
                else{
                    sender.responseHowManyDoubleCannons(amount);
                    break;
                }

            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Handles player decision on how many double engines to use
     *
     * @author Lorenzo, Alessandro
     * @param max is the total amount of usable double engines
     * @param enginePower is the required engine power
     */
    public static void responseHowManyDoubleEngines(int max, int enginePower) {

        while(true){
            System.out.println("How many double engines do you want to activate?");
            System.out.println("You have " + enginePower + " engine power and " + max + " double engines");

            String response = TuiCommandFilter.waitResponse();

            try{
                int amount = Integer.parseInt(response);
                if (amount > max)
                    System.err.println("You have exceeded the maximum number of double engines!");
                else{
                    Sender sender = GameData.getSender();
                    sender.responseHowManyDoubleEngines(amount);
                    break;
                }

            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Let the player decide were to discard the batteries
     *
     * @author Lorenzo
     * @param required is the needed amount of batteries
     */
    public static void responseBatteryToDiscard(int required) {

        while(true){
            System.out.println("Select the battery storage from which to remove the battery");
            System.out.println("You need to discard " + required + " batteries");

            System.out.print("X: ");
            String x = TuiCommandFilter.waitResponse();
            System.out.print("Y: ");
            String y = TuiCommandFilter.waitResponse();

            Sender sender = GameData.getSender();
            int levelGame = GameData.getLevelGame();
            try{
                sender.responseBatteryToDiscard(Integer.parseInt(x) - 6 + levelGame, Integer.parseInt(y) - 5);
                break;
            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Let the player decide were to discard the crew members
     *
     * @author Lorenzo
     * @param required is the needed amount of crew members
     */
    public static void responseCrewToDiscard(int required) {

        while(true){
            System.out.println("Select the housing unit from which to remove the crew member");
            System.out.println("You need to discard " + required + " crew members");

            System.out.print("X: ");
            String x = TuiCommandFilter.waitResponse();
            System.out.print("Y: ");
            String y = TuiCommandFilter.waitResponse();

            Sender sender = GameData.getSender();
            int levelGame = GameData.getLevelGame();
            try{
                sender.responseCrewToDiscard(Integer.parseInt(x) - 6 + levelGame, Integer.parseInt(y) - 5);
                break;
            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * let the player decide were the discarded boxes will be picked
     *
     * @author Lorenzo
     * @param required is the boxes amount to discard
     */
    public static void responseBoxToDiscard(int required) {

        while(true){
            System.out.println("Select the box storage from which to remove the box: <X> <Y> <storage_idx>");
            System.out.println("You need to discard " + required + " boxes");

            System.out.print("X: ");
            String x = TuiCommandFilter.waitResponse();
            System.out.print("Y: ");
            String y = TuiCommandFilter.waitResponse();
            System.out.print("Storage idx: ");
            String idx = TuiCommandFilter.waitResponse();

            Sender sender = GameData.getSender();
            int levelGame = GameData.getLevelGame();
            try{
                sender.responseBoxToDiscard(Integer.parseInt(x) - 6 + levelGame, Integer.parseInt(y) - 5, Integer.parseInt(idx));
                break;
            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Handles player decision to use a shield
     *
     * @author Lorenzo
     */
    public static void responseChooseToUseShield() {

        while(true){
            System.out.println("Do you want to use a shield? (YES or NO)");

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseChooseToUseShield(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles player decision to accept reward credits and penalty
     *
     * @author Lorenzo
     * @param reward is the credit reward
     * @param penaltyDays are the days of penalty
     * @param penaltyCrew are the crew to discard by penalty
     */
    public static void responseAcceptRewardCreditsAndPenalties(int reward, int penaltyDays, int penaltyCrew) {

        while(true){
            System.out.println("Do you want to accept the reward and penalties? (YES or NO)");
            System.out.println("Reward credits: " + reward + ", Days of penalty: " + penaltyDays + ", Crew to discard: " + penaltyCrew);

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenalties(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles player decision to accept reward credits and penalty days
     *
     * @author Lorenzo
     * @param reward is the credit reward
     * @param penaltyDays are the days of penalty
     */
    public static void responseAcceptRewardCreditsAndPenaltyDays(int reward, int penaltyDays){

        while(true){
            System.out.println("Do you want to accept the reward and the days penalty? (YES or NO)");
            System.out.println("Reward credits: " + reward + ", Days of penalty: " + penaltyDays);

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles player decision to accept reward boxes and penalty days
     *
     * @author Lorenzo
     * @param reward are the reward boxes
     * @param penaltyDays are the days of penalty
     */
    public static void responseAcceptRewardBoxesAndPenaltyDays(ArrayList<Box> reward, int penaltyDays){

        while (true) {
            System.out.println("Do you want to accept the reward and the days penalty? (YES or NO)");
            System.out.print("Days of penalty: " + penaltyDays + ", Reward boxes: ");

            for (int i = 0; i < reward.size(); i++) {
                String box = TuiPrinters.drawBox(reward.get(i));
                System.out.printf("%s ", box);
            }

            System.out.println();

            String response = TuiCommandFilter.waitResponse();

            if (response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseAcceptRewardCreditsAndPenaltyDays(response);
                break;
            } else
                System.err.println("You must choose between YES or NO");
        }
    }


    /**
     * Let the player decide from a list of boxes witch one to keep
     *
     * @author Lorenzo
     * @param availableBoxes is the list of all available boxes
     */
    public static void responseRewardBox(ArrayList<Box> availableBoxes) {

        while(true){

            System.out.println("Box list:");

            for (int i = 0; i < availableBoxes.size(); i++) {
                String box = TuiPrinters.drawBox(availableBoxes.get(i));

                System.out.printf("[%d] %s%n", i + 1, box);
            }

            System.out.println("Select an available box by index from the list (-1 if you wanna leave)");
            String idx = TuiCommandFilter.waitResponse();

            try{
                int box_idx = Integer.parseInt(idx);

                Sender sender = GameData.getSender();

                if(box_idx == -1){
                    sender.responseRewardBox(box_idx, -1, -1, -1);
                    break;
                }

                else if (box_idx > 0 && box_idx <= availableBoxes.size()){
                    System.out.println("Select a box storage were you want to insert the box: ");
                    System.out.print("X: ");
                    int x = Integer.parseInt(TuiCommandFilter.waitResponse());
                    System.out.print("Y: ");
                    int y = Integer.parseInt(TuiCommandFilter.waitResponse());
                    System.out.print("Storage idx: ");
                    int storage_idx = Integer.parseInt(TuiCommandFilter.waitResponse());

                    int levelGame = GameData.getLevelGame();
                    sender.responseRewardBox(box_idx - 1, x - 6 + levelGame, y - 5, storage_idx);
                    break;
                }else{
                    System.err.println("Box index out of bounds!");
                }

            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Handles player decision to move a box
     *
     * @author Gabriele
     * @param commandParts is the command parts
     */
    public static void moveBox(String[] commandParts) {
        Sender sender = GameData.getSender();
        int levelGame = GameData.getLevelGame();

        int xStart = 0;
        int yStart = 0;
        int idxStart = 0;
        int xEnd = 0;
        int yEnd = 0;
        int idxEnd = 0;

        try {
            xStart = Integer.parseInt(commandParts[1]) - 6 + levelGame;
            yStart = Integer.parseInt(commandParts[2]) - 5;
            idxStart = Integer.parseInt(commandParts[3]);
            xEnd = Integer.parseInt(commandParts[4]) - 6 + levelGame;
            yEnd = Integer.parseInt(commandParts[5]) - 5;
            idxEnd = Integer.parseInt(commandParts[6]);

        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }

        sender.moveBox(xStart, yStart, idxStart, xEnd, yEnd, idxEnd);
    }

    /**
     * Handles player decision to remove a box
     *
     * @author Gabriele
     * @param commandParts is the command parts
     */
    public static void removeBox(String[] commandParts) {
        Sender sender = GameData.getSender();
        int levelGame = GameData.getLevelGame();

        int x = 0;
        int y = 0;
        int idx = 0;

        try {
            x = Integer.parseInt(commandParts[1]) - 6 + levelGame;
            y = Integer.parseInt(commandParts[2]) - 5;
            idx = Integer.parseInt(commandParts[3]);

        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }

        sender.removeBox(x, y, idx);
    }

    /**
     * Handles player decision to land on a lost station
     *
     * @author Lorenzo
     */
    public static void responseLandRequest() {

        while(true){
            System.out.println("Do you want to land? (YES or NO)");

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseLandRequest(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles player decision to land on a planet
     *
     * @author Lorenzo
     * @author Alessandro
     * @param planetsTaken is the array of available planets
     */
    public static void responsePlanetLandRequest(ArrayList<ArrayList<Box>> planets, boolean[] planetsTaken) {

        while (true) {
            System.out.println("Do you want to land? (YES or NO)");

            String response = TuiCommandFilter.waitResponse();

            if (!response.equalsIgnoreCase("YES") && !response.equalsIgnoreCase("NO")) {
                System.err.println("You must choose between YES or NO");
                continue;
            }

            Sender sender = GameData.getSender();

            if (response.equalsIgnoreCase("NO")) {
                sender.responsePlanetLandRequest(-1);
                return;
            }

            System.out.println("In which planet do you want to land?");

            int currentPlanet = 0;

            for (int i = 0; i < planetsTaken.length; i++) {
                if (planetsTaken[i]) {
                    System.out.println(String.format("[%d] Planet: TAKEN", i + 1));
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (var box : planets.get(currentPlanet)) {
                        sb.append(TuiPrinters.drawBox(box)).append(" ");
                    }
                    System.out.println(String.format("[%d] Planet: %s", i + 1, sb));
                }

                currentPlanet++;
            }

            String idx = TuiCommandFilter.waitResponse();

            try {
                int planet_idx = Integer.parseInt(idx) - 1;

                if (planet_idx < 0 || planet_idx >= planetsTaken.length) {
                    System.err.println("Invalid planet index");
                    continue;
                }

                if (planetsTaken[planet_idx]) {
                    System.err.println("Planet already taken");
                    continue;
                }

                sender.responsePlanetLandRequest(planet_idx);
                break;

            } catch (NumberFormatException e) {
                System.err.println("You must insert a number!");
            }
        }
    }

    /**
     * Handles player decision to use a double cannon to protect in meteors rain
     *
     * @author Lorenzo
     */
    public static void responseUseDoubleCannonRequest() {
        while(true){
            System.out.println("Do you want to use a double cannon? (YES or NO)");

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseUseDoubleCannonRequest(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles player decision to continue travel
     *
     * @author Gabriele
     */
    public static void responseContinueTravel() {
        while (true) {
            System.out.println("Do you want to continue travel? (YES or NO)");

            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("YES") || response.equalsIgnoreCase("NO")){
                Sender sender = GameData.getSender();
                sender.responseContinueTravel(response);
                break;
            }else
                System.err.println("You must choose between YES or NO");
        }
    }

    /**
     * Handles roll dice by current player
     *
     * @author Gabriele
     */
    public static void responseRollDice() {
        while (true) {
            String response = TuiCommandFilter.waitResponse();

            if (response.equalsIgnoreCase("ROLL")){
                Sender sender = GameData.getSender();
                sender.responseRollDice();
                break;
            } else
                System.err.println("You must say ROLL");
        }
    }

    /**
     * Handles player decision to keep a spaceship branch
     *
     * @author Alessandro
     */
    public static void responseSelectSpaceshipPart() {

        while (true) {
            System.out.println("Select the component located in the part of the ship to be maintained");

            System.out.print("X: ");
            String x = TuiCommandFilter.waitResponse();
            System.out.print("Y: ");
            String y = TuiCommandFilter.waitResponse();

            Sender sender = GameData.getSender();
            int levelGame = GameData.getLevelGame();
            try{
                sender.responseSelectSpaceshipPart(Integer.parseInt(x) - 6 + levelGame, Integer.parseInt(y) - 5);
                break;
            }catch (NumberFormatException e){
                System.err.println("You must insert a number!");
            }
        }
    }
}