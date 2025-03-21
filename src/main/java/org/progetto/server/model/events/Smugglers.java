package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.*;

import java.util.ArrayList;

public class Smugglers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyBoxes;
    private int penaltyDays;
    private ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Smugglers(CardType type, String imgSrc, int firePowerRequired, int penaltyBoxes, int penaltyDays, ArrayList<Box> rewardBoxes) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyBoxes = penaltyBoxes;
        this.penaltyDays = penaltyDays;
        this.rewardBoxes = rewardBoxes;
    }

    // =======================
    // GETTERS
    // =======================

    public int getFirePowerRequired() {
        return firePowerRequired;
    }

    public int getPenaltyBoxes() {
        return penaltyBoxes;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    public ArrayList<Box> getRewardBoxes() {
        return rewardBoxes;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Adds the box chosen by the player from the rewardBoxes to the given component at the specified index
     *
     * @author Gabriele
     * @author Stefano
     * @param component BoxStorageComponent to which the box should be added
     * @param boxIdx Index in the storage where the box will be placed
     * @param box Box to be added
     * @return true if the box was successfully added, false otherwise
     */
    public boolean chooseRewardBox(BoxStorage component, int boxIdx, Box box) {
        return component.addBox(box, boxIdx);
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

    /**
     * If the player chooses to take the reward boxes, they are moved back by a number of days equal to penaltyDays
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     */
    public void penalty(Board board, Player player) {

        board.movePlayerByDistance(player, this.penaltyDays);
    }

    /**
     * Defines battle's outcome
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param firePower Player's current firepower
     * @return 1 if player wins, -1 if loses, and 0 if draws.
     */
    public int battleResult(Player player, int firePower) {
        if (firePower > this.firePowerRequired) {
            return 1;
        } else if (firePower < this.firePowerRequired) {
            return -1;
        } else {
            return 0;
        }
    }

    // TODO: The controller, giving to player the smugglers fire power, gives to the player the possibility to use double cannons through the use of batteries, calling chooseDiscardedBattery().
    //  It calls battleResult() to know battle's outcome.
    //  If player:
    //  - wins, it would ask if he wants rewardBoxes in exchange of penaltyDays.
    //          If he answers "yes", it would ask for each box contained in rewardBoxes if he wants it, otherwise the effect ends.
    //          In the for each, if he answers "yes", the controller will call chooseRewardBox() with the correct params, adding the box in the BoxStorageComponent decided by player.
    //          Otherwise, if the player answers 'no,' the controller moves to the next box in the array until there are no more left.
    //          At the end, the card's effect ends.
    //  - loses, he has to discard an amount of boxes equals to penaltyBoxes (starting from the most premium ones), calling chooseDiscardedBox().
    //           If he has no box left, he has to discard batteries calling chooseDiscardedBattery().
    //           If he hasn't any battery left, card's effect stops.
    //           Then, the smugglers will affect next player.
    //  - draws, nothing happens.
    //           The smugglers will affect next player.
}
