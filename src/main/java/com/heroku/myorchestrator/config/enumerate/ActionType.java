package com.heroku.myorchestrator.config.enumerate;

public enum ActionType {

    SNAPSHOT, DIFF, MASTER, DUMMY, SEIYULAB;

    private final String expression;

    private ActionType() {
        this.expression = this.name().toLowerCase();
    }

    public String expression() {
        return this.expression;
    }
}
