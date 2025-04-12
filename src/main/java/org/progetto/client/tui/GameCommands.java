package org.progetto.client.tui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.BoxStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

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
    // PRINTING
    // =======================

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";
    private static final String ORANGE = "\u001B[38;5;208m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String WHITE = "\u001B[37m";
    private static final String BLUE = "\u001B[34m";

    public static void printSpaceship(Spaceship spaceship) {

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getCopySpaceshipMatrix();
        int rows = spaceshipMatrix.length;
        int cols = spaceshipMatrix[0].length;

        int[][] mask = spaceship.getBuildingBoard().getBoardMask();

//        System.out.println("   ┌──────────────────────────────┐");
//
//        for (int y = 0; y < spaceshipMatrix.length; y++) {
//            System.out.print("   │ ");
//            for (int x = 0; x < spaceshipMatrix[y].length; x++) {
//                Component c = spaceshipMatrix[y][x];
//                String symbol = (c != null) ? abbreviateType(c.getType()) : " . ";
//                System.out.print(symbol + " ");
//            }
//            System.out.println("│");
//        }
//
//        System.out.println("   └──────────────────────────────┘");

        System.out.println("\n🛠  Spaceship Matrix View");

        String[][][] gridVisual = new String[rows][cols][5];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // Checks current cell could be occupied
                if (mask[i][j] != 0) {
                    gridVisual[i][j] = printComponent(spaceshipMatrix[i][j]);

                } else {
                    gridVisual[i][j][0] = "             ";
                    gridVisual[i][j][1] = "             ";
                    gridVisual[i][j][2] = "             ";
                    gridVisual[i][j][3] = "             ";
                    gridVisual[i][j][4] = "             ";
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int line = 0; line < 5; line++) {
                for (int j = 0; j < cols; j++) {
                    System.out.print(gridVisual[i][j][line] + " ");
                }
                System.out.println();
            }
        }

        System.out.println();

        System.out.println("\n🔧 Ship Attributes");
        System.out.printf("│ Level                : %d%n", spaceship.getLevelShip());
        System.out.printf("│ Destroyed components : %d%n", spaceship.getDestroyedCount());
        System.out.printf("│ Crew members         : %d%n", spaceship.getCrewCount());
        System.out.printf("│ Batteries            : %d%n", spaceship.getBatteriesCount());
        System.out.printf("│ Exposed connectors   : %d%n", spaceship.getExposedConnectorsCount());
        System.out.printf("│ Purple alien         : %b%n", spaceship.getAlienPurple());
        System.out.printf("│ Orange alien         : %b%n", spaceship.getAlienOrange());

        System.out.println("\n🔫 Cannons");
        System.out.printf("│ Normal power         : %.1f%n", spaceship.getNormalShootingPower());
        System.out.printf("│ Half double cannons  : %d%n", spaceship.getHalfDoubleCannonCount());
        System.out.printf("│ Full double cannons  : %d%n", spaceship.getFullDoubleCannonCount());

        System.out.println("\n🚀 Engines");
        System.out.printf("│ Normal engine power  : %d%n", spaceship.getNormalEnginePower());
        System.out.printf("│ Double engines       : %d%n", spaceship.getDoubleEngineCount());

        System.out.println("\n🛡  Shields (up, right, down, left)");
        System.out.printf("│ Shields              : [%d, %d, %d, %d]%n", spaceship.getIdxShieldCount(0), spaceship.getIdxShieldCount(1), spaceship.getIdxShieldCount(2), spaceship.getIdxShieldCount(3));

        System.out.println("\n📦 Storage (red, yellow, green, blue)");
        int[] boxes = spaceship.getBoxCounts();
        System.out.printf("│ Boxes                : [%d, %d, %d, %d]%n", boxes[0], boxes[1], boxes[2], boxes[3]);

        System.out.println();
    }

    private static String abbreviateType(ComponentType type) {
        return switch (type) {
            case CANNON, DOUBLE_CANNON -> "C";
            case ENGINE, DOUBLE_ENGINE -> "E";
            case SHIELD -> "S";
            case HOUSING_UNIT, CENTRAL_UNIT -> "H";
            case ORANGE_HOUSING_UNIT -> ORANGE + "M" + RESET;
            case PURPLE_HOUSING_UNIT -> PURPLE + "M" + RESET;
            case STRUCTURAL_UNIT -> "#";
            case BATTERY_STORAGE -> "B";
            case RED_BOX_STORAGE -> RED + "X" + RESET;
            case BOX_STORAGE -> WHITE + "X" + RESET;
            default -> "?";
        };
    }

    private static String[] printComponent(Component component) {

        String[] lines = new String[5];

        if (component != null) {
            ComponentType type = component.getType();

            String abbreviateType = abbreviateType(type);

            // Temp variables
            int rotation = component.getRotation();
            int[] connections = component.getConnections();
            String up;
            String right;
            String down;
            String left;
            int capacity;
            int itemsCount;
            int[] boxes;
            String[] boxesStr;

            switch (type) {
                case CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] ="│     " + up + "     │";
                    lines[2] = connections[3] + "  " + left + "  " + abbreviateType + "  " + right + "  " + connections[1];
                    lines[3] = "│     " + down + "     │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case DOUBLE_CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│  " + left + " " + up + " " + up + " " + right + "  │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│  " + left + " " + down + " " + down + " " + right + "  │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│     " + up + "     │";
                    lines[2] = connections[3] + "  " + left + "  " + abbreviateType + "  " + right + "  " + connections[1];
                    lines[3] = "│     " + down + "     │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case DOUBLE_ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│  " + left + " " + up + " " + up + " " + right + "  │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│  " + left + " " + down + " " + down + " " + right + "  │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case SHIELD:
                    switch (rotation) {
                        case 0:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│ ════════╗ │";
                            lines[2] = connections[3] + "     " + abbreviateType + "   ║ " + connections[1];
                            lines[3] = "│         ║ │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;

                        case 1:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│         ║ │";
                            lines[2] = connections[3] + "     " + abbreviateType + "   ║ " + connections[1];
                            lines[3] = "│ ════════╝ │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;

                        case 2:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│ ║         │";
                            lines[2] = connections[3] + " ║   " + abbreviateType + "     " + connections[1];
                            lines[3] = "│ ╚════════ │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;

                        case 3:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│ ╔════════ │";
                            lines[2] = connections[3] + " ║   " + abbreviateType + "     " + connections[1];
                            lines[3] = "│ ║         │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;
                    }

                    break;

                case HOUSING_UNIT, CENTRAL_UNIT:
                    // TODO: print "H" of the same color of the player, need to get player's color
                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│           │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│           │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case ORANGE_HOUSING_UNIT:
                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│           │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│           │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case PURPLE_HOUSING_UNIT:
                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│           │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│           │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case STRUCTURAL_UNIT:
                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│           │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│           │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case BATTERY_STORAGE:
                    BatteryStorage batteryStorage = (BatteryStorage) component;
                    itemsCount = batteryStorage.getItemsCount();

                    lines[0] = "┌─────" + connections[0] + "─────┐";
                    lines[1] = "│           │";
                    lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                    lines[3] = "│    (" + itemsCount + ")    │";
                    lines[4] = "└─────" + connections[2] + "─────┘";
                    break;

                case RED_BOX_STORAGE:
                    BoxStorage redBoxStorage = (BoxStorage) component;
                    capacity = redBoxStorage.getCapacity();
                    boxes = redBoxStorage.getBoxStorageValues();
                    boxesStr = new String[boxes.length];

                    for (int i = 0; i < boxes.length; i++) {
                        switch (boxes[i]) {
                            case 0:
                                boxesStr[i] = " ";
                                break;

                            case 1:
                                boxesStr[i] = BLUE + "■" + RESET;
                                break;

                            case 2:
                                boxesStr[i] = GREEN + "■" + RESET;
                                break;

                            case 3:
                                boxesStr[i] = YELLOW + "■" + RESET;
                                break;

                            case 4:
                                boxesStr[i] = RED + "■" + RESET;
                                break;
                        }
                    }

                    switch (capacity) {
                        case 1:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│           │";
                            lines[2] = connections[3] + "     " + abbreviateType + "     " + connections[1];
                            lines[3] = "│    (" + boxesStr[0] + ")    │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;

                        case 2:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│           │";
                            lines[2] = connections[3] + " (" + boxesStr[0] + ") " + abbreviateType + " (" + boxesStr[1] + ") " + connections[1];
                            lines[3] = "│           │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;
                    }

                    break;

                case BOX_STORAGE:
                    BoxStorage boxStorage = (BoxStorage) component;
                    capacity = boxStorage.getCapacity();
                    boxes = boxStorage.getBoxStorageValues();
                    boxesStr = new String[boxes.length];

                    for (int i = 0; i < boxes.length; i++) {
                        switch (boxes[i]) {
                            case 0:
                                boxesStr[i] = " ";
                                break;

                            case 1:
                                boxesStr[i] = BLUE + "■" + RESET;
                                break;

                            case 2:
                                boxesStr[i] = GREEN + "■" + RESET;
                                break;

                            case 3:
                                boxesStr[i] = YELLOW + "■" + RESET;
                                break;
                        }
                    }

                    switch (capacity) {
                        case 2:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│           │";
                            lines[2] = connections[3] + " (" + boxesStr[0] + ") " + abbreviateType + " (" + boxesStr[1] + ") " + connections[1];
                            lines[3] = "│           │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;

                        case 3:
                            lines[0] = "┌─────" + connections[0] + "─────┐";
                            lines[1] = "│           │";
                            lines[2] = connections[3] + " (" + boxesStr[0] + ") " + abbreviateType + " (" + boxesStr[1] + ") " + connections[1];
                            lines[3] = "│    (" + boxesStr[2] + ")    │";
                            lines[4] = "└─────" + connections[2] + "─────┘";
                            break;
                    }

                    break;
            }

        } else {
            lines[0] = "┌───────────┐";
            lines[1] = "│           │";
            lines[2] = "│           │";
            lines[3] = "│           │";
            lines[4] = "└───────────┘";
        }

        return lines;
    }

    // =======================
    // COMMANDS
    // =======================

    /**
     * Allows a player to show a spaceship, usage: ShowSpaceship player_name
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
     * Help command, read a list of commands and display their usage
     *
     * @author Lorenzo
     */
    public static void printHelp() {
        String path = "src/main/resources/org/progetto/client/commands/commandsList.json";
        Gson gson = new Gson();
        try (Reader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<CommandEntity>>() {}.getType();
            List<CommandEntity> commands = gson.fromJson(reader, listType);

            System.out.println("\n📖 Available Commands:\n");
            for (CommandEntity cmd : commands) {
                System.out.printf("%-20s : %s%n", cmd.getName(), cmd.getDescription());
                System.out.printf("Usage                : %s%n%n", cmd.getUsage());
            }
        } catch (IOException e) {
            System.out.println("Error loading command list: " + e.getMessage());
        }
    }
}