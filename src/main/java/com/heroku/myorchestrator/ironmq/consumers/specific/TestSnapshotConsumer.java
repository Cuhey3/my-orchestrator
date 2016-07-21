package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.util.IronmqUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class TestSnapshotConsumer extends RouteBuilder {

    private final IronmqUtil ironmqUtil = new IronmqUtil().kind("foo");

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.snapshot().consumeUri())
                .filter(simple("${exchangeProperty.CamelBatchComplete}"))
                .process((Exchange exchange) -> {
                    Document document = new Document();
                    doSnapshot(document);
                    new SnapshotUtil(exchange)
                            .saveDocument(document)
                            .updateMessage(document);
                })
                .to(ironmqUtil.diff().postUri());
    }

    private void doSnapshot(Document document) {
        document.append("foo", "bar")
                .append("minute_three",
                        Math.round(Integer.parseInt(
                                new SimpleDateFormat("mm")
                                .format(new Date())) / 3));
    }
}
