package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;

/**
 * This class contains all the methods needed to test the TUI
 */
public class TestingCommands {

    // =======================
    // COMMANDS
    // =======================

    /**
     * Enable the user to create a predefined spaceship
     *
     * @author Lorenzo
     * @param commandParts are segments of the command
     */
    public static void buildShip(String[] commandParts){
        Sender sender = GameData.getSender();

        try {
            sender.buildShip(Integer.parseInt(commandParts[1]));
        } catch (NumberFormatException e){
            System.out.println("You must insert a number!");
        }
    }
}