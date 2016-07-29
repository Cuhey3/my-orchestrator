package com.heroku.myorchestrator.config.enumerate;

public enum MongoTarget {

    SNAPSHOT, DIFF, MASTER, DUMMY, SEIYULAB;

    private final String expression;

    private MongoTarget() {
        this.expression = this.name().toLowerCase();
    }

    public String expression() {
        return this.expression;
    }
}
