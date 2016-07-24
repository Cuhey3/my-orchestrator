package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;

public abstract class SnapshotRouteBuilder extends ConsumerRouteBuilder {

    public SnapshotRouteBuilder() {
        route().snapshot();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().snapshot().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .process(defaultProcessor())
                .to(ironmq().diff().postUri());
    }

    protected Processor defaultProcessor() {
        return (Exchange exchange) -> {
            new SnapshotUtil(exchange)
                    .write(doSnapshot(exchange, new Document()));
        };
    }

    protected abstract Document doSnapshot(Exchange exchange, Document document) throws Exception;

}
