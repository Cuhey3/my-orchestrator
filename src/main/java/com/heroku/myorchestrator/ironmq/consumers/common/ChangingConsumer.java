package com.heroku.myorchestrator.ironmq.consumers.common;

import com.heroku.myorchestrator.util.IronmqUtil;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ChangingConsumer extends RouteBuilder {

    private final IronmqUtil ironmqUtil = new IronmqUtil().changed();

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.consumeUri()).to("log:foo");
    }
}
