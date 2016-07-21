package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class ActionUtil {

    protected Exchange exchange;
    protected ActionType type;

    public Optional<Document> loadDocument() throws Exception {
        Map body = new MessageUtil(exchange).getMessage();
        return new MongoUtil(exchange).type(type).findById(body);
    }

    public ActionUtil saveDocument(Document document) throws Exception {
        new MongoUtil(exchange).type(type).insertOne(document);
        return this;
    }

    public ActionUtil updateMessage(Document document) {
        new MessageUtil(exchange)
                .writeObjectId(type.expression() + "_id", document);
        return this;
    }

}
