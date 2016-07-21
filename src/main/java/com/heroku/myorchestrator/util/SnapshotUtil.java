package com.heroku.myorchestrator.util;

import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class SnapshotUtil {

    private final Exchange exchange;

    public SnapshotUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public SnapshotUtil saveDocument(Document document) throws Exception {
        new MongoUtil(exchange).type("snapshot").insertOne(document);
        return this;
    }

    public SnapshotUtil updateMessage(Document document) {
        new MessageUtil(exchange).writeObjectId("snapshot_id", document);
        return this;
    }

    public Optional<Document> loadDocument() throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        Map body = messageUtil.getMessage();
        return new MongoUtil(exchange).type("snapshot").findById(body);
    }
}
