package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.ironmq.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class TestSnapshotConsumer extends ConsumerRouteBuilder {

    public TestSnapshotConsumer() {
        setKind(Kind.foo);
        consumerUtil.snapshot();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.snapshot().consumeUri())
                .routeId(consumerUtil.id())
                .filter(consumerUtil.camelBatchComplete())
                .process((Exchange exchange) -> {
                    Document document = doSnapshot(new Document());
                    new SnapshotUtil(exchange).saveDocument(document)
                            .updateMessage(document);
                })
                .to(ironmqUtil.diff().postUri());
    }

    private Document doSnapshot(Document document) {
        document.append("foo", "bar")
                .append("minute_three",
                        Math.round(Integer.parseInt(new SimpleDateFormat("mm")
                                .format(new Date())) / 3));
        return document;
    }
}
