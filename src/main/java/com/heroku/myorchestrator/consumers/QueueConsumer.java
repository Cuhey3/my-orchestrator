package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.consumers.RouteUtil;
import org.apache.camel.builder.RouteBuilder;

public abstract class QueueConsumer extends RouteBuilder {

    protected Kind kind;
    private IronmqUtil ironmqUtil;
    //do not let have mongoUtil
    // protected MongoUtil mongoUtil;
    private RouteUtil routeUtil;

    public final void kind(Kind kind) {
        this.kind = kind;
        ironmq().kind(kind);
        route().kind(kind);
    }

    public final IronmqUtil ironmq() {
        if (ironmqUtil == null) {
            ironmqUtil = new IronmqUtil();
        }
        return ironmqUtil;
    }

    public final RouteUtil route() {
        if (routeUtil == null) {
            routeUtil = new RouteUtil();
        }
        return routeUtil;
    }
}
