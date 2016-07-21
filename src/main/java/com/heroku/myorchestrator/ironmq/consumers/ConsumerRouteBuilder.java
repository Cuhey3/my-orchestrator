package com.heroku.myorchestrator.ironmq.consumers;

import com.heroku.myorchestrator.util.ConsumerUtil;
import com.heroku.myorchestrator.util.IronmqUtil;
import org.apache.camel.builder.RouteBuilder;

public abstract class ConsumerRouteBuilder extends RouteBuilder {

    protected String kind;
    protected IronmqUtil ironmqUtil = new IronmqUtil();
    protected ConsumerUtil consumerUtil = new ConsumerUtil();

}
