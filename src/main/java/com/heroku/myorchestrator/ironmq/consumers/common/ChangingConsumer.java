package com.heroku.myorchestrator.ironmq.consumers.common;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;

@Component
public class ChangingConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("test_changed", 60))
                .to("log:foo");
    }

}
