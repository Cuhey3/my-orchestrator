package com.heroku.myorchestrator.config.enumerate;

public enum SenseType {
    EMPTY;

    public String expression() {
        return this.name().toLowerCase();
    }
}
