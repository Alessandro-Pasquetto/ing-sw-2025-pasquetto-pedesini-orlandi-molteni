package org.progetto.server.model.components;

public class ComponentConfig {
    public static Component[] createDictionaryComponents() {
        return new Component[]{

            // Esempio inizializzazione
            new Component(ComponentType.CANNON, new int[]{0,1,2,3}),


            new StorageComponent(ComponentType.CANNON, new int[]{0,1,2,3}, 2),


            new BoxStorageComponent(ComponentType.CANNON, new int[]{0,1,2,3}, 2)
        };
    }
}
