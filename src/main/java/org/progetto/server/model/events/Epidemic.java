package org.progetto.server.model.events;

import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.util.HashSet;
import java.util.Set;

public class Epidemic extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Epidemic(CardType type,int level, String imgSrc) {
        super(type, level, imgSrc);
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
    private void dfsInfectedComponents(int i, int j, boolean firstIteration, HousingUnit prevComponent, BuildingBoard buildingBoard, boolean[][] visitedCells, Set<HousingUnit> infectedComponents) {
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();
        HousingUnit currComponent;

        if (!firstIteration) {
            // Checks boundary and if the cell is not Component, already visited, or it's the first iteration of the recursion
            if (i < 0 || j < 0 || i >= spaceshipMatrix.length || j >= spaceshipMatrix[0].length || spaceshipMatrix[i][j] == null || visitedCells[i][j]) {
                return;
            }

            // Checks if the component isn't a housing unit
            if (!spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT)) {
                return;
            }

            currComponent = (HousingUnit) spaceshipMatrix[i][j];

            // Checks if prevComponent and currComponent are connected
            if (!buildingBoard.areConnected(prevComponent, currComponent)) {
                return;
            }

            // Checks if itemsCount in the components is greater than zero
            if (currComponent.getCrewCount() == 0 && !currComponent.getHasOrangeAlien() && !currComponent.getHasPurpleAlien()) {
                return;
            }

            // Adds the curr component to the infectedComponents list
            infectedComponents.add(prevComponent);
            infectedComponents.add(currComponent);
        } else {
            currComponent = (HousingUnit) spaceshipMatrix[i][j];
        }

        // Marks the current cell as visited
        visitedCells[i][j] = true;

        // Explore all 4 adjacent cells (up, down, left, right)
        int[][] directions = {
            {-1, 0}, // up
            {1, 0},  // down
            {0, -1}, // left
            {0, 1}   // right
        };

        for (int d = 0; d < 4; d++) {
            int newRow = i + directions[d][0];
            int newCol = j + directions[d][1];
            dfsInfectedComponents(newRow, newCol, false, currComponent, buildingBoard, visitedCells, infectedComponents);
        }
    }

    /**
     * Iterates through the spaceshipMatrix: when it finds the first housing unit calls dfsInfectedComponents()
     * Then, it decrements the crew members count by one for each of the infected units
     *
     * @author Gabriele
     * @author Stefano
     * @param player Current player
     * @return amount of crew members removed
     */
    public int epidemicResult(Player player) {
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrix();

        boolean[][] visitedCells = new boolean[spaceshipMatrix.length][spaceshipMatrix[0].length];

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            for (int j = 0; j < spaceshipMatrix[i].length; j++) {

                if (spaceshipMatrix[i][j] != null && spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT)) {  // if current component is a housing unit
                    Set<HousingUnit> infectedComponents = new HashSet<>();

                    dfsInfectedComponents(i, j, true, null, player.getSpaceship().getBuildingBoard(), visitedCells, infectedComponents);

                    // Deletes for each infected component found one crew mate/alien
                    for (HousingUnit component : infectedComponents) {
                        if (component.getHasOrangeAlien()) {
                            player.getSpaceship().setAlienOrange(false);
                            component.setAlienOrange(false);
                        } else if (component.getHasPurpleAlien()) {
                            player.getSpaceship().setAlienPurple(false);
                            component.setAlienPurple(false);
                        } else if (component.getCrewCount() > 0) {
                            component.decrementCrewCount(player.getSpaceship(),1);
                        }
                    }

                    return infectedComponents.size();
                }

            }
        }
    }

    // TODO: The controller calls for each player at the same time epidemicResult().
}