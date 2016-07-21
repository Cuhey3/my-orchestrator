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

    public Map getMessage() {
        return exchange.getIn().getBody(Map.class);
    }

    public void writeObjectId(String key, Document document) {
        String objectIdHexString = MongoUtil.getObjectIdHexString(document);
        updateMessage(key, objectIdHexString);
    }
}
