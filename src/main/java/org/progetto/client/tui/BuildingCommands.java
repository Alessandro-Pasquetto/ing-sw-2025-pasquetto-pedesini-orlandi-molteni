package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
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
        sender.pickVisibleComponent(Integer.parseInt(commandParts[1]));
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

        int x = Integer.parseInt(commandParts[1]) - 6 + levelGame;
        int y = Integer.parseInt(commandParts[2]) - 5;
        int rot = Integer.parseInt(commandParts[3]);

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
        sender.bookComponent(Integer.parseInt(commandParts[1]));
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
        int levelGame = GameData.getLevelGame();

        int x = Integer.parseInt(commandParts[1]) - 6 + levelGame;
        int y = Integer.parseInt(commandParts[2]) - 5;

        sender.destroyComponent(x, y);
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
        System.out.println("Timer reset");
    }

    //todo comment
    public static void populateComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        int levelGame = GameData.getLevelGame();

        String crewType = commandParts[1];
        int x = Integer.parseInt(commandParts[2]) - 6 + levelGame;
        int y = Integer.parseInt(commandParts[3]) - 5;
        sender.populateComponent(crewType, x, y);
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