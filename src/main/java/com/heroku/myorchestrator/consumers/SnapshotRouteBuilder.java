package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.consumers.KindUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class SnapshotRouteBuilder extends ConsumerRouteBuilder {

    public SnapshotRouteBuilder() {
        route().snapshot();
        kind(KindUtil.findKindByClassName(this));
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().snapshot().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(defaultPredicate())
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
                IronmqUtil.sendError(this.getClass(), "defaultPredicate", exchange, e);
                return false;
            }
        };
    }

    protected abstract Optional<Document> doSnapshot(Exchange exchange);
}
