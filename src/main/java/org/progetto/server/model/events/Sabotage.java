package org.progetto.server.model.events;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.List;

public class Sabotage extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Sabotage(CardType type, String imgSrc) {

        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Finds the player with the fewer amount of crew members
     *
     * @author Gabriele
     * @author Stefano
     * @param players Game's players array
     * @return the player with the fewer amount of crew members
     */
    public Player lessPopulatedSpaceship(Player[] players) {
        int minCrewCount = Integer.MAX_VALUE;
        Player minPlayer;
        for (Player player : players) {
            if (player.getSpaceship().getCrewCount() < minCrewCount) {
                minCrewCount = player.getSpaceship().getCrewCount();
                minPlayer = player;
            }
            else if (player.getSpaceship().getCrewCount() == minCrewCount) {
                if (player.getPosition() < minPlayer.getPosition()) {
                    minPlayer = player;
                }
            }
        }
        return minPlayer;
    }

    /**
     * Destroys the component positioned in the selected cell of spaceship
     *
     * @author Gabriele
     * @author Stefano
     * @param row X coordinate of the cell
     * @param column Y coordinate of the cell
     * @param player Current player
     * @return false if the cell is empty, true otherwise
     */
    public boolean penalty(int row, int column, Player player) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();
        if (spaceshipMatrix[row][column] != null) {
            player.getSpaceship().getBuildingBoard().destroyComponent(row, column);
            return true;
        }
        else return false;
    }

    // TODO: The controller calls lessPopulatedSpaceship() and it return the player with the less crew.
    //  The player chosen throws 2 dices to find the column and the row of the component that will be destroyed, calling penalty().
    //  If there is no component in the selected cell the player will throw the dices again.
    //  If after 3 double throws no component has been hit the effect ends.
}
