package org.progetto.client.tui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Contains commands relating to the game execution
 */
public class GameCommands {

    // =======================
    // COMMANDS
    // =======================

    /**
     * Allows a player to show a spaceship
     * usage: ShowSpaceship player_name
     *
     * @author Lorenzo
     * @param commandParts
     */
    public static void showSpaceship(String[] commandParts){
        Sender sender = GameData.getSender();

        if(commandParts.length == 1)
            sender.showSpaceship(GameData.getNamePlayer());
        else
            sender.showSpaceship(commandParts[1]);
    }

    /**
     * Allows a player to show his spaceship stats
     * usage: ShipStats
     *
     * @author Gabriele
     * @param commandParts
     */
    public static void spaceshipStats(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.spaceshipStats();
    }

    /**
     * Allows a player to show current track
     * usage: ShowTrack
     *
     * @author Gabriele
     * @param commandParts
     */
    public static void showTrack(String[] commandParts){
        Sender sender = GameData.getSender();
        sender.showTrack();
    }

    /**
     * Help command, read a list of commands and display their usage
     *
     * @author Lorenzo
     */
    public static void showHelp() {
        String path = "src/main/resources/org/progetto/client/commands/commandsList.json";
        Gson gson = new Gson();
        try (Reader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<Command>>() {}.getType();
            List<Command> commands = gson.fromJson(reader, listType);

            System.out.println("\n📖 Available Commands:\n");
            for (Command cmd : commands) {
                System.out.printf("%-20s : %s%n", cmd.getName(), cmd.getDescription());
                System.out.printf("Usage                : %s%n%n", cmd.getUsage());
            }
        } catch (IOException e) {
            System.out.println("Error loading command list: " + e.getMessage());
        }
    }
}