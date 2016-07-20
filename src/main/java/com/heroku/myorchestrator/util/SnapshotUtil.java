package com.heroku.myorchestrator.util;

import org.apache.camel.Exchange;
import org.bson.Document;

public class SnapshotUtil {

    private final Exchange exchange;
    private final String collectionKind;

    public SnapshotUtil(Exchange exchange, String collectionKind) {
        this.exchange = exchange;
        this.collectionKind = collectionKind;
    }

    public SnapshotUtil saveDocument(Document document) {
        new MongoUtil(exchange).insertOne("snapshot", collectionKind, document);
        return this;
    }

    public SnapshotUtil updateMessage(Document document) {
        new MessageUtil(exchange).writeObjectId("snapshot_id", document);
        return this;
    }
}
