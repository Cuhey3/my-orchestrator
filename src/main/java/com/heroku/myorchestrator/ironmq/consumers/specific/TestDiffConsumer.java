package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestDiffConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("test_diff", 60))
                .to(postQueueUri("test_complete"));
    }

}
