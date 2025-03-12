package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;

import java.util.List;

public class Stardust extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private int penaltyDays;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Stardust(CardType type, String imgSrc, int penaltyDays) {
        super(type, imgSrc);
        this.penaltyDays = penaltyDays;
    }

    // =======================
    // GETTERS
    // =======================

    public int getPenaltyDays() {
        return penaltyDays;
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Moves player back of a distance equal to exposedConnectorsCount
     *
     * @author Gabriele, Stefano
     * @param board Game board
     * @param player Current player
     */
    public void effect(Board board, Player player) {
        int exposedConnectorsCount = player.getSpaceship().getExposedConnectorsCount();
        board.movePlayerByDistance(player, Math.negateExact(exposedConnectorsCount));
    }
}
