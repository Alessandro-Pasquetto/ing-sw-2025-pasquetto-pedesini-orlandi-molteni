package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.HousingUnit;

public class LostShip extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int penaltyCrew;
    private final int rewardCredits;
    private final int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public LostShip(CardType type, int level, String imgSrc, int penaltyCrew, int rewardCredits, int penaltyDays) {
        super(type, level, imgSrc);
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
     * If the player chooses to take the reward credits, they are moved back by a number of days equal to penaltyDays
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     */
    public void rewardPenalty(Board board, Player player) throws IllegalStateException {
        player.addCredits(this.rewardCredits);
        board.movePlayerByDistance(player, this.penaltyDays);
    }

    // TODO: The controller has to manage the current player's decisions, giving him the possibility to discard an amount of crew members (humans or aliens) equals to penaltyCrew.
    //  This happens only if the player has at least as many crew members as penaltyCrew.
    //  If player wants to discard crew amount required, for each crew member he has to define the housing unit from which discard a member, calling chooseDiscardedCrew().
    //  When this process is ended, so he had discarded the correct amount of crew members, the controller calls rewardPenalty().
}
