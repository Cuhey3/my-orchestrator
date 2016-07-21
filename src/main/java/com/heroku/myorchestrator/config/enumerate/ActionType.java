package com.heroku.myorchestrator.config.enumerate;

public enum ActionType {

    SNAPSHOT("snapshot"), DIFF("diff"), MASTER("master");
    private final String expression;

    private ActionType(String expression) {
        this.expression = expression;
    }

    public String expression() {
        return this.expression;
    }
}
