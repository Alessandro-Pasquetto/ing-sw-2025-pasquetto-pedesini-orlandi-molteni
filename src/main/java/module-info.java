module org.progetto{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires com.google.gson;

    opens org.progetto.client to com.google.gson, javafx.fxml;
    opens org.progetto.server to com.google.gson, javafx.fxml;

    exports org.progetto.server;
    exports org.progetto.client;
}