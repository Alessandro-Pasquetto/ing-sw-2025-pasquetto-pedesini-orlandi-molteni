package org.progetto.server.model.events;

import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Box;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.StorageComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Epidemic extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Epidemic(CardType type, String imgSrc) {
        super(type, imgSrc);
    }

    // =======================
    // OTHER METHODS
    // =======================

    /**
     * Through a DFS search collects adjacent infected housing units in the spaceship
     *
     * @author Gabriele
     * @author Stefano
     * @param i spaceshipMatrix's row
     * @param j spaceshipMatrix's column
     * @param prevComponent Is the component of the previous iteration
     * @param buildingBoard Building board
     * @param visitedCells Matrix of already visited cells
     * @param infectedComponents Set of all the infected components
     */
    public void findInfectedComponents(int i, int j, boolean firstIteration, StorageComponent prevComponent, BuildingBoard buildingBoard, boolean[][] visitedCells, Set<StorageComponent> infectedComponents) {
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();
        StorageComponent currComponent;

        if (!firstIteration) {
            // Boundary checks and if the cell is not Component, already visited, or it's the first iteration of the recursion
            if (i < 0 || j < 0 || i >= spaceshipMatrix.length || j >= spaceshipMatrix[0].length || spaceshipMatrix[i][j] == null || visitedCells[i][j]) {
                return;
            }

            // Checks if the component isn't a housing unit
            if (!spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT)) {
                return;
            }

            currComponent = (StorageComponent) spaceshipMatrix[i][j];

            // Check if prevComponent and currComponent are connected
            if (!areConnected(prevComponent, currComponent)) {
                return;
            }

            // Check if itemsCount in the components is greater than zero
            if (currComponent.getItemsCount() == 0 && !currComponent.getOrangeAlien() && !currComponent.getPurpleAlien()) {
                return;
            }

            // Adds the curr component to the infectedComponents list
            infectedComponents.add(prevComponent);
            infectedComponents.add(currComponent);
        } else {
            currComponent = (StorageComponent) spaceshipMatrix[i][j];
        }

        // Mark the current cell as visited
        visitedCells[i][j] = true;

        // Explore all 4 adjacent cells (up, down, left, right)
        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};

        for (int d = 0; d < 4; d++) {
            int newRow = i + rowDir[d];
            int newCol = j + colDir[d];
            findInfectedComponents(newRow, newCol, false, currComponent, buildingBoard, visitedCells, infectedComponents);
        }
    }

    /**
     * Iterates through the spaceshipMatrix: when it finds the first housing unit calls findInfectedComponents()
     * Then, it decrements the crew members count by one for each of the infected units
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     */
    private void findHousingUnit(Player player) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();

        boolean[][] visitedCells = new boolean[spaceshipMatrix.length][spaceshipMatrix[0].length];

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            for (int j = 0; j < spaceshipMatrix[i].length; j++) {

                if (spaceshipMatrix[i][j] != null && spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT)) {
                    Set<StorageComponent> infectedComponents = new HashSet<>();

                    findInfectedComponents(i, j, true, null, player.getSpaceship().getBuildingBoard(), visitedCells, infectedComponents);

                    for (StorageComponent component : infectedComponents) {
                        component.decrementItemsCount(1); // TODO: Bisogna gestire il decremento nelle housing unit degli alieni, non solo dei crewmate

                        // if (component.getOrangeAlien()) {
                        //     component.setOrangeAlien(false);
                        // } else if (component.getPurpleAlien()) {
                        //     component.setPurpleAlien(false);
                        // } else if (component.getItemsCount() > 0) {
                        //     component.decrementItemsCount(1);
                        // }

                    }
                }

            }
        }
    }

    /**
     * Calls findHousingUnit()
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     */
    public void effect(Player player) {
        findHousingUnit(player);
    }

    // TODO: The controller calls for each player at the same time effect().
}