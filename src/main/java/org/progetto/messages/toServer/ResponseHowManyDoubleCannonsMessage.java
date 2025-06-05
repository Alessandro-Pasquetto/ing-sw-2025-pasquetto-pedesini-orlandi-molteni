package org.progetto.messages.toServer;

import java.io.Serializable;

public class ResponseHowManyDoubleCannonsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final int howManyWantToUse;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ResponseHowManyDoubleCannonsMessage(int howManyWantToUse) {
        this.howManyWantToUse = howManyWantToUse;
    }

    // =======================
    // GETTERS
    // =======================

    public int getHowManyWantToUse() {
        return howManyWantToUse;
    }
}