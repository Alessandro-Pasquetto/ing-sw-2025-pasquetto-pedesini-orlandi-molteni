package org.progetto.client.tui;

import org.progetto.client.connection.Sender;
import org.progetto.client.model.GameData;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

/**
 * Contains commands relating to the game execution
 */
public class GameCommands {

    // =======================
    // PRINTING
    // =======================
    public static void printShipStatus(Spaceship ship) {

        Component[][] spaceshipMatrix = ship.getBuildingBoard().getSpaceshipMatrix();

        System.out.println("\n🛠  Spaceship Matrix View");
        System.out.println("   ┌──────────────────────────────┐");

        for (int y = 0; y < spaceshipMatrix.length; y++) {
            System.out.print("   │ ");
            for (int x = 0; x < spaceshipMatrix[y].length; x++) {
                Component c = spaceshipMatrix[y][x];
                String symbol = (c != null) ? abbreviateType(c.getType()) : " . ";
                System.out.print(symbol + " ");
            }
            System.out.println("│");
        }

        System.out.println("   └──────────────────────────────┘");

        System.out.println("\n🔧 Ship Attributes");
        System.out.printf("│ Level                : %d%n", ship.getLevelShip());
        System.out.printf("│ Destroyed components : %d%n", ship.getDestroyedCount());
        System.out.printf("│ Crew members         : %d%n", ship.getCrewCount());
        System.out.printf("│ Batteries            : %d%n", ship.getBatteriesCount());
        System.out.printf("│ Exposed connectors   : %d%n", ship.getExposedConnectorsCount());
        System.out.printf("│ Purple alien         : %b%n", ship.getAlienPurple());
        System.out.printf("│ Orange alien         : %b%n", ship.getAlienOrange());

        System.out.println("\n🔫 Weapons");
        System.out.printf("│ Normal power         : %.1f%n", ship.getNormalShootingPower());
        System.out.printf("│ Half double cannons  : %d%n", ship.getHalfDoubleCannonCount());
        System.out.printf("│ Full double cannons  : %d%n", ship.getFullDoubleCannonCount());

        System.out.println("\n🚀 Engines");
        System.out.printf("│ Normal engine power  : %d%n", ship.getNormalEnginePower());
        System.out.printf("│ Double engines       : %d%n", ship.getDoubleEngineCount());

        System.out.println("\n🛡  Shields (up, right, down, left)");
        System.out.printf("│ Shields              : [%d, %d, %d, %d]%n", ship.getIdxShieldCount(0), ship.getIdxShieldCount(1), ship.getIdxShieldCount(2), ship.getIdxShieldCount(3));

        System.out.println("\n📦 Storage (red, yellow, green, blue)");
        int[] boxes = ship.getBoxCounts();
        System.out.printf("│ Boxes                : [%d, %d, %d, %d]%n", boxes[0], boxes[1], boxes[2], boxes[3]);

        System.out.println();
    }

    private static String abbreviateType(ComponentType type) {
        return switch (type) {
            case CANNON -> "C";
            case DOUBLE_CANNON -> "DC";
            case ENGINE -> "E";
            case DOUBLE_ENGINE -> "DE";
            case SHIELD -> "S";
            case HOUSING_UNIT, ORANGE_HOUSING_UNIT, PURPLE_HOUSING_UNIT -> "H";
            case CENTRAL_UNIT -> "CU";
            case STRUCTURAL_UNIT -> "#";
            case BATTERY_STORAGE -> "B";
            case RED_BOX_STORAGE -> "R";
            case BOX_STORAGE -> "X";
            default -> "?";
        };
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
        sender.showSpaceship(commandParts[1]);

    }

}