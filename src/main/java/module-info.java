module org.progetto{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires com.google.gson;

    opens org.progetto.client to com.google.gson, javafx.fxml;
    opens org.progetto.server.model to com.google.gson, javafx.fxml;
    opens org.progetto.server.controller to com.google.gson, javafx.fxml;


    exports org.progetto.client;
    exports org.progetto.server.model;
    exports org.progetto.server.controller;
    exports org.progetto.server.model.components;
    opens org.progetto.server.model.components to com.google.gson, javafx.fxml;
    exports org.progetto.server.model.events;
    opens org.progetto.server.model.events to com.google.gson, javafx.fxml;
}