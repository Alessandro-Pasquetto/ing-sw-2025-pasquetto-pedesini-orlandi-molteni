package org.progetto.server.model.events;

public class Projectile {

    // =======================
    // ATTRIBUTES
    // =======================

    private ProjectileSize size;
    private int from;   // [0, 1, 2, 3] = [top, right, bottom, left]

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