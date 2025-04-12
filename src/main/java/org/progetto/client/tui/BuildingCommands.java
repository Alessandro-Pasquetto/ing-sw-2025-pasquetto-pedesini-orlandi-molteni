package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

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
     * Enables to place the hand component given its coordinates and rotation, usage : Place pos_x pos_y rot
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void placeComponent(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.placeLastComponent(
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