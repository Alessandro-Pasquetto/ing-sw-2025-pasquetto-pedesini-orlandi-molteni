package org.progetto.server.model.events;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

public class MeteorsRain extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<Projectile> meteors;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MeteorsRain(CardType type, int level, String imgSrc, ArrayList<Projectile> meteors) {
        super(type, level, imgSrc);
        this.meteors = meteors;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<Projectile> getMeteors() {
        return new ArrayList<Projectile>(meteors);
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Returns first component hit by meteor
     *
     * @author Gabriele
     * @author Stefano
     * @param game Current game
     * @param player Current player
     * @param shot Current shot
     * @param position Dices result
     * @return first component in his trajectory if it has any exposed connector, otherwise null
     */
    public Component checkImpactComponent(Game game, Player player, Projectile shot, int position) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();
        int row, column;

        switch (shot.getFrom()) {
            case 0:  // shot come from up
                row = 0;
                column = position - 6 + game.getLevel(); // normalization for spaceshipMatrix
                if (column < 0 || column >= spaceshipMatrix[0].length) {
                    return null;
                }
                for (int i = row; i < spaceshipMatrix.length; i++) {
                    if (spaceshipMatrix[i][column] != null) {
                        return spaceshipMatrix[i][column];
                    }
                }
                break;

            case 1:  // shot come from right
                row = position - 5; // normalization for spaceshipMatrix
                column = spaceshipMatrix[0].length - 1;
                if (row < 0 || row >= spaceshipMatrix.length) {
                    return null;
                }
                for (int j = column; j >= 0; j--) {
                    if (spaceshipMatrix[row][j] != null) {
                        return spaceshipMatrix[row][j];
                    }
                }
                break;

            case 2:  // shot come from down
                row = spaceshipMatrix.length - 1;
                column = position - 6 + game.getLevel(); // normalization for spaceshipMatrix
                if (column < 0 || column >= spaceshipMatrix[0].length) {
                    return null;
                }
                for (int i = row; i >= 0; i--) {
                    if (spaceshipMatrix[i][column] != null) {
                        return spaceshipMatrix[i][column];
                    }
                }
                break;

            case 3:  // shot come from left
                row = position - 5; // normalization for spaceshipMatrix
                column = 0;
                if (row < 0 || row >= spaceshipMatrix.length) {
                    return null;
                }
                for (int j = column; j < spaceshipMatrix[0].length; j++) {
                    if (spaceshipMatrix[row][j] != null) {
                        return spaceshipMatrix[row][j];
                    }
                }
                break;
        }
        return null;
    }

    /**
     * Checks if there's at least a shield protecting the shot's origin direction
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param shot Current shot
     * @return true if there is at least a shield protecting the shot's origin direction, otherwise false
     */
    public boolean checkShields(Player player, Projectile shot) {
        Spaceship spaceship = player.getSpaceship();

        return spaceship.getIdxShieldCount(shot.getFrom()) > 0;
    }
}
