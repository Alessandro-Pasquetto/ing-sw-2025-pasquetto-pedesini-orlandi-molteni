package org.progetto.messages.toClient.Epidemic;

import java.io.Serializable;

public class AnotherPlayerCrewInfectedMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int infectedCrew;
    private String namePlayer;

    // =======================
    // CONSTRUCTORS
    // =======================

    public AnotherPlayerCrewInfectedMessage(int infectedCrew, String namePlayer) {
        this.infectedCrew = infectedCrew;
        this.namePlayer = namePlayer;
    }

    // =======================
    // GETTERS
    // =======================

    public int getInfectedCrew() {
        return infectedCrew;
    }

    public String getNamePlayer() {
        return namePlayer;
    }
}
