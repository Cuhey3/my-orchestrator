package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TestSnapshotConsumer extends RouteBuilder {

  @Autowired
  ApplicationContext applicationContext;

  @Override
  public void configure() throws Exception {
    from(consumeQueueUri("test_snapshot", 60))
            .filter(simple("${exchangeProperty.CamelBatchComplete}"))
            .process((Exchange exchange) -> {
              Document document = new Document().append("foo", "bar");
              String objectIdHexString
                      = new MongoUtil(applicationContext)
                      .insertOne("snapshot", "foo", document);
              new MessageUtil(exchange)
                      .updateMessage("snapshot_id", objectIdHexString);
            })
            .to(postQueueUri("test_diff"));

  }

}
