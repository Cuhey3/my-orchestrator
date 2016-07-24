package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import org.springframework.stereotype.Component;

@Component
public class ChangingConsumer extends ConsumerRouteBuilder {

    public ChangingConsumer() {
        ironmq().changed();
        route().changing();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().consumeUri())
                .routeId(route().id())
                .filter(MessageUtil.loadAffect())
                .split().body()
                .routingSlip(IronmqUtil.affectQueueUri());
    }
}
