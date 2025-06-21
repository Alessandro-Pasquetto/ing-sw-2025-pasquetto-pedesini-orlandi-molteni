package org.progetto.client.tui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.progetto.client.MainClient;
import org.progetto.client.connection.rmi.RmiClientReceiver;
import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.connection.socket.SocketListener;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Game;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class TuiCommandFilter {

    // =======================
    // ATTRIBUTES
    // =======================

    private static final Scanner scanner = new Scanner(System.in);

    private static final LinkedBlockingQueue<String> tuiMessageQueue = new LinkedBlockingQueue<>();
    private final static Object responseLock = new Object();
    private static boolean isWaitingResponse = false;
    private static String response = "";

    private static final Map<String, Command> commands = loadCommands();

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

    /**
     * Let the player decide the communication protocol between RMI and Socket
     *
     * @author Alessandro
     */
    public static void setProtocol(){

        while(true){
            System.out.println();
            System.out.println("Select Socket/Rmi:");

            String protocol = scanner.nextLine().toUpperCase().trim();

            if (protocol.equals("SOCKET")) {
                GameData.setSender(new SocketClient());
                System.out.println("Socket selected");
                break;
            } else if (protocol.equals("RMI")) {
                GameData.setSender(new RmiClientSender());
                System.out.println("RMI selected");
                break;
            } else {
                System.err.println("Command not found");
            }
        }

        while(true){
            System.out.println();
            System.out.println("What ip do you want to connect to?");

            String ip = scanner.nextLine().trim();

            if (ip.isEmpty()) {
                System.err.println("Empty response!");
                continue;
            }

            System.out.println();
            System.out.println("What port do you want to connect to?");

            int serverPort;

            try{
                serverPort = Integer.parseInt(scanner.nextLine().trim());

            }catch (NumberFormatException e){
                System.err.println("Port must be a number!");
                continue;
            }

            if (serverPort < 0) {
                System.err.println("Port cannot be negative!");
                continue;
            }

            try {
                GameData.getSender().connect(ip, serverPort);
                System.out.println();

                if (!GameData.hasSavedGameData()) {
                    System.out.println("Type 'help' to see the available commands");
                } else {
                    GameData.restoreSavedGameData();
                    System.out.println("A saved game was found and the data has been restored");

                    GameData.getSender().reconnectToGame();
                }
                break;

            } catch (Exception e) {
                System.err.println("Error connecting to " + ip + ":" + serverPort);
            }
        }

        System.out.println();
    }

    /**
     * Handles command input
     *
     * @author Alessandro
     */
    public static void listenerCommand() {

        messageDispatcher();

        while (true){

            String command = scanner.nextLine().trim();
            System.out.println();

            if (command.isEmpty()) continue;
            if (command.equalsIgnoreCase("exit")) break;

            tuiMessageQueue.offer(command);
        }

        GameData.getSender().leaveGame();
    }

    /**
     * Takes the first command in the queue
     *
     * @autrhor Alessandro
     */
    public static void messageDispatcher() {
        new Thread(() -> {
            while (true) {
                try {
                    String command = tuiMessageQueue.take();

                    try {
                        handleCommand(command);
                    } catch (IllegalStateException e){
                        if (getIsWaitingResponse())
                            setResponse(command);
                        else
                            System.err.println(e.getMessage());
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Handles the player response
     *
     * @author Alessandro
     * @param input is the response
     */
    public static void setResponse(String input) {
        synchronized (responseLock) {
            response = input;
            isWaitingResponse = false;
            responseLock.notify();
        }
    }

    /**
     * Handles the phase of waiting for a player response
     *
     * @author Alessandro
     * @return the given response
     */
    public static String waitResponse() {
        synchronized (responseLock) {
            isWaitingResponse = true;
            SocketListener.setIsHandling(false);
            RmiClientReceiver.setIsHandling(false);

            try {
                while (isWaitingResponse) {
                    responseLock.wait();
                }
                SocketListener.setIsHandling(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return response.trim();
    }

    /**
     * Verify if an input command is valid
     *
     * @author Alessandro
     * @param commandLength is the number of strings that compose the command
     * @param l expected length value
     * @return true if the command is valid
     */
    private static boolean isValidCommand(int commandLength, int l){
        if (commandLength == l)
            return true;
        else {
            System.err.println("Invalid command format");
            return false;
        }
    }

    /**
     * Load all the command available from a .json file
     *
     * @author Lorenzo
     * @return a map of String with command name with its object reference
     */
    public static Map<String, Command> loadCommands() {
        Map<String, Command> commands = new HashMap<>();
        Gson gson = new Gson();

        try (InputStream inputStream = MainClient.class.getResourceAsStream("commands/CommandsList.json");
             Reader reader = new InputStreamReader(inputStream)) {

            Type listType = new TypeToken<List<Command>>() {}.getType();
            List<Command> commandList = gson.fromJson(reader, listType);

            for (Command cmd : commandList) {
                if (cmd.getPhases() == null) {
                    cmd = new Command(
                            cmd.getName(),
                            cmd.getDescription(),
                            cmd.getUsage(),
                            new String[0]
                    );
                }
                commands.put(cmd.getName().toLowerCase(), cmd);
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("Error loading command list: " + e.getMessage());
        }

        return commands;
    }

    /**
     * Print the expected format of a command
     *
     * @author Gabriele
     * @param command is the command to print
     */
    private static void expectedFormat(String command) {
        Command cmd = commands.get(command.toLowerCase());

        if (cmd != null) {
            System.err.println("Expected format: " + cmd.getUsage());
        } else {
            System.err.println("Command not found");
        }
    }

    /**
     * Handles all the commands in input subdivided by phases
     *
     * @author Alessandro
     * @param command is the command name red in input
     * @throws IllegalStateException  if a command invoke a method in a wrong time
     * @throws InterruptedException if the connection will be lost
     */
    public static void handleCommand(String command) throws IllegalStateException, InterruptedException {
        command = command.trim();
        String[] commandParts = command.split("\\s+");
        String commandType = commandParts[0].toUpperCase();

        switch (commandType) {
            case "CLOSE":
                if (isValidCommand(commandParts.length, 1))
                    GameCommands.close(commandParts);
                else
                    expectedFormat(commandType);
                return;

            case "HELP":
                GameCommands.showHelp();
                return;
        }

        switch (GameData.getPhaseGame()) {
            case "LOBBY":
                switch (commandType) {

                    case "SHOWGAMES":
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

                    case "AUTOCREATE":
                        if (isValidCommand(commandParts.length, 1))
                            ConnectionsCommands.autoCreate(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "AUTOJOIN":
                        if (isValidCommand(commandParts.length, 2))
                            ConnectionsCommands.autoJoin(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "WAITING":
                switch (commandType) {
                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }

            case "INIT":
                switch (commandType) {
                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
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

                    case "BUILDSHIP":
                        if (isValidCommand(commandParts.length, 2))
                            TestingCommands.buildShip(commandParts);
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
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "ADJUSTING":
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
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "POPULATING":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "READY":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.readyPlayer(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "POSITIONING":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "EVENT":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "MOVEBOX":
                        if (isValidCommand(commandParts.length, 7))
                            EventCommands.moveBox(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "REMOVEBOX":
                        if (isValidCommand(commandParts.length, 4))
                            EventCommands.removeBox(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;

            case "TRAVEL":
                switch (commandType) {
                    case "SHOWSHIP":
                        if (commandParts.length <= 2)
                            GameCommands.showSpaceship(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "SHIPSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.spaceshipStats(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    case "PLAYERSTATS":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.playerStats(commandParts);
                        else
                            expectedFormat(commandType);
                        break;

                    case "SHOWTRACK":
                        if (isValidCommand(commandParts.length, 1))
                            GameCommands.showTrack(commandParts);
                        else {
                            expectedFormat(commandType);
                        }
                        break;

                    default:
                        if (commands.get(commandType.toLowerCase()) == null)
                            throw new IllegalStateException("Command not found");
                        else
                            throw new IllegalStateException("Command not available in that phase");
                }
                break;
        }
    }
}