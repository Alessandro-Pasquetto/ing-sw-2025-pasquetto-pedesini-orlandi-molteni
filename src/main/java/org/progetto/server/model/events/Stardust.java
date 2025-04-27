package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;

import java.util.List;

public class Stardust extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Stardust(CardType type, int level, String imgSrc) {
        super(type, level, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Moves player back of a distance equal to exposedConnectorsCount
     *
     * @author Gabriele
     * @author Stefano
     * @param board Game board
     * @param player Current player
     */
    public int penalty(Board board, Player player) {
        int exposedConnectorsCount = player.getSpaceship().getExposedConnectorsCount();
        board.movePlayerByDistance(player, Math.negateExact(exposedConnectorsCount));
        return exposedConnectorsCount;
    }

    // TODO: The controller call penalty() for each player.
}
