module org.progetto{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    opens org.progetto.client to com.google.gson, javafx.fxml;
    opens org.progetto.server.model to com.google.gson, javafx.fxml;
    opens org.progetto.server.controller to com.google.gson, javafx.fxml;
    opens org.progetto.server.model.components to com.google.gson, javafx.fxml;
    opens org.progetto.server.model.events to com.google.gson, javafx.fxml;

    exports org.progetto.client;
    exports org.progetto.server.model;
    exports org.progetto.server.controller;
    exports org.progetto.server.model.components;
    exports org.progetto.server.model.events;
    exports org.progetto.server.model.loadClasses to com.fasterxml.jackson.databind;
}