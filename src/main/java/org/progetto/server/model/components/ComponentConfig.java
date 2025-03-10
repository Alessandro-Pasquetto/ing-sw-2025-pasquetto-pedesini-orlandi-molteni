package org.progetto.server.model.components;

public class ComponentConfig {
    public static Component[] loadComponents() {
        return new Component[]{

            // Esempio inizializzazione
            new Component(ComponentType.CANNON, new int[]{0,1,2,3}, "imgPath"),


            new StorageComponent(ComponentType.CANNON, new int[]{0,1,2,3}, "imgPath",2),


            new BoxStorageComponent(ComponentType.CANNON, new int[]{0,1,2,3}, "imgPath", 2, true)
        };
    }
}
