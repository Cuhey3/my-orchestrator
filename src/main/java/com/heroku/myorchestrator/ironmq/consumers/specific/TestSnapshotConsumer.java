package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MongoUtil;
import com.mongodb.client.MongoCollection;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestSnapshotConsumer extends RouteBuilder {

  @Autowired
  MongoUtil mongoUtil;

  @Override
  public void configure() throws Exception {
    from(consumeQueueUri("test_snapshot", 60))
            .process((Exchange exchange) -> {
              MongoCollection<Document> collection = mongoUtil.getCollection("snapshot", "foo");
              collection.insertOne(new Document().append("foo", "bar").append("timestamp", new Date()));
              //mongoUtil.close();
            })
            .to(postQueueUri("test_diff"));

  }

}
