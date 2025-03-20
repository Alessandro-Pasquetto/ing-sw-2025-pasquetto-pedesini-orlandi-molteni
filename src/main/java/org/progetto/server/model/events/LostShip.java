package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

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
                component.setAlienPurple(false);
            } else if (component.getCrewCount() > 0) {  // if it has more than one crew member
                return component.decrementCrewCount(1);
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

    // TODO: The controller has to manage the current player's decisions, giving him the possibility to discard an amount of crew members (humans or aliens) equals to penaltyCrew.
    //  This happens only if the player has at least as many crew members as penaltyCrew.
    //  If player wants to discard crew amount required, for each crew member he has to define the housing unit from which discard a member, calling chooseDiscardedCrew().
    //  When this process is ended, so he had discarded the correct amount of crew members, the controller calls rewardPenalty().
}
