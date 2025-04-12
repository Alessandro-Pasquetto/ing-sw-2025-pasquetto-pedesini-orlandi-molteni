package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.components.Box;

import java.util.ArrayList;
import java.util.Scanner;

public class EventCommands {

    private static Scanner scanner = new Scanner(System.in);

    public static String listenResponse(){
        while(true){
            System.out.println();

            String response = scanner.nextLine();
            if(!response.isEmpty())
                return response;
        }
    }

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
    public static void responseHowManyDoubleCannons(int required, int max) {
        System.out.println("How many double cannons?");
        System.out.println("Firepower required is " + required + " and you have" + max + " double cannons");

        String response = listenResponse();
        Sender sender = GameData.getSender();
        try{
            int amount = Integer.parseInt(response);
            if (amount > max)
                System.out.println("You have exceeded the maximum number of double cannons!");
            else
                sender.responseHowManyDoubleCannons(amount);

        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

    /**
     * Handles player decision on how many double engines to use
     *
     * @author Lorenzo
     * @param max is the total amount of usable double engines
     */
    public static void responseHowManyDoubleEngines(int max) {
        System.out.println("How many double engines?");
        System.out.println("You have" + max+" double engines");

        String response = listenResponse();
        Sender sender = GameData.getSender();
        try{
            int amount = Integer.parseInt(response);
            if (amount > max)
                System.out.println("You have exceeded the maximum number of double engines!");
            else
                sender.responseHowManyDoubleEngines(amount);

        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

    /**
     * Let the player decide were to discard the batteries
     *
     * @author Lorenzo
     * @param required is the needed amount of batteries
     */
    public static void responseBatteryToDiscard(int required) {
        System.out.println("Select the battery storage from which to remove the battery");
        System.out.println("You need to discard" + required + " batteries");

        String x = null;
        String y = null;

        System.out.print("X: ");
        x = listenResponse();
        System.out.print("Y: ");
        y = listenResponse();


        Sender sender = GameData.getSender();
        try{
            sender.responseBatteryToDiscard(Integer.parseInt(x),Integer.parseInt(y));
        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

    /**
     * Let the player decide were to discard the crew members
     *
     * @author Lorenzo
     * @param required is the needed amount of crew members
     */
    public static void responseCrewToDiscard(int required) {
        System.out.println("Select the housing unit from which to remove the crew member");
        System.out.println("You need to discard" + required + " crew members");

        String x = null;
        String y = null;

        System.out.print("X: ");
        x = listenResponse();
        System.out.print("Y: ");
        y = listenResponse();

        Sender sender = GameData.getSender();
        try{
            sender.responseCrewToDiscard(Integer.parseInt(x),Integer.parseInt(y));
        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

    /**
     * let the player decide were the discarded boxes will be picked
     *
     * @author Lorenzo
     * @param required is the boxes amount to discard
     */
    public static void responseBoxToDiscard(int required) {
        System.out.println("Select the box storage from which to remove the box");
        System.out.println("You need to discard" + required + " boxes");

        String x = null;
        String y = null;
        String idx = null;

        System.out.print("X: ");
        x = listenResponse();
        System.out.print("Y: ");
        y = listenResponse();
        System.out.print("Storage idx: ");
        idx = listenResponse();

        Sender sender = GameData.getSender();
        try{
            sender.responseBoxToDiscard(Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(idx));
        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

    /**
     * Handles player decision to use a shield
     *
     * @author Lorenzo
     */
    public static void responseChooseToUseShield() {
        System.out.println("Do you want to use a shield?");

        String response = listenResponse();
        if((response.toUpperCase().equals("YES"))||(response.toUpperCase().equals("NO"))){
            Sender sender = GameData.getSender();
            sender.responseChooseToUseShield(response);

        }else
            System.out.println("You must choose between YES or NO");

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
        System.out.println("You want to accept the reward and penalties?");
        System.out.println("Reward: " + reward + " Days of penalty: " + penaltyDays + " Crew to discard: " + penaltyCrew);

        String response = listenResponse();
        if((response.toUpperCase().equals("YES"))||(response.toUpperCase().equals("NO"))){
            Sender sender = GameData.getSender();
            sender.responseAcceptRewardCreditsAndPenalties(response);

        }else
            System.out.println("You must choose between YES or NO");
    }


    //todo void responseAcceptRewardCreditsAndPenaltyDays(String response);


    /**
     * Handles player decision to land on a lost station
     *
     * @author Lorenzo
     */
    public static void responseLandRequest() {
        System.out.println("Do you want to land?");

        String response = listenResponse();
        if((response.toUpperCase().equals("YES"))||(response.toUpperCase().equals("NO"))){
            Sender sender = GameData.getSender();
            sender.responseLandRequest(response);

        }else
            System.out.println("You must choose between YES or NO");
    }

    /**
     * Handles player decision to land on a planet
     * @param planetsTaken is the array of available planets
     * @author Lorenzo
     */
    public static void responsePlanetLandRequest(boolean[] planetsTaken) {
        System.out.println("In witch planet do you want to land?");
        for (int i = 0; i < planetsTaken.length; i++){
            if (!planetsTaken[i]){
                System.out.println("Planet " + i);
            }
        }

        System.out.print("Choose a planet: ");
        String idx = listenResponse();

        try {
            int planet_idx = Integer.parseInt(idx);
            if ((planet_idx >= 0) && (planet_idx < planetsTaken.length)){
                if(!planetsTaken[planet_idx]) {
                    Sender sender = GameData.getSender();
                    sender.responsePlanetLandRequest("YES", planet_idx);
                } else
                    System.out.println("Planet already taken");
            } else
                System.out.println("Invalid planet index");

        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }


    // non sono sicuro che mandare un array di box si possa fare
    // todo void responseRewardBox(int idxBox, int xBoxStorage, int yBoxStorage, int idx);


    /**
     * Handles player decision to use a double cannon to protect in meteors rain
     *
     * @author Lorenzo
     */
    public static void responseUseDoubleCannonRequest() { //todo chiedi a seguito di che messaggio richiamare
        System.out.println("Do you want to use a double cannon?");

        String response = listenResponse();
        if((response.toUpperCase().equals("YES")) || (response.toUpperCase().equals("NO"))){
            Sender sender = GameData.getSender();
            sender.responseUseDoubleCannonRequest(response);

        }else
            System.out.println("You must choose between YES or NO");
    }


    /**
     * Enables to roll the dice
     * usage : Roll
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void rollDice(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.rollDice();
    }


}