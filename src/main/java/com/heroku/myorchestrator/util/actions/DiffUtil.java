package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import org.apache.camel.Exchange;

public class DiffUtil extends ActionUtil {

    public DiffUtil(Exchange exchange) {
        this.exchange = exchange;
        this.type = ActionType.DIFF;
    }
}
