package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

/**
 * Contains commands relating to the building phase
 */
public class BuildingCommands {

    // =======================
    // COMMANDS
    // =======================

    /**
     * Enables player to view hand component
     * usage : ShowHand
     *
     * @author Gabriele
     * @param commandParts
     */
    public static void showHandComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.showHandComponent();
    }

    /**
     * Enables to pick a hidden component if possible
     * usage : PickHidden
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickHiddenComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.pickHiddenComponent();
    }

    /**
     * Enables player to view visible components
     * usage : ShowVisible
     *
     * @author Gabriele
     * @param commandParts
     */
    public static void showVisibleComponents(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.showVisibleComponents();
    }

    /**
     * Enables to pick a visible component given its index
     * usage : PickVisible componentIdx
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void pickVisibleComponent(String[] commandParts){
        Sender sender = GameData.getSender();

        try {
            sender.pickVisibleComponent(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
    }

    /**
     * Enables to place the hand component given its coordinates and rotation
     * usage : Place pos_x pos_y rot
     *
     * @author Gabriele
     * @param commandParts are segments of the command
     */
    public static void placeComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        int levelGame = GameData.getLevelGame();

        int x = 0;
        int y = 0;
        int rot = 0;

        try {
            x = Integer.parseInt(commandParts[1]) - 6 + levelGame;
            y = Integer.parseInt(commandParts[2]) - 5;
            rot = Integer.parseInt(commandParts[3]);
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }

        sender.placeComponent(x, y, rot);
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

        try {
            sender.bookComponent(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
    }

    /**
     * Enables player to view booked component
     * usage : ShowBooked
     *
     * @author Gabriele
     * @param commandParts
     */
    public static void showBookedComponents(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.showBookedComponents();
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

        try {
            sender.pickBookedComponent(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
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

        try {
            sender.pickUpEventCardDeck(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }
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
        int levelGame = GameData.getLevelGame();

        int x = 0;
        int y = 0;

        try {
            x = Integer.parseInt(commandParts[1]) - 6 + levelGame;
            y = Integer.parseInt(commandParts[2]) - 5;
        } catch (NumberFormatException e){
            System.err.println("You must insert a number!");
        }

        sender.destroyComponent(x, y);
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
        System.out.println("Timer reset");
    }

    /**
     * Handles player response to place an alien
     *
     * @author Alessandro
     * @param alienColor
     * @param spaceship
     */
    public static void responsePlaceAlien(String alienColor, Spaceship spaceship) {

        while(true) {
            System.out.println("Do you want to place " + alienColor + " alien? (YES or NO)");
            String response = TuiCommandFilter.waitResponse();

            if(response.equalsIgnoreCase("NO")){
                GameData.getSender().responsePlaceAlien(-1, -1, alienColor);
                break;
            }

            else if (response.equalsIgnoreCase("YES")){
                Sender sender = GameData.getSender();
                TuiPrinters.highlightComponent = alienColor;
                TuiPrinters.printSpaceship(GameData.getNamePlayer(), spaceship, GameData.getColor());
                TuiPrinters.highlightComponent = null;

                System.out.println("Choose where to place " + alienColor + " alien:");
                System.out.print("X: ");
                String x = TuiCommandFilter.waitResponse();

                System.out.print("Y: ");
                String y = TuiCommandFilter.waitResponse();

                int levelShip = spaceship.getLevelShip();

                try {
                    sender.responsePlaceAlien(Integer.parseInt(x) - 6 + levelShip, Integer.parseInt(y) - 5, alienColor);
                    break;
                } catch (NumberFormatException e) {
                    System.err.println("You must insert a number!");
                }
            }

            else
                System.out.println("You must choose between YES or NO");
        }
    }
}