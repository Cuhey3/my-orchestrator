package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.List;
import java.util.Map;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public class MessageUtil {

    public static Map getMessage(Exchange ex) {
        return ex.getIn().getBody(Map.class);
    }

    public static String getKind(Exchange ex) {
        Map message = getMessage(ex);
        if (message == null || !message.containsKey("kind")) {
            return null;
        } else {
            return (String) getMessage(ex).get("kind");
        }
    }

    public static <T> T get(Exchange exchange, String key, Class<T> clazz) {
        return (T) MessageUtil.getMessage(exchange).get(key);
    }

    public static Predicate loadAffect() {
        return (Exchange ex) -> {
            List affect = MessageUtil.get(ex, "affect", List.class);
            if (affect != null && !affect.isEmpty()) {
                ex.getIn().setBody(affect);
                return true;
            } else {
                return false;
            }
        };
    }

    public static Predicate messageKindIs(Kind kind) {
        return (Exchange exchange1)
                -> MessageUtil.getKind(exchange1).equals(kind.expression());
    }

    public static Predicate messageKindContains(String str) {
        return (Exchange exchange1)
                -> MessageUtil.getKind(exchange1).contains(str);
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
        updateMessage(key, DocumentUtil.objectIdHexString(document));
    }

    public <T> T get(String key, Class<T> clazz) {
        return (T) getMessage().get(key);
    }

    public String get(String key) {
        return (String) getMessage().get(key);
    }

    public boolean getBool(String key) {
        return ofNullable((Boolean) getMessage().get(key)).orElse(false);
    }

    public boolean contains(String key) {
        return getMessage().containsKey(key);
    }
}
