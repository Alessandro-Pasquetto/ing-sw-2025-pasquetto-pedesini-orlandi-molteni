package org.progetto.server.model.events;

import org.progetto.server.model.Board;
import org.progetto.server.model.Game;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

public class Battlezone extends EventCard{

    // =======================
    // ATTRIBUTES
    // =======================

    private ArrayList<ConditionPenalty> couples;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Battlezone(CardType type, String imgSrc,ArrayList<ConditionPenalty> couples) {
        super(type, imgSrc);
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
     * @return true if the crew member was successfully discarded, false if the housing unit is empty
     */
    public boolean chooseDiscardedCrew(HousingUnit component) {
        if (component.getType().equals(ComponentType.HOUSING_UNIT)) {
            if (component.hasOrangeAlien()) {  // if it contains an orange alien
                component.setAlienOrange(false);
            } else if (component.hasPurpleAlien()) {  // if it contains a purple alien
                component.setPurpleAlien(false);
            } else if (component.getCrewCount() > 0) {  // if it has more than one crew member
                return component.decrementCrewCount(1);
            }
            return true;
        } else return false;
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
            case 0:  // shot come from up
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
            case 1:  // shot come from right
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
            case 2:  // shot come from down
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
            case 3:  // shot come from left
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
     * Checks that box chosen to be discarded by player is the most premium one possessed by him
     * If that is true, the box will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param component BoxStorageComponent from which the box will be discarded
     * @param boxIdx Index in the storage where the box is placed
     * @return true if the box was successfully discarded, false if the box chosen isn't the most premium possessed by player
     */
    public boolean chooseDiscardedBox(Player player, BoxStorage component, int boxIdx) {
        Box[] componentsBoxes = component.getBoxStorage();
        Box box = componentsBoxes[boxIdx];
        int[] playerBoxes = player.getSpaceship().getBoxCounts();

        if (playerBoxes[0] > 0) {  // if he has at least a red box
            if (box.getType().equals(BoxType.RED)) {
                component.removeBox(boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[1] > 0) {  // if he has at least a yellow box
            if (box.getType().equals(BoxType.YELLOW)) {
                component.removeBox(boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[2] > 0) {  // if he has at least a green box
            if (box.getType().equals(BoxType.GREEN)) {
                component.removeBox(boxIdx);
                return true;
            } else return false;
        }

        if (playerBoxes[3] > 0) {  // if he has at least a blue box
            if (box.getType().equals(BoxType.BLUE)) {
                component.removeBox(boxIdx);
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
    public boolean chooseDiscardedBattery(BatteryStorage component) {
        if (component.getType().equals(ComponentType.BATTERY_STORAGE)) {
            return component.decrementItemsCount(1);
        } else return false;
    }

    // TODO: There are three couples of condition and penalty for each card.
    //  The controller evaluates each couple sequentially:
    //  1) Finds witch player is the weakest for that condition (in case of draw, pick the player furthest ahead in the route order).
    //  2) Applies the penalty for that player.
    //  3) Go on with the next couple.
    //  There are three types of conditions:
    //  - CrewRequirement
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
