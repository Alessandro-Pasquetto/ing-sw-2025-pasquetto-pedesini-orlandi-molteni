package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;

import java.util.ArrayList;
import java.util.List;

public class Pirates extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyDays;
    private int rewardCredits;
    private ArrayList<Projectile> penaltyShots;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Pirates(CardType type, String imgSrc, int firePowerRequired, int penaltyDays, int rewardCredits, ArrayList<Projectile> penaltyShots) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyDays = penaltyDays;
        this.rewardCredits = rewardCredits;
        this.penaltyShots = penaltyShots;
    }

    // =======================
    // GETTERS
    // =======================

    public int getFirePowerRequired() {
        return firePowerRequired;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    public int getRewardCredits() {
        return rewardCredits;
    }

    public ArrayList<Projectile> getPenaltyShots() {
        return penaltyShots;
    }

    // =======================
    // OTHER METHODS
    // =======================

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
            case 0:
                if (spaceship.getIdxShieldCount(0) > 0) {
                    return true;
                }

            case 1:
                if (spaceship.getIdxShieldCount(1) > 0) {
                    return true;
                }

            case 2:
                if (spaceship.getIdxShieldCount(2) > 0) {
                    return true;
                }

            case 3:
                if (spaceship.getIdxShieldCount(3) > 0) {
                    return true;
                }
        }
        return false;
    }

    /**
     * If the shot find a component in its trajectory, the function destroys it.
     *
     * @author Gabriele
     * @author Stefano
     * @param game Current game
     * @param player Current player
     * @param shot Current shot
     * @param position Dices result
     * @return true if a component is destroyed, false otherwise
     */
    public boolean penaltyShot(Game game, Player player, Projectile shot, int position) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
        int row, column;

        switch (shot.getFrom()) {
            case 0:
                row = 0;
                column = position - 6 + game.getLevel(); // normalization for spaceshipMatrix
                if (column < 0 || column >= spaceshipMatrix[0].length) {
                    return false;
                }
                for (int i = row; i < spaceshipMatrix.length; i++) {
                    if (spaceshipMatrix[i][column] != null) {
                        player.getSpaceship().getBuildingBoard().destroyComponent(i, column);
                        return true;
                    }
                }
                break;
            case 1:
                row = position - 5; // normalization for spaceshipMatrix
                column = spaceshipMatrix[0].length - 1;
                if (row < 0 || row >= spaceshipMatrix.length) {
                    return false;
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
                    return false;
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
                    return false;
                }
                for (int j = column; j < spaceshipMatrix[0].length; j++) {
                    if (spaceshipMatrix[row][j] != null) {
                        player.getSpaceship().getBuildingBoard().destroyComponent(row, j);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    /**
     * If the player chooses to take the reward credits, they are moved back by a number of days equal to penaltyDays
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     */
    public void rewardPenalty(Board board, Player player) {
        player.addCredits(this.rewardCredits);
        board.movePlayerByDistance(player, this.penaltyDays);
    }

    /**
     * Applies the effect based on the player's firepower compared to the required threshold
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param firePower Player's current firepower
     * @return 1 if player wins, -1 if loses, and 0 if draws.
     */
    public int effect(Player player, int firePower) {
        if (firePower > this.firePowerRequired) {
            return 1;
        } else if (firePower < this.firePowerRequired) {
            return -1;
        } else {
            return 0;
        }
    }

    // TODO: The controller, giving to player the slavers fire power, gives to the player the possibility to use double cannons through the use of batteries.
    //  If player:
    //  - wins, it would ask if he wants rewardCredits in exchange of penaltyDays.
    //          Pirates are defeated, so now we have to handle the defeatedPlayers so far.
    //  - loses, the controller adds the player to defeatedPlayers list.
    //           Then, pirates will affect next player.
    //  - draws, nothing happens.
    //           Pirates will affect next player.
    //  If there is any defeated player, the first defeated player will throw two dices to the determinate row/column of impact.
    //  If the shot is small, the controller have to check the position of shields for each player calling checkShields(), in case it have to ask the player if he wants to use the shield or not:
    //  - "yes", uses one battery and the spaceship is safe (for now).
    //  - "no", go on.
    //  It calls penaltyShot() for each defeated player, go on with the next shot until there is no more left.
    //  The card's effect ends.
}
