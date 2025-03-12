package org.progetto.server.model.events;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;

import java.util.List;

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
     * Moves player ahead of a distance equal to enginePower
     *
     * @author Gabriele, Stefano
     * @param board Game board
     * @param player Current player
     * @param enginePower Player's engine power
     */
    public void effect(Board board, Player player, int enginePower) {
        board.movePlayerByDistance(player, enginePower);
    }

    // TODO: Controller has to manage the fact that each player can increment his enginePower by enabling doubleCannons through the use of a battery
}
