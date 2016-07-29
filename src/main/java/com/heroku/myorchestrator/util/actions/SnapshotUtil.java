package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import org.apache.camel.Exchange;

public class SnapshotUtil extends ActionUtil {

    public SnapshotUtil(Exchange exchange) {
        super(exchange);
        this.target(MongoTarget.SNAPSHOT);
    }
}
