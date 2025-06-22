package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;

public class OpenSpace extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public OpenSpace(CardType type, int level, String imgSrc) {
        super(type, level, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

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
}
