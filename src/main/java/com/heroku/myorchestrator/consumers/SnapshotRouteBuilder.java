package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;

public abstract class SnapshotRouteBuilder extends ConsumerRouteBuilder {

    public SnapshotRouteBuilder() {
        routeUtil.snapshot();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.snapshot().consumeUri())
                .routeId(routeUtil.id())
                .filter(routeUtil.camelBatchComplete())
                .process(defaultProcessor())
                .to(ironmqUtil.diff().postUri());
    }

    protected Processor defaultProcessor() {
        return (Exchange exchange) -> {
            Document document = doSnapshot(exchange, new Document());
            new SnapshotUtil(exchange)
                    .saveDocument(document)
                    .updateMessage(document);
        };
    }

    protected abstract Document doSnapshot(Exchange exchange, Document document) throws Exception;

}
