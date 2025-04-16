package org.progetto.server.model.events;

import java.io.Serializable;

public class Projectile implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    private final ProjectileSize size;
    private final int from;   // [0, 1, 2, 3] = [top, right, bottom, left]

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

    @Override
    public String toString() {
        String[] directions = {"TOP", "RIGHT", "BOTTOM", "LEFT"};
        String direction = (from >= 0 && from < directions.length) ? directions[from] : "unknown";
        return String.format("Size = %s, From = %s", size.toString(), direction);
    }
}