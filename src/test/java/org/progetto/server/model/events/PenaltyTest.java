package org.progetto.server.model.events;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyTest {

    @Test
    void getType() {
        Penalty penalty = new Penalty(PenaltyType.PENALTYDAYS, -3, new ArrayList<>());

        assertEquals(PenaltyType.PENALTYDAYS, penalty.getType());
    }

    @Test
    void getNeededAmount() {
        Penalty penalty = new Penalty(PenaltyType.PENALTYDAYS, -3, new ArrayList<>());

        assertEquals(-3, penalty.getNeededAmount());
    }

    @Test
    void getShots() {
        ArrayList<Projectile> projectiles = new ArrayList<>();
        projectiles.add(new Projectile(ProjectileSize.BIG, 0));
        projectiles.add(new Projectile(ProjectileSize.SMALL, 3));
        Penalty penalty = new Penalty(PenaltyType.PENALTYDAYS, -3, projectiles);

        assertEquals(projectiles, penalty.getShots());
    }
}