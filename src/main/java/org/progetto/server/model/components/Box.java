package org.progetto.server.model.components;

import java.io.Serializable;

public enum Box implements Serializable {

    BLUE(1),
    GREEN(2),
    YELLOW(3),
    RED(4);

    private final int value;

    Box(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}