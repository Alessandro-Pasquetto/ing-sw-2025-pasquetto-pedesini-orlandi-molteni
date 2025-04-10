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
    exports org.progetto.server.model.loading to com.fasterxml.jackson.databind;
    exports org.progetto.server.connection.socket;
    exports org.progetto.server.connection.rmi;
    exports org.progetto.messages.toClient;
    exports org.progetto.server.internalMessages;
    exports org.progetto.client.gui;
    opens org.progetto.client.gui to com.google.gson, javafx.fxml;
    exports org.progetto.server.connection;
    opens org.progetto.server.connection to com.google.gson, javafx.fxml;
    exports org.progetto.server.connection.games;
    opens org.progetto.server.connection.games to com.google.gson, javafx.fxml;
    exports org.progetto.client.model;
    opens org.progetto.client.model to com.google.gson, javafx.fxml;
    exports org.progetto.messages.toClient.LostShip;
    exports org.progetto.messages.toClient.EventCommon;
    exports org.progetto.messages.toClient.Building;
    exports org.progetto.client.tui;
    opens org.progetto.client.tui to com.google.gson, javafx.fxml;
}