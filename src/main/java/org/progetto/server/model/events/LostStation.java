package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;

import java.util.ArrayList;

public class LostStation extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int requiredCrew;
    private ArrayList<Box> rewardBoxes;
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostStation(CardType type, String imgSrc, int requiredCrew, ArrayList<Box> rewardBoxes, int penaltyDays) {
        super(type, imgSrc);
        this.requiredCrew = requiredCrew;
        this.rewardBoxes = rewardBoxes;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public int getRequiredCrew() {
        return requiredCrew;
    }

    public ArrayList<Box> getRewardBoxes() {
        return rewardBoxes;
    }

    public int getPenaltyDays() {
        return penaltyDays;
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

    // TODO: The controller asks to each player, starting from the leader, if he wants to accept the card conditions.
    //  If so, it would ask for each box contained in rewardBoxes if he wants it.
    //  If he answers "yes", the controller will call chooseRewardBox() with the correct params, adding the box in the BoxStorageComponent decided by player.
    //  Otherwise, if the player answers 'no,' the controller moves to the next box in the array until there are no more left.
    //  If player had decided to take at least a reward box, calls penalty().
}
