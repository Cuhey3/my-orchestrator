package com.heroku.myorchestrator.util;

import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MasterUtil {

    private final Exchange exchange;

    public MasterUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public Optional<Document> loadDocument() throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        Map body = messageUtil.getMessage();
        return new MongoUtil(exchange).type("master").findById(body);
    }

    public Optional<Document> loadLatestDocument() throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        Map body = messageUtil.getMessage();
        return new MongoUtil(exchange).type("master").findLatest();
    }
}
