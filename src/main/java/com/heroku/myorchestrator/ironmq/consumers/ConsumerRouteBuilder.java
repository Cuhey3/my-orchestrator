package com.heroku.myorchestrator.ironmq.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.ConsumerUtil;
import com.heroku.myorchestrator.util.IronmqUtil;
import org.apache.camel.builder.RouteBuilder;

public abstract class ConsumerRouteBuilder extends RouteBuilder {

    protected Kind kind;
    protected IronmqUtil ironmqUtil = new IronmqUtil();
    protected ConsumerUtil consumerUtil = new ConsumerUtil();

    public final void setKind(Kind kind) {
        this.kind = kind;
        ironmqUtil.kind(kind);
        consumerUtil.kind(kind);
    }
}
