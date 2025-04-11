package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.events.*;

import java.util.ArrayList;

/**
 * Contains commands relating to the building phase
 */
public class BuildingCommands {

    // =======================
    // ATTRIBUTES
    // =======================



    // =======================
    // PRINTING
    // =======================

    public static void printComponentInfo(Component component) {

        System.out.println("New component picked");

        String[] directions = {"↑", "→", "↓", "←"};
        int[] conn = component.getConnections();
        ComponentType type = component.getType();

        System.out.println("┌────────────────────────────┐");
        System.out.printf ("│ Type: %-20s │%n", type.name());
        System.out.println("├────────────────────────────┤");
        System.out.printf ("│ Position: (%2d, %2d)         │%n", component.getX(), component.getY());

        if((type == ComponentType.RED_BOX_STORAGE) || (type == ComponentType.BOX_STORAGE)) {
            BoxStorage storage = (BoxStorage) component;
            System.out.printf("│ Capacity : %-19d │%n", storage.getCapacity());
        }
        System.out.println("├───── Connectors ───────────┤");

        for (int i = 0; i < 4; i++) {
            String value = conn[i] == 0 ? "none" : String.valueOf(conn[i]);
            System.out.printf("│ %s : %-21s │%n", directions[i], value);
        }

        System.out.println("└────────────────────────────┘");
    }

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
    // COMMANDS
    // =======================

    /**
     * Enables to pick a hidden component if possible, usage : PickHidden
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickHiddenComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.pickHiddenComponent();
    }

    /**
     * Enables to pick a visible component given its index, usage : PickVisible componentIdx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickVisibleComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.pickVisibleComponent(Integer.parseInt(commandParts[1]));
    }

    /**
     * Enables to place the hand component given its coordinates and rotation, usage : PlaceLast pos_x pos_y rot
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeLastComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeLastComponent(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3])
        );
    }

    /**
     * Enables to place the hand component given its coordinates and rotation and pick a hidden component,
     * usage : PlaceAndPickHidden pos_x pos_y rot
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeHandComponentAndPickHiddenComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeHandComponentAndPickHiddenComponent(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3])
        );
    }

    /**
     * Enables to place the hand component given its coordinates and rotation and pick a visible component given its index,
     * usage : PlaceAndPickVisible pos_x pos_y rot component_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeHandComponentAndPickVisibleComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeHandComponentAndPickVisibleComponent(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3]),
                Integer.parseInt(commandParts[4])
        );
    }

    /**
     * Enables to place the hand component given its coordinates and rotation and pick an event deck given its index,
     * usage : PlaceAndPickEvent pos_x pos_y rot deck_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeHandComponentAndPickUpEventCardDeck(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeHandComponentAndPickUpEventCardDeck(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3]),
                Integer.parseInt(commandParts[4])
        );
    }

    /**
     * Enables to place the hand component given its coordinates and rotation and pick a booked component given its index,
     * usage : PlaceAndPickBooked pos_x pos_y rot component_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeHandComponentAndPickBookedComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeHandComponentAndPickBookedComponent(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3]),
                Integer.parseInt(commandParts[4])
        );
    }

    /**
     * Enables to place the hand component given its coordinates and rotation and set as ready,
     * usage : PlaceAndReady pos_x pos_y rot
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeHandComponentAndReady(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeHandComponentAndReady(
                Integer.parseInt(commandParts[1]),
                Integer.parseInt(commandParts[2]),
                Integer.parseInt(commandParts[3])
        );
    }


    /**
     * Enables to discard the hand component,
     * usage : Discard
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void discardComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.discardComponent();
    }

    /**
     * Enables to book the hand component,
     * usage : Book component_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void bookComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.bookComponent(Integer.parseInt(commandParts[1]));
    }

    /**
     * Enables to pick a booked component,
     * usage : PickBook component_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickBookedComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.pickBookedComponent(Integer.parseInt(commandParts[1]));
    }

    /**
     * Enables to pick up an event card deck,
     * usage : PickDeck deck_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickUpEventCardDeck(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.pickUpEventCardDeck(Integer.parseInt(commandParts[1]));
    }

    /**
     * Enables to put down an event card deck,
     * usage : PutDownDeck deck_idx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void putDownEventCardDeck(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.putDownEventCardDeck();
    }

    /**
     * Enables to destroy a component given its coordinates
     * usage : Destroy pos_x pos_y
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void destroyComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.destroyComponent(Integer.parseInt(commandParts[2]), Integer.parseInt(commandParts[1]));
    }

    /**
     * Enables to set a player as ready
     * usage : Ready
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void readyPlayer(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.readyPlayer();
    }

    /**
     * Enables to reset the timer
     * usage : TimerReset
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void resetTimer(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.resetTimer();
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

    /**
     * Enables to close the connection with the server
     * usage : Close
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void close(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.close();
    }
}