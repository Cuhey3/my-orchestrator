package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.consumers.RouteUtil;
import org.apache.camel.builder.RouteBuilder;

public abstract class ConsumerRouteBuilder extends RouteBuilder {

    protected Kind kind;
    protected IronmqUtil ironmqUtil = new IronmqUtil();
    protected RouteUtil routeUtil = new RouteUtil();

    public final void setKind(Kind kind) {
        this.kind = kind;
        ironmqUtil.kind(kind);
        routeUtil.kind(kind);
    }
}
