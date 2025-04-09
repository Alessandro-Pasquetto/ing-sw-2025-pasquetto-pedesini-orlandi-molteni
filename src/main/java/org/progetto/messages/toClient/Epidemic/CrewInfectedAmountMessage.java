package org.progetto.messages.toClient.Epidemic;

import java.io.Serializable;

public class CrewInfectedAmountMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int infectedCrew;

    // =======================
    // CONSTRUCTORS
    // =======================

    public CrewInfectedAmountMessage(int infectedCrew) {
        this.infectedCrew = infectedCrew;
    }

    // =======================
    // GETTERS
    // =======================

    public int getInfectedCrew() {
        return infectedCrew;
    }
}
