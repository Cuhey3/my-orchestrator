package com.heroku.myorchestrator.config.enumerate;

public enum QueueType {

    SNAPSHOT("snapshot"),
    DIFF("diff"),
    COMPLETION("completion"),
    CHANGED("changed");

    private final String expression;

    private QueueType(String expression) {
        this.expression = expression;
    }

    public String expression() {
        return this.expression;
    }
}
