package com.heroku.myorchestrator.util;

import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;

public class IronmqUtil {

  public static String consumeQueueUri(String queue, int timeout) {
    return String.format("ironmq:%s?client=myclient&timeout=%s", queue, timeout);
  }

  public static String consumeQueueUri(String kind, String collectionKind, int timeout) {
    return String.format("ironmq:%s?client=myclient&timeout=%s", kind + "_" + collectionKind, timeout);
  }

  public static String postQueueUri(String queue) {
    return String.format("ironmq:%s?client=myclient", queue);
  }

  public static String postQueueUri(String kind, String collectionKind) {
    return String.format("ironmq:%s?client=myclient", kind + "_" + collectionKind);
  }

  public static Expression defaultPostQueueUri() {
    return new Expression() {
      @Override
      public <T> T evaluate(Exchange exchange, Class<T> type) {
        Map body = exchange.getIn().getBody(Map.class);
        return (T) String.format("ironmq:%s?client=myclient", body.get("queue"));
      }
    };
  }
}
