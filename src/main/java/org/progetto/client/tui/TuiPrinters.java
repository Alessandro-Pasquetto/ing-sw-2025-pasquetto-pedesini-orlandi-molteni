package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.messages.toClient.EventCommon.IncomingProjectileMessage;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.util.ArrayList;

public class TuiPrinters {

    // =======================
    // ATTRIBUTES
    // =======================

    public static String highlightComponent;

    // =======================
    // COLORS
    // =======================

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";
    private static final String ORANGE = "\u001B[38;5;208m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String WHITE = "\u001B[37m";
    private static final String BLUE = "\u001B[34m";

    // =======================
    // PLAYER
    // =======================

    public static void printPlayerStats(String name, int credits, int position, boolean hasLeft) {

        System.out.println("\uD83E\uDDCD Your Stats:");
        System.out.println();

        System.out.printf("│ Name     : %s%n", name);
        System.out.printf("│ Credits  : %d%n", credits);
        System.out.printf("│ Position : %d%n", position);
        System.out.printf("│ Has left : %s%n", hasLeft ? "YES" : "NO");
    }

    // =======================
    // COMPONENTS
    // =======================

    public static String drawBox(Box box) {

        if(box == null)
            return " ";

        return switch (box.getValue()) {
            case 1 -> BLUE + "■" + RESET;
            case 2 -> GREEN + "■" + RESET;
            case 3 -> YELLOW + "■" + RESET;
            case 4 -> RED + "■" + RESET;
            default -> "";
        };
    }

    public static String abbreviateComponentType(ComponentType type) {
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

    public static String[] drawComponent(Component component) {

        String[] lines = new String[5];

        if (component != null) {
            ComponentType type = component.getType();

            String abbreviateType = abbreviateComponentType(type);

            // Rotation and Connections
            int rotation = component.getRotation();
            int[] connections = component.getConnections();

            String[] upConnections = new String[3];
            String[] rightConnections = new String[3];
            String[] downConnections = new String[3];
            String[] leftConnections = new String[3];

            for (int i = 0; i < connections.length; i++) {

                switch (i) {
                    case 0:
                        if (connections[i] == 0) {
                            upConnections[0] = "─";
                            upConnections[1] = "─";
                            upConnections[2] = "─";
                        } else if (connections[i] == 1) {
                            upConnections[0] = "─";
                            upConnections[1] = "┴";
                            upConnections[2] = "─";
                        } else if (connections[i] == 2) {
                            upConnections[0] = "┴";
                            upConnections[1] = "─";
                            upConnections[2] = "┴";
                        } else if (connections[i] == 3) {
                            upConnections[0] = "┴";
                            upConnections[1] = "┴";
                            upConnections[2] = "┴";
                        }
                        break;

                    case 1:
                        if (connections[i] == 0) {
                            rightConnections[0] = "│ ";
                            rightConnections[1] = "│ ";
                            rightConnections[2] = "│ ";
                        } else if (connections[i] == 1) {
                            rightConnections[0] = "│ ";
                            rightConnections[1] = "├─";
                            rightConnections[2] = "│ ";
                        } else if (connections[i] == 2) {
                            rightConnections[0] = "├─";
                            rightConnections[1] = "│ ";
                            rightConnections[2] = "├─";
                        } else if (connections[i] == 3) {
                            rightConnections[0] = "├─";
                            rightConnections[1] = "├─";
                            rightConnections[2] = "├─";
                        }
                        break;

                    case 2:
                        if (connections[i] == 0) {
                            downConnections[0] = "─";
                            downConnections[1] = "─";
                            downConnections[2] = "─";
                        } else if (connections[i] == 1) {
                            downConnections[0] = "─";
                            downConnections[1] = "┬";
                            downConnections[2] = "─";
                        } else if (connections[i] == 2) {
                            downConnections[0] = "┬";
                            downConnections[1] = "─";
                            downConnections[2] = "┬";
                        } else if (connections[i] == 3) {
                            downConnections[0] = "┬";
                            downConnections[1] = "┬";
                            downConnections[2] = "┬";
                        }
                        break;

                    case 3:
                        if (connections[i] == 0) {
                            leftConnections[0] = " │";
                            leftConnections[1] = " │";
                            leftConnections[2] = " │";
                        } else if (connections[i] == 1) {
                            leftConnections[0] = " │";
                            leftConnections[1] = "─┤";
                            leftConnections[2] = " │";
                        } else if (connections[i] == 2) {
                            leftConnections[0] = "─┤";
                            leftConnections[1] = " │";
                            leftConnections[2] = "─┤";
                        } else if (connections[i] == 3) {
                            leftConnections[0] = "─┤";
                            leftConnections[1] = "─┤";
                            leftConnections[2] = "─┤";
                        }
                        break;
                }
            }

            // Engines/Cannons directions
            String up;
            String right;
            String down;
            String left;

            // Storages
            int capacity;
            int itemsCount;
            Box[] boxes;
            String[] boxesStr;

            // Colors
            String color;
            String stopColor;

            switch (type) {
                case CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "     " + up + "     " + rightConnections[0];
                    lines[2] = leftConnections[1] + "  " + left + "  " + abbreviateType + "  " + right + "  " + rightConnections[1];
                    lines[3] = leftConnections[2] + "     " + down + "     " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case DOUBLE_CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "  " + left + " " + up + " " + up + " " + right + "  " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "  " + left + " " + down + " " + down + " " + right + "  " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "     " + up + "     " + rightConnections[0];
                    lines[2] = leftConnections[1] + "  " + left + "  " + abbreviateType + "  " + right + "  " + rightConnections[1];
                    lines[3] = leftConnections[2] + "     " + down + "     " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case DOUBLE_ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "  " + left + " " + up + " " + up + " " + right + "  " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "  " + left + " " + down + " " + down + " " + right + "  " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case SHIELD:
                    color = !component.getIncorrectlyPlaced() ? GREEN : "";
                    stopColor = !component.getIncorrectlyPlaced() ? RESET : "";

                    switch (rotation) {
                        case 0:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + " " + color + "═══════╗" + stopColor + "  " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + color + "  ║  " + stopColor + rightConnections[1];
                            lines[3] = leftConnections[2] + "        " + color + "║ " + stopColor + " " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                        case 1:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + "        " + color + "║ " + stopColor + " " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + color + "  ║  " + stopColor + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + color + "═══════╝ " + stopColor + " " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                        case 2:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + " " + color + " ║" + stopColor + "        " + rightConnections[0];
                            lines[2] = leftConnections[1] + color + "  ║  " + stopColor + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + color + " ╚═══════" + stopColor + " " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                        case 3:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + " " + color + " ╔═══════" + stopColor + " " + rightConnections[0];
                            lines[2] = leftConnections[1] + color + "  ║  " + stopColor + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + color + " ║" + stopColor + "        " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                    }
                    break;


                case HOUSING_UNIT, CENTRAL_UNIT:
                    // TODO: print "H" of the same color of the player, need to get player's color

                    HousingUnit housingUnit = (HousingUnit) component;
                    itemsCount = housingUnit.getCrewCount();

                    if (housingUnit.getHasPurpleAlien()) {
                        color = PURPLE;
                        stopColor = RESET;
                    } else if (housingUnit.getHasOrangeAlien()) {
                        color = ORANGE;
                        stopColor = RESET;
                    } else {
                        color = "";
                        stopColor = "";
                    }

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "    (" + color + itemsCount + stopColor + ")    " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case ORANGE_HOUSING_UNIT:
                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case PURPLE_HOUSING_UNIT:
                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case STRUCTURAL_UNIT:
                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case BATTERY_STORAGE:
                    BatteryStorage batteryStorage = (BatteryStorage) component;
                    itemsCount = batteryStorage.getItemsCount();
                    capacity = batteryStorage.getCapacity();

                    lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "    " + itemsCount + "/" + capacity + "    " + rightConnections[2];
                    lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                    break;

                case RED_BOX_STORAGE:
                    BoxStorage redBoxStorage = (BoxStorage) component;
                    capacity = redBoxStorage.getCapacity();
                    boxes = redBoxStorage.getBoxStorage();
                    boxesStr = new String[boxes.length];

                    for (int i = 0; i < boxes.length; i++) {
                        boxesStr[i] = drawBox(boxes[i]);
                    }

                    switch (capacity) {
                        case 1:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + "    [" + boxesStr[0] + "]    " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;

                        case 2:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[1] + "] " + rightConnections[1];
                            lines[3] = leftConnections[2] + "           " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                    }

                    break;

                case BOX_STORAGE:
                    BoxStorage boxStorage = (BoxStorage) component;
                    capacity = boxStorage.getCapacity();
                    boxes = boxStorage.getBoxStorage();
                    boxesStr = new String[boxes.length];

                    for (int i = 0; i < boxes.length; i++) {
                        boxesStr[i] = drawBox(boxes[i]);
                    }

                    switch (capacity) {
                        case 2:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[1] + "] "  + rightConnections[1];
                            lines[3] = leftConnections[2] + "           " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;

                        case 3:
                            lines[0] = " ┌──" + upConnections[0] + "──" + upConnections[1] + "──" + upConnections[2] + "──┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[2] + "] " + rightConnections[1];
                            lines[3] = leftConnections[2] + "    [" + boxesStr[1] + "]    " + rightConnections[2];
                            lines[4] = " └──" + downConnections[0] + "──" + downConnections[1] + "──" + downConnections[2] + "──┘ ";
                            break;
                    }

                    break;
            }

        } else {
            lines[0] = " ┌───────────┐ ";
            lines[1] = " │           │ ";
            lines[2] = " │           │ ";
            lines[3] = " │           │ ";
            lines[4] = " └───────────┘ ";
        }

        // Logic to highlight components that are incorrectly placed
        if (component != null && component.getIncorrectlyPlaced()) {
            lines[0] = RED + lines[0] + RESET;
            lines[1] = RED + lines[1] + RESET;
            lines[2] = RED + lines[2] + RESET;
            lines[3] = RED + lines[3] + RESET;
            lines[4] = RED + lines[4] + RESET;
            return lines;
        }

        // Logic to highlight components that can host an alien
        if (highlightComponent != null && component != null) {

            if (highlightComponent.equalsIgnoreCase("purple") && component.getType().equals(ComponentType.HOUSING_UNIT) && ((HousingUnit) component).getAllowPurpleAlien()) {
                lines[0] = PURPLE + lines[0] + RESET;
                lines[1] = PURPLE + lines[1] + RESET;
                lines[2] = PURPLE + lines[2] + RESET;
                lines[3] = PURPLE + lines[3] + RESET;
                lines[4] = PURPLE + lines[4] + RESET;
            }

            if (highlightComponent.equalsIgnoreCase("orange") && component.getType().equals(ComponentType.HOUSING_UNIT) && ((HousingUnit) component).getAllowOrangeAlien()) {
                lines[0] = ORANGE + lines[0] + RESET;
                lines[1] = ORANGE + lines[1] + RESET;
                lines[2] = ORANGE + lines[2] + RESET;
                lines[3] = ORANGE + lines[3] + RESET;
                lines[4] = ORANGE + lines[4] + RESET;
            }
        }

        return lines;
    }

    public static void printComponent(Component component) {

        String[] lines = TuiPrinters.drawComponent(component);

        for (int row = 0; row < 5; row++) {
            System.out.print(lines[row]);
            System.out.println();
        }
    }

    public static void printVisibleComponents(ArrayList<Component> visibleComponents) {

        System.out.println("Current Visible Components:");
        System.out.println();

        int totalComponents = visibleComponents.size();
        int maxPerRow = 5;

        if (totalComponents == 0) {
            System.out.println("Empty visible components...");
            return;
        }

        for (int start = 0; start < totalComponents; start += maxPerRow) {
            int end = Math.min(start + maxPerRow, totalComponents);
            int numComponentsInRow = end - start;

            String[][] componentLines = new String[numComponentsInRow][5];

            for (int i = 0; i < numComponentsInRow; i++) {
                componentLines[i] = TuiPrinters.drawComponent(visibleComponents.get(start + i));
            }

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < numComponentsInRow; col++) {
                    System.out.print(componentLines[col][row] + "  ");
                }
                System.out.println();
            }

            for (int i = 0; i < numComponentsInRow; i++) {
                String indexStr = String.format("      [%d]      ", start + i);
                System.out.print(indexStr + "  ");
            }

            System.out.println();
            System.out.println();
        }
    }

    public static void printBookedComponents(Component[] bookedComponents) {

        System.out.println("Current Booked Components:");
        System.out.println();

        int numComponents = bookedComponents.length;
        String[][] componentLines = new String[numComponents][5];

        for (int i = 0; i < numComponents; i++) {
            componentLines[i] = TuiPrinters.drawComponent(bookedComponents[i]);
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < numComponents; col++) {
                System.out.print(componentLines[col][row] + "  ");
            }
            System.out.println();
        }

        for (int i = 0; i < numComponents; i++) {
            String indexStr = String.format("      [%d]      ", i);
            System.out.print(indexStr + "  ");
        }
        System.out.println();
    }

    public static void printDestroyedComponent(String player, int x, int y){
        int levelGame = GameData.getLevelGame();

        int xNormalized = x + 6 - levelGame;
        int yNormalized = y + 5;

        if (player == null) {
            System.out.println("You lost component positioned:");
            System.out.printf ("│ X: %d %n", xNormalized);
            System.out.printf ("│ Y: %d %n", yNormalized);
        } else {
            System.out.printf("%s lost component positioned: %n", player);
            System.out.printf ("│ X: %d %n", xNormalized);
            System.out.printf ("│ Y: %d %n", yNormalized);
        }
    }

    // =======================
    // SPACESHIP
    // =======================

    public static void printSpaceship(String owner, Spaceship spaceship) {

        if (GameData.getNamePlayer().equals(owner)) {
            System.out.println("\uD83D\uDE80 Your Spaceship:");
        } else {
            System.out.println("\uD83D\uDE80 " + owner + "'s Spaceship:");
        }
        System.out.println();

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getCopySpaceshipMatrix();
        int rows = spaceshipMatrix.length;
        int cols = spaceshipMatrix[0].length;

        int[][] mask = spaceship.getBuildingBoard().getBoardMask();

        String[][][] gridVisual = new String[rows][cols][5];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // Checks current cell could be occupied
                if (mask[i][j] != 0) {
                    gridVisual[i][j] = drawComponent(spaceshipMatrix[i][j]);

                } else {
                    gridVisual[i][j][0] = "               ";
                    gridVisual[i][j][1] = "               ";
                    gridVisual[i][j][2] = "               ";
                    gridVisual[i][j][3] = "               ";
                    gridVisual[i][j][4] = "               ";
                }
            }
        }

        System.out.print("      ");
        for (int j = 0; j < cols; j++) {
            int num = j + 6 - spaceship.getLevelShip();
            System.out.print("      [" + num + "]      ");
        }
        System.out.println();
        System.out.println();

        for (int i = 0; i < rows; i++) {
            for (int line = 0; line < 5; line++) {
                if (line == 2) {
                    int num = i + 5;
                    System.out.print(String.format("[%d]   ", num));
                } else {
                    System.out.print("      ");
                }

                for (int j = 0; j < cols; j++) {
                    System.out.print(gridVisual[i][j][line] + "");
                }
                System.out.println();
            }
        }

        System.out.println();
    }

    public static void printSpaceshipStats(Spaceship spaceship) {

        System.out.println("🚀 Your Spaceship Stats:");

        System.out.println("\nAttributes");
        System.out.printf("│ Level                : %d%n", spaceship.getLevelShip());
        System.out.printf("│ Destroyed components : %d%n", spaceship.getDestroyedCount());
        System.out.printf("│ Crew members         : %d%n", spaceship.getCrewCount());
        System.out.printf("│ Batteries            : %d%n", spaceship.getBatteriesCount());
        System.out.printf("│ Exposed connectors   : %d%n", spaceship.getExposedConnectorsCount());
        System.out.printf("│ Purple alien         : %b%n", spaceship.getAlienPurple());
        System.out.printf("│ Orange alien         : %b%n", spaceship.getAlienOrange());

        System.out.println("\nCannons");
        System.out.printf("│ Normal power         : %.1f%n", spaceship.getNormalShootingPower());
        System.out.printf("│ Half double cannons  : %d%n", spaceship.getHalfDoubleCannonCount());
        System.out.printf("│ Full double cannons  : %d%n", spaceship.getFullDoubleCannonCount());

        System.out.println("\nEngines");
        System.out.printf("│ Normal engine power  : %d%n", spaceship.getNormalEnginePower());
        System.out.printf("│ Double engines       : %d%n", spaceship.getDoubleEngineCount());

        System.out.println("\nShields (up, right, down, left)");
        System.out.printf("│ Shields              : [%d, %d, %d, %d]%n", spaceship.getIdxShieldCount(0), spaceship.getIdxShieldCount(1), spaceship.getIdxShieldCount(2), spaceship.getIdxShieldCount(3));

        System.out.println("\nStorage (red, yellow, green, blue)");
        int[] boxes = spaceship.getBoxCounts();
        System.out.printf("│ Boxes                : [%d, %d, %d, %d]%n", boxes[0], boxes[1], boxes[2], boxes[3]);
        System.out.println();
    }

    // =======================
    // EVENT CARDS
    // =======================
    
    public static void printEventCard(EventCard card) {
        int width = 50;

        String topBorder = "┌" + "─".repeat(width) + "┐";
        String middleBorder = "├" + "─".repeat(width) + "┤";
        String bottomBorder = "└" + "─".repeat(width) + "┘";

        System.out.println(topBorder);

        String title = switch (card.getType()) {
            case METEORSRAIN -> "Meteor Rain";
            case SLAVERS -> "Slavers";
            case SMUGGLERS -> "Smugglers";
            case LOSTSTATION -> "Lost Station";
            case BATTLEZONE -> "Battlezone";
            case PIRATES -> "Pirates";
            case PLANETS -> "Planets";
            case LOSTSHIP -> "Lost Ship";
            case STARDUST -> "Stardust";
            case EPIDEMIC -> "Epidemic";
            case OPENSPACE -> "Open Space";
            default -> "Card Not Found";
        };

        printEventCardLine(title);

        System.out.println(middleBorder);

        switch (card.getType()) {
            case METEORSRAIN -> {
                MeteorsRain meteorsRain = (MeteorsRain) card;
                printEventCardLine("Meteors:");
                for (int i = 0; i < meteorsRain.getMeteors().size(); i++) {
                    printEventCardLine(String.format("  Meteor %d: %s", i, meteorsRain.getMeteors().get(i).toString()));
                }
            }
            case SLAVERS -> {
                Slavers slavers = (Slavers) card;
                printEventCardLine(String.format("Strength: %d", slavers.getFirePowerRequired()));
                printEventCardLine(String.format("Penalty crew: %d", slavers.getPenaltyCrew()));
                printEventCardLine(String.format("Penalty days: %d", slavers.getPenaltyDays()));
                printEventCardLine(String.format("Credits reward: %d", slavers.getRewardCredits()));
            }
            case SMUGGLERS -> {
                Smugglers smugglers = (Smugglers) card;
                printEventCardLine(String.format("Strength: %d", smugglers.getFirePowerRequired()));
                printEventCardLine(String.format("Penalty boxes: %d", smugglers.getPenaltyBoxes()));
                printEventCardLine(String.format("Penalty days: %d", smugglers.getPenaltyDays()));
                String line = "Rewards: ";
                for (var box : smugglers.getRewardBoxes()) {
                    line += TuiPrinters.drawBox(box) + " ";
                }
                printEventCardLine(line);
            }
            case LOSTSTATION -> {
                LostStation station = (LostStation) card;
                String line = "Rewards: ";
                for (var box : station.getRewardBoxes()) {
                    line += TuiPrinters.drawBox(box) + " ";
                }
                printEventCardLine(line);
                printEventCardLine(String.format("Penalty days: %d", station.getPenaltyDays()));
                printEventCardLine(String.format("Required crew: %d", station.getRequiredCrew()));
            }
            case BATTLEZONE -> {
                Battlezone battlezone = (Battlezone) card;
                int count = 0;

                for (ConditionPenalty couple : battlezone.getCouples()) {
                    String line = "Condition: " + couple.getCondition();
                    printEventCardLine(line);
                    line = "Penalty: " + couple.getPenalty().getType();
                    printEventCardLine(line);
                    if (couple.getPenalty().getType().toString().equals("PENALTYSHOTS")) {
                        for (int i = 0; i < couple.getPenalty().getShots().size(); i++) {
                            printEventCardLine(String.format("  Shot %d: %s", i, couple.getPenalty().getShots().get(i)));
                        }
                    } else {
                        printEventCardLine(String.format("  Amount discard: %d", couple.getPenalty().getNeededAmount()));
                    }

                    count++;

                    if (count < battlezone.getCouples().size()) {
                        System.out.println(middleBorder);
                    }
                }
            }
            case PIRATES -> {
                Pirates pirates = (Pirates) card;
                printEventCardLine(String.format("Strength: %d", pirates.getFirePowerRequired()));
                printEventCardLine("Shots:");
                for (int i = 0; i < pirates.getPenaltyShots().size(); i++) {
                    printEventCardLine(String.format("  Shot %d: %s", i, pirates.getPenaltyShots().get(i)));
                }
                printEventCardLine(String.format("Penalty days: %d", pirates.getPenaltyDays()));
                printEventCardLine(String.format("Credits reward: %d", pirates.getRewardCredits()));
            }
            case PLANETS -> {
                Planets planets = (Planets) card;
                printEventCardLine("Rewards per planet:");
                for (int i = 0; i < planets.getRewardsForPlanets().size(); i++) {
                    StringBuilder sb = new StringBuilder();
                    for (var box : planets.getRewardsForPlanets().get(i)) {
                        sb.append(TuiPrinters.drawBox(box)).append(" ");
                    }
                    printEventCardLine(String.format("  Planet %d: %s", i, sb));
                }
                printEventCardLine(String.format("Penalty days: %d", planets.getPenaltyDays()));
            }
            case LOSTSHIP -> {
                LostShip lostShip = (LostShip) card;
                printEventCardLine(String.format("Penalty crew: %d", lostShip.getPenaltyCrew()));
                printEventCardLine(String.format("Penalty days: %d", lostShip.getPenaltyDays()));
                printEventCardLine(String.format("Reward credits: %d", lostShip.getRewardCredits()));
            }
            case STARDUST, EPIDEMIC, OPENSPACE -> {
                printEventCardLine("No special data");
            }
        }

        System.out.println(bottomBorder);
    }

    private static void printEventCardLine(String text) {
        int width = 48;

        String plainText = text.replaceAll("\u001B\\[[;\\d]*m", "");

        int colorCodeLength = text.length() - plainText.length();

        int totalWidth = width + colorCodeLength;

        System.out.printf("│ %-"+totalWidth+"s │%n", text);
    }

    public static void printEventCardDeck(ArrayList<EventCard> eventCardDeck) {

        System.out.println("Picked Up Event Card Deck:");

        for (EventCard eventCard : eventCardDeck) {
            System.out.println();
            printEventCard(eventCard);
        }
    }

    public static void printIncomingProjectile(IncomingProjectileMessage message){

        int from = message.getProjectile().getFrom();
        String[] directions = {"TOP", "RIGHT", "BOTTOM", "LEFT"};
        String direction = (from >= 0 && from < directions.length) ? directions[from] : "unknown";

        System.out.println("Incoming Projectile:");
        System.out.printf ("│ Size: %s %n", message.getProjectile().getSize());
        System.out.printf ("│ From: %s %n", direction);
    }

    // =======================
    // TRACK
    // =======================

    public static void printTrack(ArrayList<Player> travelers, Player[] track) {

        System.out.println("♟\uFE0F Current Track:\n");

        if (!travelers.isEmpty()) {
            for (int i = 0; i < travelers.size(); i++) {
                System.out.println("P" + (i + 1) + ": " + travelers.get(i).getName());
            }
        } else {
            System.out.println("No travelers found");
        }

        System.out.println();

        for (int i = 0; i < track.length; i++) {
            Player current = track[i];

            if (current == null) {
                System.out.print("[  ] ");

            } else {
                int playerIndex = travelers.indexOf(current);
                System.out.print("[P" + (playerIndex + 1) + "] ");
            }
        }

        System.out.println();
    }
}
