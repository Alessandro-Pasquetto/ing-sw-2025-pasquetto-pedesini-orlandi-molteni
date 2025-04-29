package org.progetto.client.tui;

public class Command {

    // =======================
    // ATTRIBUTES
    // =======================

    private String name;
    private String description;
    private String usage;
    private String[] phases;

    // =======================
    // CONSTRUCTOR
    // =======================

    public Command(String name, String description, String usage) {
        this.name = name;
        this.description = description;
        this.usage = usage;
    }

    // =======================
    // GETTERS
    // =======================

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }
}