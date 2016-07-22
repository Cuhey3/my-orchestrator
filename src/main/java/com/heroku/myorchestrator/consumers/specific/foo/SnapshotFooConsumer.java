package com.heroku.myorchestrator.consumers.specific.foo;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFooConsumer extends SnapshotRouteBuilder {

    public SnapshotFooConsumer() {
        setKind(Kind.foo);
    }

    @Override
    protected Document doSnapshot(Document document) throws Exception {
        document.append("foo", "bar")
                .append("minute_three",
                        Math.round(Integer.parseInt(new SimpleDateFormat("mm")
                                .format(new Date())) / 3));
        return document;
    }
}