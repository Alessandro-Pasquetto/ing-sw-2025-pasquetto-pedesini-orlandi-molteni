module org.progetto{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires java.rmi;

    opens org.progetto.client to com.google.gson, javafx.fxml;
    opens org.progetto.server.model to com.google.gson, javafx.fxml;
    opens org.progetto.server.controller to com.google.gson, javafx.fxml;
    opens org.progetto.server.model.components to com.google.gson, javafx.fxml;
    opens org.progetto.server.model.events to com.google.gson, javafx.fxml;
    opens org.progetto.server.connection.socket to com.google.gson, javafx.fxml;
    opens org.progetto.client.connection.socket to com.google.gson, javafx.fxml;
    opens org.progetto.client.connection.rmi to java.rmi;

    exports org.progetto.client;
    exports org.progetto.client.connection.rmi;
    exports org.progetto.server.model;
    exports org.progetto.server.controller;
    exports org.progetto.server.model.components;
    exports org.progetto.server.model.events;
    exports org.progetto.server.model.loadClasses to com.fasterxml.jackson.databind;
    exports org.progetto.server.connection.socket;
    exports org.progetto.server.connection.rmi;
    exports org.progetto.messages.toClient;
    exports org.progetto.server.internalMessages;
    exports org.progetto.client.view;
    opens org.progetto.client.view to com.google.gson, javafx.fxml;
}