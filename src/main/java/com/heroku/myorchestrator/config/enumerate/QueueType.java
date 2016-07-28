package com.heroku.myorchestrator.config.enumerate;

public enum QueueType {

    SNAPSHOT, DIFF, COMPLETION, CHANGED, EXCEPTION;

    private final String expression;

    private QueueType() {
        this.expression = this.name().toLowerCase();
    }

    public String expression() {
        return this.expression;
    }
}
