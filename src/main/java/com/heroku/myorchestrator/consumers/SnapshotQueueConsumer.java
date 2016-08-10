package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class SnapshotQueueConsumer extends QueueConsumer {

    public SnapshotQueueConsumer() {
        route().snapshot();
        kind(Kind.findKindByClassName(this));
    }

    @Override
    public void configure() {
        from(ironmq().snapshot().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(defaultPredicate())
                .choice()
                .when((Exchange exchange)
                        -> new MessageUtil(exchange).getBool("skip_diff"))
                .to(ironmq().completionPostUri())
                .otherwise()
                .to(ironmq().diff().postUri());
    }

    protected Predicate defaultPredicate() {
        return (Exchange exchange) -> {
            Optional<Document> snapshot = doSnapshot(exchange);
            try {
                if (snapshot.isPresent()) {
                    new SnapshotUtil(exchange).writeDocument(snapshot.get());
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                IronmqUtil.sendError(this, "defaultPredicate", e);
                return false;
            }
        };
    }

    protected abstract Optional<Document> doSnapshot(Exchange exchange);
}
