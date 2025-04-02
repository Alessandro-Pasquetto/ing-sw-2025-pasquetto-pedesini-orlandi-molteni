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
    private static boolean timerExpired;

    // =======================
    // GETTERS
    // =======================

    public static ImageView getHandComponent() {
        return handComponent;
    }

    public static ImageView getTempBookedComponent() {
        return tempBookedComponent;
    }

    public static int getrHandComponent(){
        return rHandComponent;
    }

    public static int getxHandComponent(){
        return xHandComponent;
    }

    public static int getyHandComponent(){
        return yHandComponent;
    }

    public static boolean getTimerExpired(){
        return timerExpired;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setNewHandComponent(ImageView handComponent) {

        if (BuildingData.getHandComponent() != null)
            DragAndDrop.disableDragAndDrop(BuildingData.handComponent);

        resetHandComponent();

        BuildingData.handComponent = handComponent;
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

    public static void setTimerExpired(boolean timerExpired){
        BuildingData.timerExpired = timerExpired;
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
        handComponent = null;
        xHandComponent = -1; // If it has not been placed in the matrix yet
        yHandComponent = 0;
        rHandComponent = 0;
    }

    public static boolean isTempPlaced(){
        return handComponent != null && xHandComponent != -1;
    }

    public static boolean isBookedPlaced(){
        return handComponent != null && yHandComponent == -1;
    }
}