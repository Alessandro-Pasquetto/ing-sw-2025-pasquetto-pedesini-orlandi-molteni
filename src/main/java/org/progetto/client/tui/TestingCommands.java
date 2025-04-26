package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;

/**
 * this class contains all the methods needed to test the TUI
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
        int idShip = Integer.parseInt(commandParts[1]);
        sender.buildShip(idShip);
    }
}