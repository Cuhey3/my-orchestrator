package com.heroku.myorchestrator.config.enumerate;

public enum Paths {

    SETTINGS, IRON;

    private final String path;

    private Paths() {
        this.path = String.format("/config/%s.json", this.name().toLowerCase());
    }

    public String getPath() {
        return this.path;
    }
}
