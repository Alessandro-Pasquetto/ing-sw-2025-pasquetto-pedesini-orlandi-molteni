package org.progetto.server;

public class Component {

    ComponentType type;
    int[] connections;
    int capacity;

    int rotation;
    boolean hidden;
    int stockedItems;

    Component(ComponentType type, int[] connections, int capacity) {
        this.type = type;
        this.connections = connections;
        this.capacity = capacity;

        this.rotation = 0;
        this.hidden = false;
        this.stockedItems = 0;
    }
}
