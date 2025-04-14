package org.progetto.client.tui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.model.GameData;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

public class TuiCommandFilter {

    // =======================
    // ATTRIBUTES
    // =======================

    public static boolean fixing = false;


    // =======================
    // GETTERS
    // =======================
    public static boolean getFixing(){
        return fixing;
    }

    // =======================
    // SETTERS
    // =======================
    public static void setFixing(boolean fixing) {
        TuiCommandFilter.fixing = fixing;
    }

    // =======================
    // OTHER METHODS
    // =======================

    private static Scanner scanner = new Scanner(System.in);

    public static void setProtocol(){
        System.out.println();
        System.out.println("Select Socket/Rmi:");

        while(true){
            String protocol = scanner.nextLine().toUpperCase();

            if (protocol.equals("SOCKET")) {
                GameData.setSender(new SocketClient());
                System.out.println("Socket selected");
                break;
            } else if (protocol.equals("RMI")) {
                GameData.setSender(new RmiClientSender());
                System.out.println("RMI selected");
                break;
            } else {
                System.out.println("Command not found");
            }
        }

        System.out.println();
    }

    public static void listenCommand() {

        while (true){

            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("exit")) break;

            handleCommand(command);

            System.out.println();
        }

        GameData.getSender().close();
    }

    private static boolean isValidCommand(int commandLength, int l){
        if (commandLength == l)
            return true;
        else {
            System.out.println("Invalid command format");
            return false;
        }
    }

    private static void expectedFormat(String command) {
        String path = "src/main/resources/org/progetto/client/commands/commandsList.json";
        Gson gson = new Gson();

        try (Reader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<CommandEntity>>() {}.getType();
            List<CommandEntity> commands = gson.fromJson(reader, listType);

            for (CommandEntity cmd : commands) {
                if (cmd.getName().equalsIgnoreCase(command)) {
                    System.out.println("Expected format: " + cmd.getUsage());
                    return;
                }
            }

            System.out.println("Command not found in command list.");

        } catch (IOException e) {
            System.out.println("Error loading command list: " + e.getMessage());
        }
    }

    public static void handleCommand(String command) {
        String[] commandParts = command.split(" ");
        String commandType = commandParts[0].toUpperCase();

        if(!fixing) {

            switch (commandType) {

                case "CONNECT":
                    if (!isValidCommand(commandParts.length, 3)) {
                        expectedFormat(commandType);
                        return;
                    }
                    ConnectionsCommands.connect(commandParts);
                    break;

                case "SHOWWAITINGGAMES":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    ConnectionsCommands.showWaitingGames();
                    break;

                case "CREATEGAME":
                    if (!isValidCommand(commandParts.length, 4)) {
                        expectedFormat(commandType);
                        return;
                    }
                    ConnectionsCommands.createGame(commandParts);
                    break;

                case "JOINGAME":
                    if (!isValidCommand(commandParts.length, 3)) {
                        expectedFormat(commandType);
                        return;
                    }
                    ConnectionsCommands.joinGame(commandParts);
                    break;

                case "READY":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.readyPlayer(commandParts);
                    break;

                case "TIMERRESET":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.resetTimer(commandParts);
                    break;

                case "SHOWHAND":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.showHandComponent(commandParts);
                    break;

                case "PICKHIDDEN":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.pickHiddenComponent(commandParts);
                    break;

                case "SHOWVISIBLE":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.showVisibleComponents(commandParts);
                    break;

                case "PICKVISIBLE":
                    if (!isValidCommand(commandParts.length, 2)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.pickVisibleComponent(commandParts);
                    break;

                case "PLACE":
                    if (!isValidCommand(commandParts.length, 4)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.placeComponent(commandParts);
                    break;

                case "DISCARD":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.discardComponent(commandParts);
                    break;

                case "BOOK":
                    if (!isValidCommand(commandParts.length, 2)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.bookComponent(commandParts);
                    break;

                case "SHOWBOOKED":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.showBookedComponents(commandParts);
                    break;

                case "PICKBOOKED":
                    if (!isValidCommand(commandParts.length, 2)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.pickBookedComponent(commandParts);
                    break;

                case "PICKDECK":
                    if (!isValidCommand(commandParts.length, 2)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.pickUpEventCardDeck(commandParts);
                    break;

                case "PUTDOWNDECK":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.putDownEventCardDeck(commandParts);
                    break;

                case "SHOWSHIP":
                    if (commandParts.length > 2) {
                        System.out.println("Invalid command format");
                        expectedFormat(commandType);
                        return;
                    }
                    GameCommands.showSpaceship(commandParts);
                    break;

                case "ROLL":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    EventCommands.rollDice(commandParts);
                    break;

                case "CLOSE":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.close(commandParts);
                    break;

                case "HELP":
                    GameCommands.showHelp();
                    break;

                default:
                    System.out.println("Command not found");
                    break;
            }

        }else if(fixing){
            switch (commandType) {

                case "DESTROY":
                    if (!isValidCommand(commandParts.length, 3)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.destroyComponent(commandParts);
                    break;

                case "READY":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.readyPlayer(commandParts);
                    break;

                case "SHOWSHIP":
                    if (commandParts.length > 2) {
                        System.out.println("Invalid command format");
                        expectedFormat(commandType);
                        return;
                    }
                    GameCommands.showSpaceship(commandParts);
                    break;

                case "CLOSE":
                    if (!isValidCommand(commandParts.length, 1)) {
                        expectedFormat(commandType);
                        return;
                    }
                    BuildingCommands.close(commandParts);
                    break;

                default:
                    System.out.println("Command not found");
                    break;

            }

        }
    }
}