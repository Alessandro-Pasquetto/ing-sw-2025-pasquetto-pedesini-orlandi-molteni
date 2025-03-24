package org.progetto.server.model.loadClasses;

/**
 * @implNote class for mapping Matrix readings from src/main/resources/org.progetto.server/Masks.json
 */
public class MaskMatrix {

    // =======================
    // ATTRIBUTES
    // =======================

    private int[][] baseMatrix;
    private int[][] advancedMatrix;

    // =======================
    // CONSTRUCTORS
    // =======================

    public MaskMatrix() {}

    // =======================
    // GETTERS
    // =======================

    public int[][] getBaseMatrix() {
        return baseMatrix;
    }
    public int[][] getAdvancedMatrix() {
        return advancedMatrix;
    }

    // =======================
    // SETTERS
    // =======================

    public void setBaseMatrix(int[][] baseMatrix) {
        this.baseMatrix = baseMatrix;
    }

    public void setAdvancedMatrix(int[][] advancedMatrix) {
        this.advancedMatrix = advancedMatrix;
    }

}