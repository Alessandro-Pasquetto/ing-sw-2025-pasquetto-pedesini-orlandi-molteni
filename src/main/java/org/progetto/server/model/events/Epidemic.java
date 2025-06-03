package org.progetto.server.model.events;

import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Player;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;
import org.progetto.server.model.components.HousingUnit;

import java.util.HashSet;
import java.util.Set;

public class Epidemic extends EventCard {

    // =======================
    // CONSTRUCTORS
    // =======================

    public Epidemic(CardType type, int level, String imgSrc) {
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
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrixCopy();
        HousingUnit currComponent;

        if (!firstIteration) {
            // Checks boundary and if the cell is not Component, already visited, or it's the first iteration of the recursion
            if (i < 0 || j < 0 || i >= spaceshipMatrix.length || j >= spaceshipMatrix[0].length || spaceshipMatrix[i][j] == null || visitedCells[i][j])
                return;

            // Checks if the component isn't a housing unit
            if (!spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT) && !spaceshipMatrix[i][j].getType().equals(ComponentType.CENTRAL_UNIT))
                return;

            currComponent = (HousingUnit) spaceshipMatrix[i][j];

            // Checks if prevComponent and currComponent are connected
            if (!buildingBoard.areConnected(prevComponent, currComponent))
                return;

            // Checks if itemsCount in the components is greater than zero
            if (currComponent.getCrewCount() == 0 && !currComponent.getHasOrangeAlien() && !currComponent.getHasPurpleAlien())
                return;


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
     * Checks if the StorageComponent chosen by player is a housing unit
     * If that is true, the crew member will be removed
     *
     * @author Gabriele
     * @author Stefano
     * @param component StorageComponent from which the crew will be discarded
     */
    private void chooseDiscardedCrew(Spaceship spaceship, HousingUnit component) throws IllegalStateException {
        if (component.getHasOrangeAlien()) {  // if it contains an orange alien
            spaceship.setAlienOrange(false);
            component.setAlienOrange(false);
            spaceship.addNormalEnginePower(-2);
            spaceship.addCrewCount(-1);

        }
        else if (component.getHasPurpleAlien()) {  // if it contains a purple alien
            spaceship.setAlienPurple(false);
            component.setAlienPurple(false);
            spaceship.addNormalShootingPower(-2);
            spaceship.addCrewCount(-1);
        }
        else
            component.decrementCrewCount(spaceship, 1);
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
        Component[][] spaceshipMatrix = player.getSpaceship().getBuildingBoard().getSpaceshipMatrixCopy();

        boolean[][] visitedCells = new boolean[spaceshipMatrix.length][spaceshipMatrix[0].length];

        int infectedCrew = 0;

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            for (int j = 0; j < spaceshipMatrix[i].length; j++) {

                if (spaceshipMatrix[i][j] != null && spaceshipMatrix[i][j].getType().equals(ComponentType.HOUSING_UNIT)) {  // If current component is a housing unit
                    Set<HousingUnit> infectedComponents = new HashSet<>();

                    dfsInfectedComponents(i, j, true, null, player.getSpaceship().getBuildingBoard(), visitedCells, infectedComponents);

                    Spaceship spaceship = player.getSpaceship();

                    // Deletes for each infected component found one crew mate/alien
                    for (HousingUnit component : infectedComponents) {
                        chooseDiscardedCrew(spaceship, component);
                    }

                    infectedCrew += infectedComponents.size();
                }
            }
        }

        return infectedCrew;
    }

    // TODO: The controller calls for each player at the same time epidemicResult().
}