package org.progetto.messages.toServer;

import java.io.Serializable;

public class PopulatingMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String crewType;
    private int xComponent;
    private int yComponent;

    // =======================
    // CONSTRUCTORS
    // =======================

    public PopulatingMessage(String crewType, int xComponent, int yComponent) {
        this.crewType = crewType;
        this.xComponent = xComponent;
        this.yComponent = yComponent;
    }

    // =======================
    // GETTERS
    // =======================

    public String getCrewType() {
        return crewType;
    }

    public int getxComponent() {
        return xComponent;
    }

    public int getyComponent() {
        return yComponent;
    }


    // =======================
    // SETTERS
    // =======================

    public void setCrewType(String crewType) {
        this.crewType = crewType;
    }

    public void setxComponent(int xComponent) {
        this.xComponent = xComponent;
    }

    public void setyComponent(int yComponent) {
        this.yComponent = yComponent;
    }
}