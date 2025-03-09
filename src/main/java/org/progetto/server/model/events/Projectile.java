package org.progetto.server.model.events;

public class Projectile {

    // =======================
    // ATTRIBUTES
    // =======================

    private ProjectileSize size;
    private int from;

    // =======================
    // CONSTRUCTORS
    // =======================

    public Projectile(ProjectileSize size, int from) {
        this.size = size;
        this.from = from;
    }

    // =======================
    // GETTERS
    // =======================

    public ProjectileSize getSize() {
        return size;
    }

    public int getFrom() {
        return from;
    }
}