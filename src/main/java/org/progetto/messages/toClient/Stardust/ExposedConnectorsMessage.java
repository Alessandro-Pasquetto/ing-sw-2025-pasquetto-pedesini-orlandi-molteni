package org.progetto.messages.toClient.Stardust;

import java.io.Serializable;

public class ExposedConnectorsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private int exposedConnectorsCount;

    // =======================
    // CONSTRUCTORS
    // =======================

    public ExposedConnectorsMessage(int exposedConnectorsCount) {
        this.exposedConnectorsCount = exposedConnectorsCount;
    }

    // =======================
    // GETTERS
    // =======================

    public int getExposedConnectorsCount() {
        return exposedConnectorsCount;
    }
}
