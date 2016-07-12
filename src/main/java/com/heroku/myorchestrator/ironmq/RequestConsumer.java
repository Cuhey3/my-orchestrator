package com.heroku.myorchestrator.ironmq;

import static com.heroku.myorchestrator.ironmq.IronmqUtil.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RequestConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("request", true))
                .choice().when((Exchange exchange) -> {
                    return true;
                })
                .to("direct:requestToTask")
                .otherwise()
                .to("direct:requestPool");

        from("direct:requestPool")
                .delay(60 * 1000L)
                .to("ironmq:request?client=myclient&batchDelete=true");

        from("direct:requestToTask")
                .routingSlip(defaultPostQueueUri());
    }
}
