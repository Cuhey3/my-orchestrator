package com.heroku.myorchestrator.util;

import java.util.Map;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MessageUtil {

    public static Map getMessage(Exchange ex) {
        return ex.getIn().getBody(Map.class);
    }

    public static String getKind(Exchange ex) {
        return (String) getMessage(ex).get("kind");
    }

    private final Exchange exchange;

    public MessageUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public void updateMessage(String key, Object value) {
        Map message = getMessage();
        message.put(key, value);
        exchange.getIn().setBody(message, String.class);
    }

    public static void updateMessage(Exchange exchange, String key, Object value) {
        Map message = getMessage(exchange);
        message.put(key, value);
        exchange.getIn().setBody(message, String.class);
    }

    public Map getMessage() {
        return exchange.getIn().getBody(Map.class);
    }

    public void writeObjectId(String key, Document document) {
        String objectIdHexString = MongoUtil.getObjectIdHexString(document);
        updateMessage(key, objectIdHexString);
    }

    public static void writeObjectId(Exchange exchange, String key, Document document) {
        String objectIdHexString = MongoUtil.getObjectIdHexString(document);
        updateMessage(exchange, key, objectIdHexString);
    }

    public static <T> T get(Exchange exchange, String key, Class<T> clazz) {
        Map message = MessageUtil.getMessage(exchange);
        return (T) message.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Map message = getMessage();
        return (T) message.get(key);
    }

    public String get(String key) {
        Map message = getMessage();
        return (String) message.get(key);
    }
}
