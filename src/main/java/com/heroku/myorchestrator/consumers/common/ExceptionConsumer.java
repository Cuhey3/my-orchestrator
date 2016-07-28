package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExceptionConsumer extends ConsumerRouteBuilder {

    public ExceptionConsumer() {
        route().exception();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().exception().kind("in").consumeUri())
                .routeId(route().id())
                .to(ironmq().exception().kind("out").postUri());
    }
}
