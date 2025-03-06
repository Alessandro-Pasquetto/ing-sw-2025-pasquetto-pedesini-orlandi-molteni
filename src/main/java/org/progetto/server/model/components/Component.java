package org.progetto.server.model.components;

public class Component {

    ComponentType type;
    int[] connections;

    int rotation;
    boolean hidden;
    int stockedItems;

    Component(ComponentType type, int[] connections) {
        this.type = type;
        this.connections = connections;

        this.rotation = 0;
        this.hidden = false;
        this.stockedItems = 0;
    }
}
