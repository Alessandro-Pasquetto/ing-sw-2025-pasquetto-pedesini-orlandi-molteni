package org.progetto.server.model.events;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.StorageComponent;

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
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
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

        switch (shot.getFrom()) {
            case 0:  // shot come from up
                if (spaceship.getIdxShieldCount(0) > 0) {
                    return true;
                }

            case 1:  // shot come from right
                if (spaceship.getIdxShieldCount(1) > 0) {
                    return true;
                }

            case 2:  // shot come from down
                if (spaceship.getIdxShieldCount(2) > 0) {
                    return true;
                }

            case 3:  // shot come from left
                if (spaceship.getIdxShieldCount(3) > 0) {
                    return true;
                }
        }
        return false;
    }

    /**
     * Checks if the StorageComponent chosen by player is a battery storage
     * If that is true, the battery will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param component StorageComponent from which the battery will be discarded
     * @return true if the battery was successfully discarded, false if the battery storage is empty
     */
    public boolean chooseDiscardedBattery(StorageComponent component) {
        if (component.getType().equals(ComponentType.BATTERY_STORAGE)) {
            return component.decrementItemsCount(1);
        } else return false;
    }

    // TODO: For each meteor, the controller let the leader throw the dices to find impact position.
    //  Each one of the players will be affected by that projectile simultaneously.
    //  There is two types of meteor:
    //  - small: the controller checks if it hits an exposed connector calling checkImpactComponent().
    //           If returns null, the meteor does not destroy anything.
    //           Otherwise, the controller has to check if that component has any exposed connector in shot direction (connections[shotDirection] > 0).
    //           If it has any exposed connector, the controller checks if the player has a shield pointing in the projectile direction, calling checkShields().
    //           If returns true, the controller asks to player if he wants to use battery to enable it (if he has at least a battery).
    //           If he wants to use it, it calls chooseDiscardedBattery().
    //           Otherwise, the meteor hits the exposed connector component, so controller destroys previously passed component.
    //  - big: the controller checks what's the first component in meteor's way (row/column), calling checkImpactComponent().
    //         That returns null if there's no component in its trajectory; otherwise returns component's reference.
    //         Controller checks component's type:
    //         1) if it is NOT a cannon: the meteor hits the spaceship, destroying the component.
    //         2) if it is a single cannon: checks cannon's rotation.
    //                                      If it's directed against meteor, it destroys it without asking anything and goes to the next meteor.
    //                                      Otherwise, it destroys the cannon.
    //         3) if it is a double cannon, checks cannon's rotation.
    //                                      If it's NOT directed against meteor, it destroys the cannon.
    //                                      Otherwise, if it's directed against meteor, asks to player if he wants to use a battery to enable the cannon.
    //                                      If he answers "yes", the battery is used and the meteor destroyed; otherwise goes on and destroys the cannon.
    //  It goes on until there are no meteor left.
}
