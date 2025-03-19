package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.ComponentType;

public class OpenSpace extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpace(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Checks if the StorageComponent chosen by player is a battery storage
     * If that is true, the battery will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param component StorageComponent from which the battery will be discarded
     * @return true if the battery was successfully discarded, false if the battery storage is empty
     */
    public boolean chooseDiscardedBattery(BatteryStorage component) {
        if (component.getType().equals(ComponentType.BATTERY_STORAGE)) {
            return component.decrementItemsCount(1);
        } else return false;
    }

    /**
     * Moves player ahead of a distance equal to enginePower
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     * @param enginePower Player's engine power
     */
    public void moveAhead(Board board, Player player, int enginePower) {
        board.movePlayerByDistance(player, enginePower);
    }

    // TODO: Controller has to ask for each player if he wants to use a battery for his double cannons, calling each time chooseDiscardedBattery().
    //  He asks the players starting from the leader in route order.
}
