package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TestDiffConsumer extends RouteBuilder {

  @Autowired
  ApplicationContext applicationContext;

  @Override
  public void configure() throws Exception {
    from(consumeQueueUri("test_diff", 60))
            .filter(simple("${exchangeProperty.CamelBatchComplete}"))
            .process((Exchange exchange) -> {
              Map body = exchange.getIn().getBody(Map.class);
              MongoUtil mongoUtil = new MongoUtil(applicationContext);
              Document document = mongoUtil.findById("snapshot", "foo", body);
              System.out.println("in diff consumer: " + document);
            })
            .to(postQueueUri("test_complete"));
  }

}
