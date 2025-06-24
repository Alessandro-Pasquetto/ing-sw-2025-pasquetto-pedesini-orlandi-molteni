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

    public Command(String name, String description, String usage, String[] phases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.phases = phases;
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

    public String[] getPhases() {
        return phases;
    }
}