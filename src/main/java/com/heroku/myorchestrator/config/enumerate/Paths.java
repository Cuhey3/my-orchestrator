package com.heroku.myorchestrator.config.enumerate;

public enum Paths {
    SETTINGS("/config/settings.json"), IRON("/config/iron.json");
    private final String path;

    private Paths(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
