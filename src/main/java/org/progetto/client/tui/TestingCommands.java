package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;


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
            System.err.println("You must insert a number!");
        }
    }
}