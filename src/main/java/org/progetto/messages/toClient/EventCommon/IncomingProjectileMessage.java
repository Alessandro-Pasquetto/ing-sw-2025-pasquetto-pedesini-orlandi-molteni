package org.progetto.messages.toClient.EventCommon;

import org.progetto.server.model.events.Projectile;
import java.io.Serializable;

public class IncomingProjectileMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final Projectile projectile;

    // =======================
    // CONSTRUCTORS
    // =======================

    public IncomingProjectileMessage(Projectile projectile) {
        this.projectile = projectile;
    }

    // =======================
    // GETTERS
    // =======================

    public Projectile getProjectile() {
        return projectile;
    }
}
