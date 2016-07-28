package com.heroku.myorchestrator.consumers.deprecated.foo;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

//@Component
public class SnapshotFooConsumer extends SnapshotRouteBuilder {

    public SnapshotFooConsumer() {
        kind(Kind.foo);
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        document.append("foo", "bar")
                .append("minute_three",
                        Math.round(Integer.parseInt(new SimpleDateFormat("mm")
                                .format(new Date())) / 3));
        return Optional.ofNullable(document);
    }
}
