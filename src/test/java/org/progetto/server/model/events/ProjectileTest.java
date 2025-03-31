package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileTest {

    @Test
    void getSize() {
        Projectile projectile = new Projectile(ProjectileSize.BIG, 3);

        assertEquals(ProjectileSize.BIG, projectile.getSize());
    }

    @Test
    void getFrom() {
        Projectile projectile = new Projectile(ProjectileSize.BIG, 3);

        assertEquals(3, projectile.getFrom());
    }
}