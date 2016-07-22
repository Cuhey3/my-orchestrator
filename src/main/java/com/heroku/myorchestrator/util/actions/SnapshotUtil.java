package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import org.apache.camel.Exchange;

public class SnapshotUtil extends ActionUtil {

    public SnapshotUtil(Exchange exchange) {
        this.exchange = exchange;
        this.type = ActionType.SNAPSHOT;
    }
}