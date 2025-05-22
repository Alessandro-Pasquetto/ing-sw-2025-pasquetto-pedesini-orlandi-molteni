package org.progetto.client.model;

import javafx.animation.RotateTransition;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.progetto.client.gui.DragAndDrop;

public class BuildingData {

    // =======================
    // ATTRIBUTES
    // =======================

    private static Pane handComponent = null;
    private static Pane tempPickingBookedComponent = null;
    private static int tempXPickingBooked = 0;
    private static int rHandComponent = 0;
    private static int xHandComponent = -1;
    private static int yHandComponent = 0;
    private static boolean isRotating = false;
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

    public static int getTempXPickingBooked() {
        return tempXPickingBooked;
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


    public static void setHandComponent(Pane handComponent) {
        BuildingData.handComponent = handComponent;
    }

    public static void setNewHandComponent(Pane handComponent) {
        resetHandComponent();

        BuildingData.handComponent = handComponent;
        if(GameData.getUIType().equals("GUI"))
            DragAndDrop.enableDragAndDropComponent(BuildingData.handComponent);
    }

    public static void setTempPickingBookedComponent(Pane tempPickingBookedComponent) {
        BuildingData.tempPickingBookedComponent = tempPickingBookedComponent;
    }

    public static void setTempXPickingBooked(int tempXPickingBooked) {
        BuildingData.tempXPickingBooked = tempXPickingBooked;
    }

    public static void setRHandComponent(int rHandComponent) {
        BuildingData.rHandComponent = rHandComponent;
    }

    public static void setXHandComponent(int xHandComponent){
        BuildingData.xHandComponent = xHandComponent;
    }

    public static void setYHandComponent(int yHandComponent){
        BuildingData.yHandComponent = yHandComponent;
    }

    public static void setIsRotating(boolean isRotating) {
        BuildingData.isRotating = isRotating;
    }

    public static void setIsTimerExpired(boolean isTimerExpired){
        BuildingData.isTimerExpired = isTimerExpired;
    }

    public static void setShipMask(int[][] shipMask) {
        BuildingData.shipMask = shipMask;
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
        if (isRotating) return;

        isRotating = true;

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), handComponent);
        rotateTransition.setByAngle(90);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);

        rotateTransition.setOnFinished(event -> {
            isRotating = false;

            if(rHandComponent == 3)
                rHandComponent = 0;
            else
                rHandComponent++;
        });

        rotateTransition.play();
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

    public static void resetBuildingData(){
        setHandComponent(null);
        setTempPickingBookedComponent(null);
        setTempXPickingBooked(0);
        setRHandComponent(0);
        setXHandComponent(-1);
        setYHandComponent(0);
        setIsRotating(false);
        setIsTimerExpired(false);
        setShipMask(null);
        setCurrentDeckIdx(-1);
    }
}