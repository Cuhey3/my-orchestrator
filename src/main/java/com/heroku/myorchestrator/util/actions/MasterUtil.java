package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MasterUtil extends ActionUtil{

    public MasterUtil(Exchange exchange) {
        this.exchange = exchange;
        this.type = ActionType.MASTER;
    }

    public Optional<Document> loadLatestDocument() throws Exception {
        return new MongoUtil(exchange).type("master").findLatest();
    }
}
