package com.heroku.myorchestrator.ironmq.consumers.common;

import com.heroku.myorchestrator.ironmq.consumers.ConsumerRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ChangingConsumer extends ConsumerRouteBuilder {

    public ChangingConsumer() {
        ironmqUtil.changed();
        consumerUtil.changing();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.consumeUri())
                .routeId(consumerUtil.id())
                .to("log:foo");
    }
}
