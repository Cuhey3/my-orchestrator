package com.heroku.myorchestrator.ironmq.consumers.common;

import com.heroku.myorchestrator.ironmq.consumers.ConsumerRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ChangingConsumer extends ConsumerRouteBuilder {

    public ChangingConsumer() {
        ironmqUtil.changed();
        routeUtil.changing();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.consumeUri())
                .routeId(routeUtil.id())
                .to("log:foo");
    }
}
