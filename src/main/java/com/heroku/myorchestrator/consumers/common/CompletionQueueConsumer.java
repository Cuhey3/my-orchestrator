package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.QueueConsumer;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class CompletionQueueConsumer extends QueueConsumer {

    public CompletionQueueConsumer() {
        route().completion();
    }

    @Override
    public void configure() {
        from(ironmq().completionConsumeUri())
                .routeId(route().id())
                .choice()
                .when((Exchange exchange)
                        -> new MasterUtil(exchange).isSkipComparedValidation())
                .to("direct:completionSaveToMaster")
                .otherwise()
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).comparedIsValid(this))
                .to("direct:completionSaveToMaster");

        from("direct:completionSaveToMaster")
                .routeId("completion_save_to_master")
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster(this))
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff(this))
                .to(ironmq().changed().postUri());
    }
}
