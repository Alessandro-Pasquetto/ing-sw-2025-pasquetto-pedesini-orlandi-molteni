package org.progetto.server.model.events;

import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

public class Battlezone extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ArrayList<ConditionPenalty> couples;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Battlezone(CardType type, int level, String imgSrc, ArrayList<ConditionPenalty> couples) {
        super(type, level, imgSrc);
        this.couples = couples;
    }

    // =======================
    // GETTERS
    // =======================

    public ArrayList<ConditionPenalty> getCouples() {
        return couples;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Finds the player with the fewer amount of crew members
     *
     * @author Gabriele
     * @author Stefano
     * @param players Game's players array
     * @return the player with the fewer amount of crew members
     */
    public Player lessPopulatedSpaceship(ArrayList<Player> players) {
        int minCrewCount = Integer.MAX_VALUE;
        Player minPlayer = null;

        for (Player player : players) {
            // Calculates the current player crew count
            int currCrewCount = player.getSpaceship().getTotalCrewCount();

            if (currCrewCount < minCrewCount) {
                minCrewCount = currCrewCount;
                minPlayer = player;
            }
            else if (currCrewCount == minCrewCount) {
                // In case of tie, picks farthest player on the route
                if (player.getPosition() > minPlayer.getPosition()) {
                    minPlayer = player;
                }
            }
        }
        return minPlayer;
    }

    /**
     * The player moves back by a number of days equal to penaltyDays
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     * @param penaltyDays Number of days that the player have to lose
     */
    public void penaltyDays(Board board, Player player, int penaltyDays) {
        board.movePlayerByDistance(player, penaltyDays);
    }

    /**
     * Checks if the StorageComponent chosen by player is a housing unit
     * If that is true, the crew member will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param component StorageComponent from which the crew will be discarded
     */
    public void chooseDiscardedCrew(Spaceship spaceship, HousingUnit component) throws IllegalStateException {
        if (component.getHasOrangeAlien()) {  // if it contains an orange alien
            spaceship.setAlienOrange(false);
            component.setAlienOrange(false);
            spaceship.addNormalEnginePower(-2);
        }
        else if (component.getHasPurpleAlien()) {  // if it contains a purple alien
            spaceship.setAlienPurple(false);
            component.setAlienPurple(false);
            spaceship.addNormalShootingPower(-2);
        }

        component.decrementCrewCount(spaceship, 1);
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

    /**
     * If the shot find a component in its trajectory, the function returns it
     *
     * @author Gabriele
     * @author Stefano
     * @param game Current game
     * @param player Current player
     * @param shot Current shot
     * @param position Dices result
     * @return the component to destroy, null otherwise
     */
    public Component penaltyShot(Game game, Player player, Projectile shot, int position) {
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
     * Checks that box chosen to be discarded by player is the most premium one possessed by him
     * If that is true, the box will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param spaceship the spaceship of the current player
     * @param component BoxStorageComponent from which the box will be discarded
     * @param boxIdx Index in the storage where the box is placed
     * @return true if the box was successfully discarded, false if the box chosen isn't the most premium possessed by player
     */
    public boolean chooseDiscardedBox(Spaceship spaceship, BoxStorage component, int boxIdx) {
        Box[] componentsBoxes = component.getBoxes();

        if(boxIdx >= component.getCapacity())
            return false;

        Box box = componentsBoxes[boxIdx];

        if(box == null)
            return false;

        int[] playerBoxes = spaceship.getBoxCounts();

        if (playerBoxes[0] > 0) {  // if he has at least a red box
            if (box == Box.RED) {
                component.removeBox(spaceship, boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[1] > 0) {  // if he has at least a yellow box
            if (box == Box.YELLOW) {
                component.removeBox(spaceship, boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[2] > 0) {  // if he has at least a green box
            if (box == Box.GREEN) {
                component.removeBox(spaceship, boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[3] > 0) {  // if he has at least a blue box
            if (box == Box.BLUE) {
                component.removeBox(spaceship, boxIdx);
                return true;
            } else return false;
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
    public boolean chooseDiscardedBattery(Spaceship spaceship, BatteryStorage component) {
        if (component.getType().equals(ComponentType.BATTERY_STORAGE)) {
            return component.decrementItemsCount(spaceship, 1);
        }
        return false;
    }

    // TODO: There are three couples of condition and penalty for each card.
    //  The controller evaluates each couple sequentially:
    //  1) Finds which player is the weakest for that condition (in case of draw, pick the player furthest ahead in the route order).
    //  2) Applies the penalty for that player.
    //  3) Go on with the next couple.
    //  There are three types of conditions:
    //  - CrewRequirement: finds player with fewer number of crew members calling lessPopulatedSpaceship().
    //  - FirePowerRequirement: starting from the leader and further ahead in the route order the controller gives to the player the possibility to use double cannons through the use of batteries.
    //  - EnginePowerRequirement: starting from the leader and further ahead in the route order the controller gives to the player the possibility to use double engines through the use of batteries.
    //  There are four types of penalties:
    //  - PenaltyDays: calls penaltyDays() for the player
    //  - PenaltyCrew: the player has to discard an amount of crew members (humans or aliens) equals to needAmount, calling for each crew member chooseDiscardedCrew()
    //  - PenaltyShots: the player will throw two dices to the determinate row/column of impact.
    //                  If the shot is small, the controller have to check the position of shields for the player calling checkShields(), in case it have to ask the player if he wants to use the shield or not:
    //                  - "yes", uses one battery and the spaceship is safe (for now), calling chooseDiscardedBattery().
    //                  - "no", go on.
    //                  It calls penaltyShot() for the player, go on with the next shot until there is no more left.
    //  - PenaltyBoxes: he has to discard an amount of boxes equals to penaltyBoxes (starting from the most premium ones).
    //                  If he has no box left, he has to discard batteries.
    //                  If he hasn't any battery left, card's effect stops.
}
