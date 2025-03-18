import org.junit.jupiter.api.Test;
import org.progetto.server.model.BuildingBoard;
import org.progetto.server.model.Spaceship;
import org.progetto.server.model.components.Component;
import org.progetto.server.model.components.ComponentType;

class ValidityShipTest {

    @Test
    void testSomething() {
        Spaceship spaceship = new Spaceship(2, 1);

        BuildingBoard buildingBoard = spaceship.getBuildingBoard();
        Component[][] spaceshipMatrix = buildingBoard.getSpaceshipMatrix();

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 2, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);

        for (int i = 0; i < 4; i++)
            System.out.print(buildingBoard.getHandComponent().getConnections()[i] + " ");

        buildingBoard.placeComponent(1, 3);


        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);
        buildingBoard.placeComponent(1, 2);

        buildingBoard.setHandComponent(new Component(ComponentType.SHIELD, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 1);

        buildingBoard.setHandComponent(new StorageComponent(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 4);

        buildingBoard.setHandComponent(new StorageComponent(ComponentType.BATTERY_STORAGE, new int[]{1, 1, 1, 1}, "imgPath", 3));
        buildingBoard.getHandComponent().setRotation(1);
        buildingBoard.placeComponent(1, 5);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{1, 1, 1, 1}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(2);
        buildingBoard.placeComponent(3, 3);

        buildingBoard.setHandComponent(new Component(ComponentType.CANNON, new int[]{0, 0, 0, 0}, "imgPath"));
        buildingBoard.getHandComponent().setRotation(0);
        buildingBoard.placeComponent(3, 2);


        int[][] mask = buildingBoard.getBoardMask();

        for (int i = 0; i < mask.length; i++) {
            System.out.println();
            for (int j = 0; j < mask[i].length; j++) {
                System.out.printf("%-5s", mask[i][j] + " ");
            }
        }


        System.out.println();

        for (int i = 0; i < spaceshipMatrix.length; i++) {
            System.out.println();
            for (int j = 0; j < spaceshipMatrix[0].length; j++) {
                String value = (spaceshipMatrix[i][j] == null) ? "NULL" : spaceshipMatrix[i][j].getType().toString() + "-" + spaceshipMatrix[i][j].getRotation();
                System.out.printf("%-20s", value);
            }
        }

        System.out.println();
        System.out.println();

        System.out.println(buildingBoard.checkShipValidity());
    }
}
