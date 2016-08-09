package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.exceptions.MessageElementNotFoundException;
import com.heroku.myorchestrator.exceptions.MessageNotSetException;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public class MessageUtil {

    public static Map getMessage(Exchange ex) {
        return ofNullable(ex.getIn().getBody(Map.class))
                .orElseThrow(() -> new MessageNotSetException());
    }

    public static Optional<String> getKind(Exchange ex) {
        return get(ex, "kind", String.class);
    }

    private static <T> Optional<T> get(Exchange ex, String key, Class<T> clazz) {
        return ofNullable(clazz.cast(getMessage(ex).get(key)));
    }

    public static Predicate loadAffect() {
        return (Exchange ex) -> {
            Optional<List> affect = get(ex, "affect", List.class);
            if (affect.isPresent() && !affect.get().isEmpty()) {
                ex.getIn().setBody(affect.get());
                return true;
            } else {
                return false;
            }
        };
    }

    public static Predicate messageKindIs(Kind kind) {
        return (Exchange ex)
                -> {
            Optional<String> k = getKind(ex);
            return k.isPresent() && k.get().equals(kind.expression());
        };
    }

    public static Predicate messageKindContains(String str) {
        return (Exchange ex)
                -> {
            Optional<String> k = getKind(ex);
            return k.isPresent() && k.get().contains(str);
        };
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
        return ofNullable(exchange.getIn().getBody(Map.class))
                .orElseThrow(() -> new MessageNotSetException());
    }

    public void writeObjectId(String key, Document document) {
        updateMessage(key, DocumentUtil.objectIdHexString(document));
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        return ofNullable(clazz.cast(getMessage().get(key)));
    }

    public <T> T getOrElseThrow(String key, Class<T> clazz) {
        return get(key, clazz)
                .orElseThrow(() -> new MessageElementNotFoundException());
    }

    public Optional<String> get(String key) {
        return get(key, String.class);
    }

    public String getOrElseThrow(String key) {
        return getOrElseThrow(key, String.class);
    }

    public boolean getBool(String key) {
        return get(key, Boolean.class).orElse(false);
    }
}
