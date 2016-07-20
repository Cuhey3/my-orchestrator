package com.heroku.myorchestrator.util;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

public class IronmqUtil {

    public static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    public static String consumeQueueUri(String queue, int timeout) {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                queue, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public static String consumeQueueUri(String kind, String collectionKind, int timeout) {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                kind + "_" + collectionKind, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public static String postQueueUri(String queue) {
        return String.format("ironmq:%s?client=%s",
                queue, IRONMQ_CLIENT_BEAN_NAME);
    }

    public static String postQueueUri(String kind, String collectionKind) {
        return String.format("ironmq:%s?client=%s",
                kind + "_" + collectionKind, IRONMQ_CLIENT_BEAN_NAME);
    }

    public static Expression defaultPostQueueUri() {
        return new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                Map body = exchange.getIn().getBody(Map.class);
                return (T) String.format("ironmq:%s?client=%s",
                        body.get("queue"), IRONMQ_CLIENT_BEAN_NAME);
            }
        };
    }
}
