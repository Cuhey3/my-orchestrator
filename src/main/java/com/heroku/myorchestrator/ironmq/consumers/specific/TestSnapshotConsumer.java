package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class TestSnapshotConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("test_snapshot", 60))
                .filter(simple("${exchangeProperty.CamelBatchComplete}"))
                .process((Exchange exchange) -> {
                    Document document = new Document();
                    doSnapshot(document);
                    new SnapshotUtil(exchange)
                            .saveDocument(document)
                            .updateMessage(document);
                })
                .to(postQueueUri("test_diff"));
    }

    private void doSnapshot(Document document) {
        document.append("foo", "bar")
                .append("minute_three",
                        Math.round(Integer.parseInt(
                                new SimpleDateFormat("mm")
                                .format(new Date())) / 3));
    }
}
