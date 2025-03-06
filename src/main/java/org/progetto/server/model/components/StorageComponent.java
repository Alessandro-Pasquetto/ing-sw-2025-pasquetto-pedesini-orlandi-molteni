package org.progetto.server.model.components;

public class StorageComponent extends Component {
    int capacity;

    public StorageComponent(ComponentType type, int[] connectionsArray, int capacity) {
        super(type, connectionsArray);
        this.capacity = capacity;
    }
}
