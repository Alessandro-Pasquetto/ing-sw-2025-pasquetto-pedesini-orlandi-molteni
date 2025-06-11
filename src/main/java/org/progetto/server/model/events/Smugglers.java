package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.*;
import org.progetto.server.model.components.Box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Smugglers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int firePowerRequired;
    private final int penaltyBoxes;
    private final int penaltyDays;
    private final ArrayList<Box> rewardBoxes;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Smugglers(CardType type, int level, String imgSrc, int firePowerRequired, int penaltyBoxes, int penaltyDays, ArrayList<Box> rewardBoxes) {
        super(type, level, imgSrc);
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
     * @param box Box to be added
     * @param boxIdx Index in the storage where the box will be placed
     */
    public void chooseRewardBox(Spaceship spaceship, BoxStorage component, Box box, int boxIdx) throws IllegalStateException {
        component.addBox(spaceship, box, boxIdx);
    }


    /**
     * Discards a number of box members from the spaceship, randomly choosing a box storage
     *
     * @author Alessandro
     * @param spaceship Spaceship from which the box will be discarded
     * @param boxesToDiscard Number of box members to discard
     */
    public void randomDiscardBoxes(Spaceship spaceship, int boxesToDiscard) {
        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        Map<Box, Integer> boxCountsMap = new HashMap<>();

        Box[] boxTypes = {Box.RED, Box.YELLOW, Box.GREEN, Box.BLUE};
        int[] boxCounts = spaceship.getBoxCounts();

        int remaining = boxesToDiscard;

        // How many boxes of that type I need to discard
        for (int i = 0; i < 4; i++) {
            int toDiscard = remaining > 0 ? Math.min(boxCounts[i], remaining) : 0;

            boxCountsMap.put(boxTypes[i], toDiscard);
            remaining -= toDiscard;
        }

        // Discard
        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {
                Component component = spaceshipMatrix[row][col];

                if (component instanceof BoxStorage boxStorage) {
                    Box[] boxes = boxStorage.getBoxes();

                    for (int i = 0; i < boxes.length; i++) {

                        Box box = boxes[i];

                        if(box == null) continue;

                        int boxCount = boxCountsMap.get(box);

                        if (boxCount == 0) continue;

                        boxCountsMap.put(box, boxCount - 1);

                        boxStorage.removeBox(spaceship, i);
                        boxesToDiscard--;

                        if(boxesToDiscard == 0) return;
                    }
                }
            }
        }
    }

    public void discardAllBoxes(Spaceship spaceship){

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {
                Component component = spaceshipMatrix[row][col];

                if (component instanceof BoxStorage boxStorage) {

                    Box[] boxes = boxStorage.getBoxes();

                    for (int i = 0; i < boxes.length; i++) {

                        Box box = boxes[i];

                        if(box != null)
                            boxStorage.removeBox(spaceship, i);
                    }
                }
            }
        }
    }

    /**
     * Discards a number of batteries from the spaceship, randomly choosing a battery storage
     *
     * @author Gabriele
     * @param spaceship Spaceship from which the batteries will be discarded
     * @param batteriesToDiscard Number of batteries to discard
     */
    public void randomDiscardBatteries(Spaceship spaceship, int batteriesToDiscard) {
        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {

                Component component = spaceshipMatrix[row][col];

                if (component instanceof BatteryStorage batteryStorage) {

                    while (batteriesToDiscard != 0 && batteryStorage.getItemsCount() > 0) {
                        batteryStorage.decrementItemsCount(spaceship, 1);
                        batteriesToDiscard--;
                    }

                    if (batteriesToDiscard == 0 || spaceship.getBatteriesCount() == 0)
                        return;
                }
            }
        }
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
     * @param firePower Player's current firepower
     * @return 1 if player wins, -1 if loses, and 0 if draws.
     */
    public int battleResult(float firePower) {
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
    //          Otherwise, if the player answers "no", the controller moves to the next box in the array until there are no more left.
    //          At the end, the card's effect ends.
    //  - loses, he has to discard an amount of boxes equals to penaltyBoxes (starting from the most premium ones), calling chooseDiscardedBox().
    //           If he has no box left, he has to discard batteries calling chooseDiscardedBattery().
    //           If he hasn't any battery left, card's effect stops.
    //           Then, the smugglers will affect next player.
    //  - draws, nothing happens.
    //           The smugglers will affect next player.
}
