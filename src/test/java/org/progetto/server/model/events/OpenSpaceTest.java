package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;
import org.progetto.server.model.Board;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.BatteryStorage;
import org.progetto.server.model.components.ComponentType;

import static org.junit.jupiter.api.Assertions.*;

class OpenSpaceTest {

    @Test
    void chooseDiscardedBattery() {
        BatteryStorage notBattery = new BatteryStorage(ComponentType.HOUSING_UNIT, new int[]{1, 1, 1, 1}, "imgPath", 2);
        BatteryStorage battery = new BatteryStorage(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 2);
        battery.incrementItemsCount(2);

        OpenSpace openspace = new OpenSpace(CardType.OPENSPACE, "imgPath");

        // Returns false if component is not a Housing Unit
        assertFalse(openspace.chooseDiscardedBattery(notBattery));

        // Removes one battery member from the Housing Unit
        assertTrue(openspace.chooseDiscardedBattery(battery));
        assertEquals(1, battery.getItemsCount());
    }

    @Test
    void moveAhead() {
        Board board = new Board(1);
        Player player = new Player("gino", 0, 1);

        board.addTraveler(player, 1);

        OpenSpace openspace = new OpenSpace(CardType.OPENSPACE, "imgPath");

        // Calls moveAhead method
        openspace.moveAhead(board, player, 5);

        assertEquals(player, board.getTrack()[9]);
    }
}