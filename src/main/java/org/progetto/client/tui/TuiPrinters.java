package org.progetto.client.tui;

import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.util.ArrayList;

public class TuiPrinters {

    // =======================
    // COLORS
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

            switch (type) {
                case CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "     " + up + "     " + rightConnections[0];
                    lines[2] = leftConnections[1] + "  " + left + "  " + abbreviateType + "  " + right + "  " + rightConnections[1];
                    lines[3] = leftConnections[2] + "     " + down + "     " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case DOUBLE_CANNON:
                    up = rotation == 0 ? "↑" : " ";
                    right = rotation == 1 ? "→" : " ";
                    down = rotation == 2 ? "↓" : " ";
                    left = rotation == 3 ? "←" : " ";

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "  " + left + " " + up + " " + up + " " + right + "  " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "  " + left + " " + down + " " + down + " " + right + "  " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "     " + up + "     " + rightConnections[0];
                    lines[2] = leftConnections[1] + "  " + left + "  " + abbreviateType + "  " + right + "  " + rightConnections[1];
                    lines[3] = leftConnections[2] + "     " + down + "     " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case DOUBLE_ENGINE:
                    up = rotation == 2 ? "↑" : " ";
                    right = rotation == 3 ? "→" : " ";
                    down = rotation == 0 ? "↓" : " ";
                    left = rotation == 1 ? "←" : " ";

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "  " + left + " " + up + " " + up + " " + right + "  " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "  " + left + " " + down + " " + down + " " + right + "  " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case SHIELD:
                    switch (rotation) {
                        case 0:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + " " + GREEN + "════════╗" + RESET + " " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + GREEN + "   ║ " + RESET + rightConnections[1];
                            lines[3] = leftConnections[2] + "         " + GREEN + "║" + RESET + " " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;
                        case 1:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + "         " + GREEN + "║" + RESET + " " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + GREEN + "   ║ " + RESET + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + GREEN + "════════╝" + RESET + " " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;
                        case 2:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + " " + GREEN + "║" + RESET + "         " + rightConnections[0];
                            lines[2] = leftConnections[1] + GREEN + " ║   " + RESET + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + GREEN + "╚════════" + RESET + " " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;
                        case 3:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + " " + GREEN + "╔════════" + RESET + " " + rightConnections[0];
                            lines[2] = leftConnections[1] + GREEN + " ║   " + RESET + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + " " + GREEN + "║" + RESET + "         " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;
                    }
                    break;


                case HOUSING_UNIT, CENTRAL_UNIT:
                    // TODO: print "H" of the same color of the player, need to get player's color

                    HousingUnit housingUnit = (HousingUnit) component;
                    itemsCount = housingUnit.getCrewCount();
                    String color;

                    if (housingUnit.getHasPurpleAlien()) {
                        color = PURPLE;
                    } else if (housingUnit.getHasOrangeAlien()) {
                        color = ORANGE;
                    } else {
                        color = RESET;
                    }

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "    (" + color + itemsCount + RESET + ")    " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case ORANGE_HOUSING_UNIT:
                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case PURPLE_HOUSING_UNIT:
                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case STRUCTURAL_UNIT:
                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "           " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                    break;

                case BATTERY_STORAGE:
                    BatteryStorage batteryStorage = (BatteryStorage) component;
                    itemsCount = batteryStorage.getItemsCount();
                    capacity = batteryStorage.getCapacity();

                    lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                    lines[1] = leftConnections[0] + "           " + rightConnections[0];
                    lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                    lines[3] = leftConnections[2] + "    " + itemsCount + "/" + capacity + "    " + rightConnections[2];
                    lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
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
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + "     " + abbreviateType + "     " + rightConnections[1];
                            lines[3] = leftConnections[2] + "    [" + boxesStr[0] + "]    " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;

                        case 2:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[1] + "] " + rightConnections[1];
                            lines[3] = leftConnections[2] + "           " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
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
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[1] + "] "  + rightConnections[1];
                            lines[3] = leftConnections[2] + "           " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
                            break;

                        case 3:
                            lines[0] = " ┌───" + upConnections[0] + "─" + upConnections[1] + "─" + upConnections[2] + "───┐ ";
                            lines[1] = leftConnections[0] + "           " + rightConnections[0];
                            lines[2] = leftConnections[1] + " [" + boxesStr[0] + "] " + abbreviateType + " [" + boxesStr[1] + "] " + rightConnections[1];
                            lines[3] = leftConnections[2] + "    [" + boxesStr[2] + "]    " + rightConnections[2];
                            lines[4] = " └───" + downConnections[0] + "─" + downConnections[1] + "─" + downConnections[2] + "───┘ ";
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
                String indexStr = String.format("     [%d]     ", start + i);
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
            String indexStr = String.format("     [%d]     ", i);
            System.out.print(indexStr + "  ");
        }
        System.out.println();
    }

    // =======================
    // SPACESHIP
    // =======================

    public static void printSpaceship(Spaceship spaceship) {

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getCopySpaceshipMatrix();
        int rows = spaceshipMatrix.length;
        int cols = spaceshipMatrix[0].length;

        int[][] mask = spaceship.getBuildingBoard().getBoardMask();

        System.out.println("\n🛠  Spaceship Matrix View");

        System.out.println();

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

        System.out.println("Your Spaceship Stats:");

        System.out.println("\n🔧 Attributes");
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
    }

    // =======================
    // EVENT CARDS
    // =======================

    public static void printEventCard(EventCard card) {

        switch (card.getType()){
            case METEORSRAIN -> {
                MeteorsRain meteorsRain = (MeteorsRain) card;
                System.out.println ("┌─ Meteor Rain ───────");
                System.out.println ("│  Meteors : ");

                for (int i = 0; i < meteorsRain.getMeteors().size(); i++) {
                    System.out.printf("│    Meteor %d :  ", i);
                    System.out.print(meteorsRain.getMeteors().get(i).toString());
                    System.out.println();
                }
                System.out.println("└─────────────────────");
            }

            case SLAVERS -> {
                Slavers slavers = (Slavers) card;
                System.out.println("┌─ Slavers ───────────");
                System.out.printf ("│  Strength : %d %n", slavers.getFirePowerRequired());
                System.out.printf ("│  Penalty crew : %d %n", slavers.getPenaltyCrew());
                System.out.printf ("│  Penalty days : %d %n", slavers.getPenaltyDays());
                System.out.printf ("│  Credits reward : %d %n", slavers.getRewardCredits());
                System.out.println("└─────────────────────");
            }

            case SMUGGLERS -> {
                Smugglers smugglers = (Smugglers) card;
                System.out.println("┌─ Smugglers ─────────");
                System.out.printf ("│  Strength : %d %n", smugglers.getFirePowerRequired());
                System.out.printf ("│  Penalty boxes : %d %n", smugglers.getPenaltyBoxes());
                System.out.printf ("│  Penalty days : %d %n", smugglers.getPenaltyDays());

                System.out.printf("│  Rewards :  ");
                for (int i = 0; i < smugglers.getRewardBoxes().size(); i++) {
                    System.out.print(TuiPrinters.drawBox(smugglers.getRewardBoxes().get(i)) + " ");
                }
                System.out.println();

                System.out.println("└─────────────────────");
            }

            case LOSTSTATION -> {
                LostStation station = (LostStation) card;
                System.out.println("┌─ Lost Station ──────");

                System.out.printf("│  Rewards :  ");
                for (int i = 0; i < station.getRewardBoxes().size(); i++) {
                    System.out.print(TuiPrinters.drawBox(station.getRewardBoxes().get(i)) + " ");
                }
                System.out.println();

                System.out.printf ("│  Penalty days : %d %n", station.getPenaltyDays());
                System.out.printf ("│  Required crew : %d %n", station.getRequiredCrew());
                System.out.println("└─────────────────────");
            }

            case BATTLEZONE -> {
                Battlezone battlezone = (Battlezone) card;
                ArrayList<ConditionPenalty> couples = battlezone.getCouples();
                System.out.println("┌─ Battlezone ────────");

                for (ConditionPenalty couple : couples) {
                    System.out.println("├─────────────────────");

                    System.out.printf ("│  Condition type : %s %n", couple.getCondition().toString());
                    System.out.printf ("│  Penalty type : %s %n", couple.getPenalty().getType().toString());

                    if (couple.getPenalty().getType().toString().equals("PENALTYSHOTS")) {
                        for (int i = 0; i < couple.getPenalty().getShots().size(); i++) {
                            System.out.printf("│    Shot %d :  ", i);
                            System.out.print(couple.getPenalty().getShots().get(i).toString());
                            System.out.println();
                        }
                    } else {
                        System.out.printf ("│    Amount to discard : %d %n", couple.getPenalty().getNeededAmount());
                    }
                }

                System.out.println("└─────────────────────");
            }

            case PIRATES -> {
                Pirates pirates = (Pirates) card;
                System.out.println("┌─ Pirates ───────────");
                System.out.printf ("│  Strength : %d %n", pirates.getFirePowerRequired());
                System.out.println ("│  Shots : ");

                for (int i = 0; i < pirates.getPenaltyShots().size(); i++) {
                    System.out.printf("│    Shot %d :  ", i);
                    System.out.print(pirates.getPenaltyShots().get(i).toString());
                    System.out.println();
                }

                System.out.printf ("│  Penalty days : %d %n", pirates.getPenaltyDays());
                System.out.printf ("│  Credits reward : %d %n", pirates.getRewardCredits());
                System.out.println("└─────────────────────");
            }

            case PLANETS -> {
                Planets planets = (Planets) card;
                System.out.println("┌─ Planets ───────────");
                System.out.println("│  Rewards per planet :");

                for (int i = 0; i < planets.getRewardsForPlanets().size(); i++) {
                    System.out.printf("│    Planet %d :  ", i);
                    for (int j = 0; j < planets.getRewardsForPlanets().get(i).size(); j++) {
                        System.out.print(TuiPrinters.drawBox(planets.getRewardsForPlanets().get(i).get(j)) + " ");
                    }
                    System.out.println();
                }

                System.out.printf("│  Penalty days : %d%n", planets.getPenaltyDays());
                System.out.println("└─────────────────────");
            }

            case LOSTSHIP -> {
                LostShip lostShip = (LostShip) card;
                System.out.println("┌─ Lost Ship ─────────");
                System.out.printf ("│  Penalty crew : %d %n", lostShip.getPenaltyCrew());
                System.out.printf ("│  Penalty days : %d %n", lostShip.getPenaltyDays());
                System.out.printf ("│  Reward credits : %d %n", lostShip.getRewardCredits());
                System.out.println("└─────────────────────");
            }

            case STARDUST -> {
                Stardust stardust = (Stardust) card;
                System.out.println("┌─ Stardust ──────────");
                System.out.println("└─────────────────────");
            }

            case EPIDEMIC -> {
                Epidemic epidemic = (Epidemic) card;
                System.out.println("┌─ Epidemic ──────────");
                System.out.println("└─────────────────────");
            }

            case OPENSPACE -> {
                OpenSpace openSpace = (OpenSpace) card;
                System.out.println("┌─ OpenSpace ─────────");
                System.out.println("└─────────────────────");
            }
        }
    }

    public static void printEventCardDeck(ArrayList<EventCard> eventCardDeck) {

        System.out.println("Picked Up Event Card Deck:");

        for (EventCard eventCard : eventCardDeck) {
            System.out.println();
            printEventCard(eventCard);
        }
    }

    // =======================
    // TRACK
    // =======================

    public static void printTrack(ArrayList<Player> travelers, Player[] track) {

        System.out.println("Current Track:\n");

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
                System.out.print("[  ]");

            } else {
                int playerIndex = travelers.indexOf(current);
                System.out.print("[P" + (playerIndex + 1) + "]");
            }
        }

        System.out.println();
    }
}
