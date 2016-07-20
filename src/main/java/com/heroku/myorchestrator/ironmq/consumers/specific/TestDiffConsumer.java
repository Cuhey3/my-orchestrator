package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MessageUtil;

@Component
public class TestDiffConsumer extends RouteBuilder {

  @Autowired
  ApplicationContext applicationContext;

  @Override
  public void configure() throws Exception {
    from(consumeQueueUri("test_diff", 60))
            .filter(simple("${exchangeProperty.CamelBatchComplete}"))
            .process((Exchange exchange) -> {
              Map body = new MessageUtil(exchange).getMessage();
              Document document
                      = new MongoUtil(applicationContext)
                      .findById("snapshot", "foo", body);
              System.out.println("in diff consumer: " + document);
            })
            .to(postQueueUri("test_complete"));
  }

}
