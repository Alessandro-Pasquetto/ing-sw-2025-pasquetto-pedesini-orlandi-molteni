package org.progetto.messages.toClient.EventCommon;

import java.io.Serializable;

public class HowManyDoubleCannonsMessage implements Serializable {

    // =======================
    // ATTRIBUTES
    // =======================

    int maxUsable;
    int firePowerRequired;
    float shootingPower;

    // =======================
    // CONSTRUCTORS
    // =======================

    public HowManyDoubleCannonsMessage(int maxUsable, int firePowerRequired, float shootingPower) {
        this.maxUsable = maxUsable;
        this.firePowerRequired = firePowerRequired;
        this.shootingPower = shootingPower;
    }

    // =======================
    // GETTERS
    // =======================

    public int getMaxUsable() {
        return maxUsable;
    }
    public int getFirePowerRequired() {return firePowerRequired;}
    public float getShootingPower() {return shootingPower;}
}