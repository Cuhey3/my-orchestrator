package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class ActionUtil extends MongoUtil {

    protected Exchange exchange;
    private final MessageUtil messageUtil;

    public ActionUtil(Exchange exchange) {
        super(exchange);
        this.exchange = exchange;
        this.messageUtil = new MessageUtil(exchange);
    }

    public Optional<Document> loadDocument() throws Exception {
        return findById(message().getMessage());
    }

    public void writeDocument(Document document) throws Exception {
        this.insertOne(document);
        message().writeObjectId(type.expression() + "_id", document);
    }

    public MessageUtil message() {
        return this.messageUtil;
    }
}
