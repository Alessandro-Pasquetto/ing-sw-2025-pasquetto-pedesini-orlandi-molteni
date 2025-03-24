package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.BoxStorage;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;

public class Planets extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private boolean planetsTaken[]; // for each cell, it becomes true if that planet is taken by a player
    private ArrayList<ArrayList<Box>> rewardsForPlanets;
    private Stack<Player> landedPlayers; // ordered based on route order
    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Planets(CardType type,int level, String imgSrc, ArrayList<ArrayList<Box>> rewardsForPlanets, int penaltyDays) {
        super(type,level, imgSrc);
        this.rewardsForPlanets = rewardsForPlanets;
        this.penaltyDays = penaltyDays;
        this.planetsTaken = new boolean[rewardsForPlanets.size()];
        this.landedPlayers = new Stack<>();
    }

    // =======================
    // GETTERS
    // =======================

    public boolean[] getPlanetsTaken() {
        return planetsTaken;
    }

    public ArrayList<ArrayList<Box>> getRewardsForPlanets() {
        return rewardsForPlanets;
    }

    public int getPenaltyDays() {
        return penaltyDays;
    }

    public Stack<Player> getLandedPlayers() {
        return landedPlayers;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Sets the planet as taken in planetsTaken array and saves the reference to landed players
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @param planetIdx Planet index in the card
     * @return true if the index chosen is valid and it is actually chosen, false otherwise
     */
    public boolean choosePlanet(Player player, int planetIdx) {
        if (planetIdx >= 0 && planetIdx < rewardsForPlanets.size()) {
            if (!planetsTaken[planetIdx]) {
                planetsTaken[planetIdx] = true;
                landedPlayers.push(player);
                return true;
            }
        }
        return false;
    }

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
    public boolean chooseRewardBox(Spaceship spaceship, BoxStorage component, int boxIdx, Box box) {
        spaceship.addBoxCount(1,box.getType());
        return component.addBox(spaceship ,box, boxIdx);
    }

    /**
     * For each player that has decided to land on a planet, in reverse route order, move their rocket backward on the route, losing the indicated number of flight days (penaltyDays).
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     */
    public void penalty(Board board) {
        while (!landedPlayers.isEmpty()) {
            board.movePlayerByDistance(landedPlayers.peek(), this.penaltyDays);
            landedPlayers.pop();
        }
    }

    // TODO: Starting from the leader, the current player have two choices:
    //  - skip
    //  - chooses a planet to land (if available), so the controller calls choosePlanet().
    //                                             It will ask for each box contained in the corresponding rewardsForPlanets of the planet if he wants it.
    //                                             In the for each, if he answers "yes", the controller will call chooseRewardBox() with the correct params, adding the box in the BoxStorageComponent decided by player.
    //                                             Otherwise, if the player answers 'no,' the controller moves to the next box in the array until there are no more left.
    //  After, the decision process of the current player, if there are any planets still available, the controller moves to the next player.
    //  Otherwise, for each player that has decided to land on a planet, in reverse route order, move their rocket backward on the route, losing the indicated number of flight days (penaltyDays), calling penalty().
}
