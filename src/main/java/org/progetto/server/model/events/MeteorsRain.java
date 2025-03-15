package org.progetto.server.model.events;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;

import java.util.ArrayList;

public class MeteorsRain extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<Projectile> meteors;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRain(CardType type, String imgSrc, ArrayList<Projectile> meteors) {
        super(type, imgSrc);
        this.meteors = meteors;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Projectile> getMeteors() {
        return meteors;
    }

    // =======================
    // OTHER METHODS
    // =======================

    // WORK IN PROGRESS...
    public Component checkExposedConnector(Game game, Player player, Projectile shot, int position) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
        int row, column;

        // REMINDER: mod4(Shot direction - Rotation) = Index in Connections array

        switch (shot.getFrom()) {
            case 0:
                row = 0;
                column = position - 6 + game.getLevel(); // normalization for spaceshipMatrix
                if (column < 0 || column >= spaceshipMatrix[0].length) {
                    return null;
                }
                for (int i = row; i < spaceshipMatrix.length; i++) {
                    if (spaceshipMatrix[i][column] != null) {
                        int[] connections = spaceshipMatrix[i][column].getConnections();
                        if (connections[spaceshipMatrix[i][column].getRotation()] > )   spaceshipMatrix[i][column].getRotation()
                        player.getSpaceship().getBuildingBoard().destroyComponent(i, column);
                        return true;
                    }
                }
                break;
            case 1:
                row = position - 5; // normalization for spaceshipMatrix
                column = spaceshipMatrix[0].length - 1;
                if (row < 0 || row >= spaceshipMatrix.length) {
                    return null;
                }
                for (int j = column; j >= 0; j--) {
                    if (spaceshipMatrix[row][j] != null) {
                        player.getSpaceship().getBuildingBoard().destroyComponent(row, j);
                        return true;
                    }
                }
                break;
            case 2:
                row = spaceshipMatrix.length - 1;
                column = position - 6 + game.getLevel(); // normalization for spaceshipMatrix
                if (column < 0 || column >= spaceshipMatrix[0].length) {
                    return null;
                }
                for (int i = row; i >= 0; i--) {
                    if (spaceshipMatrix[i][column] != null) {
                        player.getSpaceship().getBuildingBoard().destroyComponent(i, column);
                        return true;
                    }
                }
                break;
            case 3:
                row = position - 5; // normalization for spaceshipMatrix
                column = 0;
                if (row < 0 || row >= spaceshipMatrix.length) {
                    return null;
                }
                for (int j = column; j < spaceshipMatrix[0].length; j++) {
                    if (spaceshipMatrix[row][j] != null) {
                        player.getSpaceship().getBuildingBoard().destroyComponent(row, j);
                        return true;
                    }
                }
                break;
        }
        return null;
    }

    public void effect() {

    }

    // TODO: For each meteor, the controller let the leader throw the dices to find impact position.
    //  Each one of the players will be affected by that projectile simultaneously.
    //  There is two types of meteor:
    //  - small: the controller checks if it hits an exposed connector calling checkExposedConnector().
    //           If returns null, the meteor does not destroy anything.
    //           Otherwise, the controller has to check if the player has a shield pointing in the projectile direction, calling checkShields().
    //           If returns true, the controller asks to player if he wants to use battery to enable it (if he has at least a battery).
    //           Otherwise, the meteor hits the exposed connector component.
    //  - big: the controller checks if there is a cannon in the same row/column where the meteor is coming.
    //         If so, there are two options:
    //         1) if it is a single cannon, it destroys the meteor without asking anything and goes to the next meteor.
    //         2) if it is a double cannon, the controller asks to player if he wants to use a battery to enable the cannon.
    //                                      If he answers "yes", the battery is used and the meteor destroyed; otherwise goes on.
    //         Otherwise, the meteor hits the the spaceship.
    //  It goes on until there are no meteor left.
}
