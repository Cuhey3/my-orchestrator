package com.heroku.myorchestrator.util.consumers;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.Kind;
import org.apache.camel.builder.SimpleBuilder;

public class RouteUtil {

    private String type, kind;

    public RouteUtil type(ActionType type) {
        this.type = type.expression();
        return this;
    }

    public RouteUtil kind(Kind kind) {
        this.kind = kind.expression();
        return this;
    }

    public RouteUtil request() {
        this.type = "request";
        return this;
    }

    public RouteUtil snapshot() {
        this.type = ActionType.SNAPSHOT.expression();
        return this;
    }

    public RouteUtil diff() {
        this.type = ActionType.DIFF.expression();
        return this;
    }

    public RouteUtil completion() {
        this.type = "completion";
        this.kind = "all";
        return this;
    }

    public RouteUtil changing() {
        this.type = "changing";
        this.kind = "all";
        return this;
    }

    public RouteUtil exception() {
        this.type = "exception";
        this.kind = "in";
        return this;
    }

    public String id() {
        return type + "_" + kind;
    }

    public SimpleBuilder camelBatchComplete() {
        return SimpleBuilder.simple("${exchangeProperty.CamelBatchComplete}");
    }
}
