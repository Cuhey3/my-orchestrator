package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class CompletionQueueConsumer extends QueueConsumer {

    @Override
    public void configure() {
        from(route().completionConsumeUri())
                .routeId(route().completion().id())
                .filter((Exchange exchange) -> {
                    return new MasterUtil(exchange).toCompleteLogic(this);
                })
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster(this))
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff(this))
                .to(route().changed().postUri());
    }
}
