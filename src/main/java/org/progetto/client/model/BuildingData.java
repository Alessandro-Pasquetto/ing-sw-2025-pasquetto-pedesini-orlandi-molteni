package org.progetto.client.model;

import javafx.scene.layout.Pane;
import org.progetto.client.gui.DragAndDrop;

public class BuildingData {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Pane handComponent = null;
    private static Pane tempPickingBookedComponent = null;
    private static int rHandComponent = 0;
    private static int xHandComponent = -1;
    private static int yHandComponent = 0;
    private static boolean isTimerExpired;
    private static int[][] shipMask;
    private static int currentDeckIdx = -1;

    // =======================
    // GETTERS
    // =======================

    public static Pane getHandComponent() {
        return handComponent;
    }

    public static Pane getTempPickingBookedComponent() {
        return tempPickingBookedComponent;
    }

    public static int getXHandComponent(){
        return xHandComponent;
    }

    public static int getYHandComponent(){
        return yHandComponent;
    }

    public static int getRHandComponent(){
        return rHandComponent;
    }

    public static boolean getIsTimerExpired(){
        return isTimerExpired;
    }

    public static boolean getCellMask(int x, int y){
        return shipMask[y][x] == 1;
    }

    public static int getCurrentDeckIdx() {
        return currentDeckIdx;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setNewHandComponent(Pane handComponent) {
        resetHandComponent();

        BuildingData.handComponent = handComponent;
        if(GameData.getUIType().equals("GUI"))
            DragAndDrop.enableDragAndDropComponent(BuildingData.handComponent);
    }

    public static void setTempPickingBookedComponent(Pane tempPickingBookedComponent) {
        BuildingData.tempPickingBookedComponent = tempPickingBookedComponent;
    }

    public static void setXHandComponent(int xHandComponent){
        BuildingData.xHandComponent = xHandComponent;
    }

    public static void setYHandComponent(int yHandComponent){
        BuildingData.yHandComponent = yHandComponent;
    }

    public static void setIsTimerExpired(boolean isTimerExpired){
        BuildingData.isTimerExpired = isTimerExpired;
    }

    public static void setCurrentDeckIdx(int currentDeckIdx) {
        BuildingData.currentDeckIdx = currentDeckIdx;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void initMask(int levelShip){
        shipMask =  switch (levelShip) {
                        case 1 -> new int[][]{
                                {0, 0, 1, 0, 0},
                                {0, 1, 1, 1, 0},
                                {1, 1, 1, 1, 1},
                                {1, 1, 1, 1, 1},
                                {1, 1, 0, 1, 1},
                        };
                        case 2 -> new int[][]{
                                {0, 0, 1, 0, 1, 0, 0},
                                {0, 1, 1, 1, 1, 1, 0},
                                {1, 1, 1, 1, 1, 1, 1},
                                {1, 1, 1, 1, 1, 1, 1},
                                {1, 1, 1, 0, 1, 1, 1},
                        };
                        default -> null;
                    };
    }

    public static void rotateComponent(){
        handComponent.setRotate(handComponent.getRotate() + 90);

        if(rHandComponent == 3)
            rHandComponent = 0;
        else
            rHandComponent++;
    }

    public static void resetHandComponent(){
        if (BuildingData.getHandComponent() != null && (BuildingData.getYHandComponent() != -1 || BuildingData.getIsTimerExpired())){
            if(GameData.getUIType().equals("GUI")) {
                DragAndDrop.disableDragAndDropComponent(BuildingData.handComponent);
            }
        }

        handComponent = null;
        xHandComponent = -1; // If it has not been placed in the matrix yet
        yHandComponent = 0;
        rHandComponent = 0;
    }
}