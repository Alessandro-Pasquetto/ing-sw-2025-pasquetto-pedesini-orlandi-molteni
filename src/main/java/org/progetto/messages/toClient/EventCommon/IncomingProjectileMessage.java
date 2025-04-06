package org.progetto.messages.toClient.EventCommon;

import org.progetto.server.model.events.ProjectileSize;

import java.io.Serializable;

public class IncomingProjectileMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private String size;
    private int from;

    // =======================
    // CONSTRUCTORS
    // =======================

    public IncomingProjectileMessage(ProjectileSize projectileSize, int from) {
        this.size = projectileSize.toString();
        this.from = from;
    }

    // =======================
    // GETTERS
    // =======================

    public String getSize() {
        return size;
    }

    public int getFrom() {
        return from;
    }
}
