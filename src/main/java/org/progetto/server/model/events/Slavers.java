package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.StorageComponent;

import java.util.ArrayList;

public class Slavers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int firePowerRequired;
    private int penaltyCrew;
    private int penaltyDays;
    private int rewardCredits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Slavers(CardType type, String imgSrc, int firePowerRequired, int penaltyCrew, int penaltyDays, int rewardCredits) {
        super(type, imgSrc);
        this.firePowerRequired = firePowerRequired;
        this.penaltyCrew = penaltyCrew;
        this.penaltyDays = penaltyDays;
        this.rewardCredits = rewardCredits;
    }

    // =======================
    // GETTERS
    // =======================

    public int getFirePowerRequired() {
        return firePowerRequired;
    }

    public int getPenaltyCrew() {
        return penaltyCrew;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    public int getRewardCredits() {
        return rewardCredits;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Checks if the StorageComponent chosen by player is a housing unit
     * If that is true, the crew member will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param component StorageComponent from which the crew will be discarded
     * @return true if the crew member was successfully discarded, false if the housing unit is empty
     */
    public boolean chooseDiscardedCrew(StorageComponent component) {
        if (component.getType().equals(ComponentType.HOUSING_UNIT)) {
            if (component.getOrangeAlien()) {
                component.setOrangeAlien(false);
            } else if (component.getPurpleAlien()) {
                component.setPurpleAlien(false);
            } else if (component.getItemsCount() > 0) {
                component.decrementItemsCount(1);
            }
            return true;
        } else return false;
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
    //  - wins, it would ask if he wants rewardCredits in exchange of penaltyDays and the card's effect ends.
    //  - loses, he has to discard an amount of crew members (humans or aliens) equals to penaltyCrew.
    //           Then, the slavers will affect next player.
    //  - draws, nothing happens.
    //           The slavers will affect next player.
}
