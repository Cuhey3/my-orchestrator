package com.heroku.myorchestrator.ironmq;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

public class IronmqUtil {

    static String consumeQueueUri(String queue, boolean batchDelete) {
        return String.format("ironmq:%s?client=myclient&batchDelete=%s", queue, batchDelete);
    }

    static Expression defaultPostQueueUri() {
        return new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                Map body = exchange.getIn().getBody(Map.class);
                return (T) String.format("ironmq:%s?client=myclient", body.get("queue"));
            }
        };
    }
}
