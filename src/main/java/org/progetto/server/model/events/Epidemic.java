package org.progetto.server.model.events;

import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

import java.util.ArrayList;

public class Epidemic extends EventCard {

    // =======================
    // ATTRIBUTES
    // =======================

    private Component[][] spaceshipMatrix;
    private boolean[][] visitedCells;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Epidemic(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    public void dfs(int i, int j, Component prevComponent) {
        // Boundary checks and if the cell is not "Component" or already visited
        if (i < 0 || j < 0 || i >= spaceshipMatrix.length || j >= spaceshipMatrix[0].length || spaceshipMatrix[i][j] == null || visitedCells[i][j]) {
            return;
        }

        // Checks if the component isn't a housing unit
        if (spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT) || spaceshipMatrix[i][j].getType().equals(ComponentType.ORANGE_HOUSING_UNIT) || spaceshipMatrix[i][j].getType().equals(ComponentType.PURPLE_HOUSING_UNIT)) {
            return;
        }

        // If prevComponent is null, assign the current component as prevComponent
        if (prevComponent == null) {
            prevComponent = spaceshipMatrix[i][j];
        }

        // Mark the current cell as visited
        visitedCells[i][j] = true;

        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};

        // Explore all 4 adjacent cells (up, down, left, right)
        for (int d = 0; d < 4; d++) {
            int newRow = i + rowDir[d];
            int newCol = j + colDir[d];
            dfs(newRow, newCol, prevComponent);
        }
    }

    private void findHousingUnit(Player player) {
        this.spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            for (int j = 0; j < spaceshipMatrix[i].length; j++) {

                if (spaceshipMatrix[i][j] != null && (spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT) || spaceshipMatrix[i][j].getType().equals(ComponentType.ORANGE_HOUSING_UNIT) || spaceshipMatrix[i][j].getType().equals(ComponentType.PURPLE_HOUSING_UNIT))) {
                    // in progress...
                }
            }
        }
    }

    // The Epidemic makes you remove 1 crew member (human or alien) from each occupied cabin that is interconnected to another occupied cabin
    public void effect() {

    }
}
