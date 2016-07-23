package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.List;
import org.apache.camel.Exchange;
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
                .process((Exchange exchange) -> {
                    exchange.getIn().setBody(
                            MessageUtil.get(exchange, "affect", List.class));
                })
                .split().body()
                .routingSlip(IronmqUtil.affectQueueUri());
    }
}
