package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseHowManyDoubleEnginesMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int howManyWantToUse;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseHowManyDoubleEnginesMessage(int howManyWantToUse) {
        this.howManyWantToUse = howManyWantToUse;
    }

    // =======================
    // GETTERS
    // =======================

    public int getHowManyWantToUse() {
        return howManyWantToUse;
    }
}
