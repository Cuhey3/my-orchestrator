package com.heroku.myorchestrator.util;

import org.apache.camel.Exchange;
import org.bson.Document;

public class DiffUtil {

    private final Exchange exchange;

    public DiffUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public DiffUtil saveDocument(Document document) throws Exception {
        new MongoUtil(exchange).type("diff").insertOne(document);
        return this;
    }

    public DiffUtil updateMessage(Document document) {
        new MessageUtil(exchange).writeObjectId("diff_id", document);
        return this;
    }
}
