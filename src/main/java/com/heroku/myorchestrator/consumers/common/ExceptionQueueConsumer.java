package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.QueueConsumer;
import org.springframework.stereotype.Component;

@Component
public class ExceptionQueueConsumer extends QueueConsumer {

    public ExceptionQueueConsumer() {
        route().exception();
    }

    @Override
    public void configure() {
        from(ironmq().exception().kind("in").consumeUri())
                .routeId(route().id())
                .to(ironmq().exception().kind("out").postUri());
    }
}
