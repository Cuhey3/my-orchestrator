package com.heroku.myorchestrator.util;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

public class IronmqUtil {

    private static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    public static String consumeQueueUri(String queue, int timeout) {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                queue, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public static String consumeQueueUri(String type, String kind, int timeout) {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                type + "_" + kind, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public static String postQueueUri(String queue) {
        return String.format("ironmq:%s?client=%s",
                queue, IRONMQ_CLIENT_BEAN_NAME);
    }

    public static String postQueueUri(String type, String kind) {
        return String.format("ironmq:%s?client=%s",
                type + "_" + kind, IRONMQ_CLIENT_BEAN_NAME);
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

    private IronmqUtil() {
    }
}
