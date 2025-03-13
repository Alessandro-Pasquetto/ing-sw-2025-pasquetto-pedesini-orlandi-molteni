package org.progetto.server.model.events;
import javafx.util.Pair;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.StorageComponent;

import java.util.ArrayList;
import java.util.List;

public class LostShip extends EventCard{

    // =======================
    // ATTRIBUTES
    // =======================

    private int penaltyCrew;
    private int rewardCredits;
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShip(CardType type, String imgSrc, int penaltyCrew, int rewardCredits, int penaltyDays) {
        super(type, imgSrc);
        this.penaltyCrew = penaltyCrew;
        this.rewardCredits = rewardCredits;
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public int getPenaltyCrew() {
        return penaltyCrew;
    }

    public int getRewardCredits() {
        return rewardCredits;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * The player decides to lose a specific number of crew members to get a specified number of credits, that costs a certain number of flight days
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     * @param componentsToProcess ArrayList of Pair objects that contains for each StorageComponent the number of crew members to delete
     */
    public void effect(Board board, Player player, ArrayList<Pair<StorageComponent, Integer>> componentsToProcess) {
        for (Pair<StorageComponent, Integer> pair : componentsToProcess) {
            StorageComponent component = pair.getKey();
            component.decrementItemsCount(pair.getValue());
        }
        board.movePlayerByDistance(player, this.penaltyDays);
        player.addCredits(this.rewardCredits);
    }

    // TODO: Controller has to manage the current player's decisions, giving him the possibility to choose the StorageComponent from which delete the specified number of crew members. Then, it has to create the ArrayList of Pairs, where the first element is the reference to the component and the second crew members amount to delete. When this process is ended, it calls effect() with correct params.
}
