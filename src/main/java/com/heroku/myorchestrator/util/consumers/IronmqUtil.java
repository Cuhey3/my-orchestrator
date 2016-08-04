package com.heroku.myorchestrator.util.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.QueueType;
import com.heroku.myorchestrator.util.MessageUtil;
import io.iron.ironmq.Client;
import java.io.IOException;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class IronmqUtil {

    private static final String IRONMQ_CLIENT_BEAN_NAME = "myironmq";

    public static Expression affectQueueUri() {
        return new Expression() {
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                String kindString = exchange.getIn().getBody(String.class);
                exchange.getIn().setBody(Kind.valueOf(kindString).preMessage());
                return (T) String.format("ironmq:%s?client=%s",
                        "snapshot_" + kindString, IRONMQ_CLIENT_BEAN_NAME);
            }
        };
    }

    public static void sendError(RouteBuilder rb, String method, Exception ex) {
        try {
            Client client = rb.getContext().getRegistry()
                    .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
            client.queue("exception_in").push(
                    new Date().toString()
                    + "\nClass: " + rb.getClass().getName()
                    + "\nmethod: " + method
                    + "\nException class: " + ex.getClass().getName()
                    + "\nmessage: " + ex.getMessage());
        } catch (IOException ex1) {
        }
    }

    public static Processor requestSnapshotProcess() {
        return (Exchange exchange) -> {
            Kind k = Kind.valueOf(MessageUtil.getKind(exchange));
            new IronmqUtil().snapshot().postMessage(exchange, k);
        };
    }

    private String type, kind;
    private int timeout;

    public IronmqUtil() {
        this.timeout = 60;
    }

    public IronmqUtil type(QueueType type) {
        this.type = type.expression();
        return this;
    }

    public IronmqUtil kind(Kind kind) {
        this.kind = kind.expression();
        return this;
    }

    public IronmqUtil kind(String kind) {
        this.kind = kind;
        return this;
    }

    public IronmqUtil timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public IronmqUtil snapshot() {
        this.type = QueueType.SNAPSHOT.expression();
        return this;
    }

    public IronmqUtil diff() {
        this.type = QueueType.DIFF.expression();
        return this;
    }

    public IronmqUtil completion() {
        this.type = QueueType.COMPLETION.expression();
        this.kind = "all";
        return this;
    }

    public IronmqUtil changed() {
        this.type = QueueType.CHANGED.expression();
        this.kind = "all";
        return this;
    }

    public IronmqUtil exception() {
        this.type = QueueType.EXCEPTION.expression();
        return this;
    }

    public void postMessage(Exchange exchange, Kind k) throws IOException {
        Client client = exchange.getContext().getRegistry()
                .lookupByNameAndType(IRONMQ_CLIENT_BEAN_NAME, Client.class);
        client.queue(this.type + "_" + k.expression())
                .push(k.preMessage());
    }

    public String consumeUri() {
        return String.format("ironmq:%s"
                + "?client=%s"
                + "&timeout=%s"
                + "&maxMessagesPerPoll=100",
                type + "_" + kind, IRONMQ_CLIENT_BEAN_NAME, timeout);
    }

    public String postUri() {
        return String.format("ironmq:%s?client=%s",
                type + "_" + kind, IRONMQ_CLIENT_BEAN_NAME);
    }
}
