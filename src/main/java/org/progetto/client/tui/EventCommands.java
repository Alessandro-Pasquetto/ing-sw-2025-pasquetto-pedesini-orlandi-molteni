package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.events.*;

import java.util.ArrayList;
import java.util.Scanner;

public class EventCommands {

    // =======================
    // PRINTING
    // =======================

    public static void printEventCard(EventCard card) {
        System.out.println("┌────────────────────────────────────────────┐");
        System.out.printf ("│ Type       : %-30s │%n", card.getType(),  "|");
        System.out.printf ("│ Level      : %-30s │%n", card.getLevel(), "|");

        switch (card.getType()){

            case METEORSRAIN -> {
                MeteorsRain meteors = (MeteorsRain) card;
                System.out.println("┌── Meteor Rain ────────────────────────────┐");
                System.out.printf ("│ Mereors  : %-28s   │%n", meteors.getMeteors());
                System.out.printf ("│ Count      : %-28d │%n", meteors.getMeteors().size());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case SLAVERS -> {
                Slavers slavers = (Slavers) card;
                System.out.println("┌── Slavers ──────────────────────────────────┐");
                System.out.printf ("│ Crew penalty  : %-24d │%n", slavers.getPenaltyCrew());
                System.out.printf ("│ Penalty Days  : %-24d │%n", slavers.getPenaltyDays());
                System.out.printf ("│ Strength      : %-24d │%n", slavers.getFirePowerRequired());
                System.out.printf ("│ Credits reward: %-24d │%n", slavers.getRewardCredits());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case SMUGGLERS -> {
                Smugglers smugglers = (Smugglers) card;
                System.out.println("┌── Smugglers ────────────────────────────────┐");
                System.out.printf ("│ Rewards      : %-24s │%n", smugglers.getRewardBoxes());
                System.out.printf ("│ Penalty Days : %-24d │%n", smugglers.getPenaltyDays());
                System.out.printf ("│ Strength     : %-24d │%n", smugglers.getFirePowerRequired());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case LOSTSTATION -> {
                LostStation station = (LostStation) card;
                System.out.println("┌── Lost Station ─────────────────────────────┐");
                System.out.printf ("│ Rewards      : %-24s │%n", station.getRewardBoxes());
                System.out.printf ("│ Penalty Days : %-24d │%n", station.getPenaltyDays());
                System.out.printf ("│ Required crew: %-24d │%n", station.getRequiredCrew());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case BATTLEZONE -> {
                Battlezone battlezone = (Battlezone) card;
                ArrayList<ConditionPenalty> couples = battlezone.getCouples();
                System.out.println("┌── Battlezone ───────────────────────────────────┐");
                for (ConditionPenalty couple : couples) {
                    System.out.printf ("│ Condition : %-29s │%n", couple.getCondition());
                    System.out.printf ("│ Penalty   : %-10s (x%-5d)         │%n",
                            couple.getPenalty().getType(),
                            couple.getPenalty().getNeededAmount());
                    System.out.println("├─────────────────────────────────────────────┤");
                }
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case PIRATES -> {
                Pirates pirates = (Pirates) card;
                System.out.println("┌── Pirates ──────────────────────────────────┐");
                System.out.printf ("│ Strength   : %-26d │%n", pirates.getFirePowerRequired());
                System.out.printf ("│ Penalty Days: %-26d│%n",pirates.getPenaltyDays());
                System.out.printf ("│ Shots  : %-26s     │%n", pirates.getPenaltyShots());
                System.out.printf ("│ Credits reward: %-26d │%n", pirates.getRewardCredits());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case PLANETS -> {
                Planets planets = (Planets) card;
                System.out.println("┌── Planets ──────────────────────────────────┐");
                System.out.printf ("│ Number of planets : %-20d │%n", planets.getPlanetsTaken().length);
                System.out.printf ("│ Rewards per planet: %-20s │%n", planets.getRewardsForPlanets());
                System.out.printf ("│ Penalty days: %-20d       │%n",planets.getPenaltyDays());
                System.out.println("└─────────────────────────────────────────────┘");
            }

            case LOSTSHIP -> {
                LostShip lostShip = (LostShip) card;
                System.out.println("┌── Lost Ship ────────────────────────────────┐");
                System.out.printf ("│ Penalty Crew     : %-22d │%n", lostShip.getPenaltyCrew());
                System.out.printf ("│ Penalty Days     : %-22d │%n", lostShip.getPenaltyDays());
                System.out.printf ("│ Reward Credits   : %-22d │%n", lostShip.getRewardCredits());
                System.out.println("└─────────────────────────────────────────────┘");
            }
        }

        System.out.println();
    }


    // =======================
    // SCANNER
    // =======================

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
        System.out.println("You have" + max + " double engines");

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
        System.out.println("Select the box storage from which to remove the box: <X> <Y> <storage_idx>");
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


    /**
     * Handles player decision to accept reward credits and penalty days
     *
     * @author Lorenzo
     * @param reward is the credit reward
     * @param penaltyDays are the days of penalty
     */
    public static void responseAcceptRewardCreditsAndPenaltyDays(int reward, int penaltyDays){
        System.out.println("You want to accept the reward and the days penalty?");
        System.out.println("Reward: " + reward + " Days of penalty: " + penaltyDays);

        String response = listenResponse();
        if((response.toUpperCase().equals("YES"))||(response.toUpperCase().equals("NO"))){
            Sender sender = GameData.getSender();
            sender.responseAcceptRewardCreditsAndPenaltyDays(response);

        }else
            System.out.println("You must choose between YES or NO");
    }

    /**
     * Let the player decide from a list of boxes witch one to keep
     *
     * @author Lorenzo
     * @param availableBoxes is the list of all available boxes
     */
    public static void responseRewardBox(ArrayList<Integer> availableBoxes) {
        System.out.println("📦 Box list:");

        for (int i = 0; i < availableBoxes.size(); i++) {
            String box = switch (availableBoxes.get(i)) {
                            case 1 -> "BLUE";
                            case 2 -> "GREEN";
                            case 3 -> "YELLOW";
                            case 4 -> "RED";
                            default -> "?";
                        };

            System.out.printf(" [%2d]  Color: %-10s%n", i, box);
        }

        System.out.println("Select an available box dy index from the list");
        String idx = listenResponse();
        try{
            int box_idx = Integer.parseInt(idx);
            if(box_idx >= 0 && box_idx < availableBoxes.size()){
                System.out.println("Select a box storage were you want to insert the box: <X> <Y> <storage_idx>");
                String x = listenResponse();
                String y = listenResponse();
                String storage_idx = listenResponse();

                Sender sender = GameData.getSender();
                sender.responseRewardBox(box_idx,Integer.parseInt(x),Integer.parseInt(y),Integer.parseInt(storage_idx));

            }else{
                System.out.println("Box index out of bounds!");
            }

        }catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }

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

    /**
     * Handles player decision to use a double cannon to protect in meteors rain
     *
     * @author Lorenzo
     */
    public static void responseUseDoubleCannonRequest() {
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