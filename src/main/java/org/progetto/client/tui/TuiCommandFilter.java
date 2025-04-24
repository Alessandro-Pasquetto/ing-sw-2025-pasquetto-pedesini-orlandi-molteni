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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TuiCommandFilter {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final Scanner scanner = new Scanner(System.in);

    private final static Object responseLock = new Object();
    private static boolean isWaitingResponse = false;
    private static String response = "";

    private static Map<String, Command> commands = loadCommands();

    // =======================
    // GETTERS
    // =======================

    public static Object getResponseLock() {
        return responseLock;
    }

    public static boolean getIsWaitingResponse() {
        return isWaitingResponse;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setIsWaitingResponse(boolean isWaitingResponse) {
        TuiCommandFilter.isWaitingResponse = isWaitingResponse;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static String waitResponse() {
        isWaitingResponse = true;
        synchronized (responseLock) {
            try {
                while (isWaitingResponse) {
                    responseLock.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return response;
    }

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

            if (command.isEmpty()) continue;

            if (command.equalsIgnoreCase("exit")) break;

            if (!isWaitingResponse) {
                handleCommand(command);

            } else {

                if (!handleCommand(command)) {
                    response = command;
                    isWaitingResponse = false;

                    synchronized(responseLock) {
                        responseLock.notify();
                    }

                }
            }

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

    private static Map<String, Command> loadCommands() {
        String path = "src/main/resources/org/progetto/client/commands/commandsList.json";
        Gson gson = new Gson();

        Map<String, Command> commands = new HashMap<>();

        try (Reader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<Command>>() {}.getType();
            List<Command> commandList = gson.fromJson(reader, listType);

            for (Command cmd : commandList) {
                commands.put(cmd.getName().toLowerCase(), cmd);
            }

        } catch (IOException e) {
            System.out.println("Error loading command list: " + e.getMessage());
        }

        return commands;
    }

    private static void expectedFormat(String command) {
        Command cmd = commands.get(command.toLowerCase());

        if (cmd != null) {
            System.out.println("Expected format: " + cmd.getUsage());
        } else {
            System.out.println("Command not found");
        }
    }

    public static boolean handleCommand(String command) {
        String[] commandParts = command.split(" ");
        String commandType = commandParts[0].toUpperCase();

        boolean foundCommand = true;

        switch (commandType) {
            case "CLOSE":
                if (isValidCommand(commandParts.length, 1))
                    BuildingCommands.close(commandParts);
                else
                    expectedFormat(commandType);
                break;

            case "HELP":
                GameCommands.showHelp();
                break;
        }

        switch (GameData.getPhaseGame()) {
            case "LOBBY":
                switch (commandType) {

                    case "CONNECT":
                        if (isValidCommand(commandParts.length, 3))
                            ConnectionsCommands.connect(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWWAITINGGAMES":
                        if (isValidCommand(commandParts.length, 1))
                            ConnectionsCommands.showWaitingGames();
                        else
                            expectedFormat(commandType);
                        break;

                    case "CREATEGAME":
                        if (isValidCommand(commandParts.length, 4))
                            ConnectionsCommands.createGame(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "JOINGAME":
                        if (isValidCommand(commandParts.length, 3))
                            ConnectionsCommands.joinGame(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;

            case "BUILDING":
                switch (commandType) {
                    case "TIMERRESET":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.resetTimer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWHAND":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.showHandComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PICKHIDDEN":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.pickHiddenComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWVISIBLE":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.showVisibleComponents(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PICKVISIBLE":
                        if (isValidCommand(commandParts.length, 2))
                            BuildingCommands.pickVisibleComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PLACE":
                        if (isValidCommand(commandParts.length, 4))
                            BuildingCommands.placeComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "DISCARD":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.discardComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "BOOK":
                        if (isValidCommand(commandParts.length, 2))
                            BuildingCommands.bookComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWBOOKED":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.showBookedComponents(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PICKBOOKED":
                        if (isValidCommand(commandParts.length, 2))
                            BuildingCommands.pickBookedComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PICKUPDECK":
                        if (isValidCommand(commandParts.length, 2))
                            BuildingCommands.pickUpEventCardDeck(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "PUTDOWNDECK":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.putDownEventCardDeck(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;

            case "START_ADJUSTING", "ADJUSTING":
                switch (commandType) {
                    case "DESTROY":
                        if (isValidCommand(commandParts.length, 3))
                            BuildingCommands.destroyComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;

            case "POPULATING":
                switch (commandType) {
                    case "POPULATE":
                        if (isValidCommand(commandParts.length, 4))
                            BuildingCommands.populateComponent(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            BuildingCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;

            case "EVENT":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;

            case "TRAVEL":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            System.out.println("Invalid command format");
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null) {
                            System.out.println("Command not found");
                        } else {
                            System.out.println("Command not available in that phase");
                        }
                        foundCommand = false;
                        break;
                }
                break;
        }

        return foundCommand;
    }
}