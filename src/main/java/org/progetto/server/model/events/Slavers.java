package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.HousingUnit;

public class Slavers extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int firePowerRequired;
    private final int penaltyCrew;
    private final int penaltyDays;
    private final int rewardCredits;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Slavers(CardType type, int level, String imgSrc, int firePowerRequired, int penaltyCrew, int penaltyDays, int rewardCredits) {
        super(type, level, imgSrc);
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
     */
    public void chooseDiscardedCrew(Spaceship spaceship, HousingUnit component) throws IllegalStateException {
        if (component.getHasOrangeAlien()) {  // if it contains an orange alien
            spaceship.setAlienOrange(false);
            component.setAlienOrange(false);
            if (spaceship.getIncreasedEnginePowerByAlien()) {
                spaceship.setIncreasedEnginePowerByAlien(false);
                spaceship.addNormalEnginePower(-2);
            }
        }
        else if (component.getHasPurpleAlien()) {  // if it contains a purple alien
            spaceship.setAlienPurple(false);
            component.setAlienPurple(false);
            if (spaceship.getIncreasedShootingPowerByAlien()) {
                spaceship.setIncreasedShootingPowerByAlien(false);
                spaceship.addNormalShootingPower(-2);
            }
        }

        component.decrementCrewCount(spaceship, 1);
    }

    /**
     * Discards a number of crew members from the spaceship, randomly choosing a housing unit
     *
     * @author Alessandro
     * @param spaceship Spaceship from which the crew members will be discarded
     * @param crewMembersToDiscard Number of crew members to discard
     */
    public void randomDiscardCrew(Spaceship spaceship, int crewMembersToDiscard) {

        Component[][] spaceshipMatrix = spaceship.getBuildingBoard().getSpaceshipMatrixCopy();

        for (int row = 0; row < spaceshipMatrix.length; row++) {
            for (int col = 0; col < spaceshipMatrix[row].length; col++) {

                Component component = spaceshipMatrix[row][col];

                if(component instanceof HousingUnit housingUnit){

                    while(crewMembersToDiscard != 0 && housingUnit.getCrewCount() > 0){
                        chooseDiscardedCrew(spaceship, housingUnit);
                        crewMembersToDiscard--;
                    }

                    if(crewMembersToDiscard == 0 || spaceship.getCrewCount() == 0)
                        return;
                }
            }
        }
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
}
