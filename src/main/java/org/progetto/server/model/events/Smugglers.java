package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorageComponent;
import org.progetto.server.model.components.BoxType;
import org.progetto.server.model.components.StorageComponent;
import org.progetto.server.model.components.ComponentType;

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
     * @param idx Index in the storage where the box will be placed
     * @param box Box to be added
     * @return true if the box was successfully added, false otherwise
     */
    public boolean chosenRewardBox(BoxStorageComponent component, int idx, Box box) {
        return component.addBox(box, idx);
    }

    /**
     * Checks that box chosen to be discarded by player is the most premium one possessed by him
     * If that is true, the box will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param component BoxStorageComponent from which the box will be discarded
     * @param idx Index in the storage where the box is placed
     * @return true if the box was successfully discarded, false if the box chosen isn't the most premium possessed by player
     */
    public boolean chosenDiscardedBox(Player player, BoxStorageComponent component, int idx) {
        Box[] componentsBoxes = component.getBoxStorage();
        Box box = componentsBoxes[idx];
        int[] playerBoxes = player.getSpaceship().getBoxes();

        if (playerBoxes[0] > 0) {
            if (box.getType().equals(BoxType.RED)) {
                component.removeBox(idx);
                return true;
            } else return false;
        }

        if (playerBoxes[1] > 0) {
            if (box.getType().equals(BoxType.YELLOW)) {
                component.removeBox(idx);
                return true;
            } else return false;
        }

        if (playerBoxes[2] > 0) {
            if (box.getType().equals(BoxType.GREEN)) {
                component.removeBox(idx);
                return true;
            } else return false;
        }

        if (playerBoxes[3] > 0) {
            if (box.getType().equals(BoxType.BLUE)) {
                component.removeBox(idx);
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
    public boolean chosenDiscardedBattery(StorageComponent component) {
        if (component.getType().equals(ComponentType.BATTERYSTORAGE)) {
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

    // TODO: The controller, giving to player the smugglers fire power, gives to the player the possibility to use double cannons through the use of batteries.
    //  If player:
    //  - wins, it would ask if he wants rewardBoxes in exchange of penaltyDays.
    //          If he answers "yes", it would ask for each box contained in rewardBoxes if he wants it, otherwise the effect ends.
    //          In the for each, if he answers "yes", the controller will call chosenRewardBox() with the correct params, adding the box in the BoxStorageComponent decided by player.
    //          Otherwise, if the player answers 'no,' the controller moves to the next box in the array until there are no more left.
    //  - loses, he has to discard an amount of boxes equals to penaltyBoxes (starting from the most premium ones).
    //           If he has no box left, he has to discard batteries.
    //           Then, the smugglers will affect next player.
    //  - draws, nothing happens.
    //           The smugglers will affect next player.
}
