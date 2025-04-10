package org.progetto.client.model;

import javafx.scene.image.ImageView;
import org.progetto.client.gui.DragAndDrop;

public class BuildingData {

    // =======================
    // ATTRIBUTES
    // =======================

    private static ImageView handComponent = null;
    private static ImageView tempBookedComponent = null;
    private static int rHandComponent = 0;
    private static int xHandComponent = -1;
    private static int yHandComponent = 0;
    private static boolean isTimerExpired;

    // =======================
    // GETTERS
    // =======================

    public static ImageView getHandComponent() {
        return handComponent;
    }

    public static ImageView getTempBookedComponent() {
        return tempBookedComponent;
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

    // =======================
    // SETTERS
    // =======================

    public static void setNewHandComponent(ImageView handComponent) {
        resetHandComponent();

        BuildingData.handComponent = handComponent;
        if(GameData.getUIType().equals("GUI"))
            DragAndDrop.enableDragAndDrop(BuildingData.handComponent);
    }

    public static void setTempBookedComponent(ImageView tempBookedComponent) {
        BuildingData.tempBookedComponent = tempBookedComponent;
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

    // =======================
    // OTHER METHODS
    // =======================

    public static void rotateComponent(){
        handComponent.setRotate(handComponent.getRotate() + 90);

        if(rHandComponent == 3)
            rHandComponent = 0;
        else
            rHandComponent++;
    }

    public static void resetHandComponent(){
        if (BuildingData.getHandComponent() != null && (BuildingData.getYHandComponent() != -1 || BuildingData.getIsTimerExpired())){
            if(GameData.getUIType().equals("GUI"))
                DragAndDrop.disableDragAndDrop(BuildingData.handComponent);
        }

        handComponent = null;
        xHandComponent = -1; // If it has not been placed in the matrix yet
        yHandComponent = 0;
        rHandComponent = 0;
    }
}