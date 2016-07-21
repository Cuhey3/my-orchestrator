package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.Kind;
import org.apache.camel.builder.SimpleBuilder;

public class ConsumerUtil {

    private String type;
    private String kind;

    public ConsumerUtil type(ActionType type) {
        this.type = type.expression();
        return this;
    }

    public ConsumerUtil kind(Kind kind) {
        this.kind = kind.expression();
        return this;
    }

    public ConsumerUtil request() {
        this.type = "request";
        return this;
    }

    public ConsumerUtil snapshot() {
        this.type = ActionType.SNAPSHOT.expression();
        return this;
    }

    public ConsumerUtil diff() {
        this.type = ActionType.DIFF.expression();
        return this;
    }

    public ConsumerUtil completion() {
        this.type = "completion";
        this.kind = "all";
        return this;
    }

    public ConsumerUtil changing() {
        this.type = "changing";
        this.kind = "all";
        return this;
    }

    public String id() {
        return type + "_ " + kind;
    }

    public SimpleBuilder camelBatchComplete() {
        return SimpleBuilder.simple("${exchangeProperty.CamelBatchComplete}");
    }
}
