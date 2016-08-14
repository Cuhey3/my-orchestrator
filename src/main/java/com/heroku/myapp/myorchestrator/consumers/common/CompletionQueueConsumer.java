package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
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
                .filter((Exchange exchange) -> {
                    return new MasterUtil(exchange).toCompleteLogic(this);
                })
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster(this))
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff(this))
                .to(ironmq().changed().postUri());
    }
}
