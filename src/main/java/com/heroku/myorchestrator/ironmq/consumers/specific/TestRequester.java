package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestRequester extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=60s")
                .setBody(constant("{}"))
                .to(postQueueUri("test_snapshot"));
    }
}
