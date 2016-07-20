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
            .filter((Exchange exchange) -> {
              MessageUtil messageUtil = new MessageUtil(exchange);
              Map body = messageUtil.getMessage();
              MongoUtil mongoUtil = new MongoUtil(applicationContext);
              Document snapshot = mongoUtil.findById("snapshot", "foo", body)
                      .orElse(new Document());
              Document master = mongoUtil.findLatest("master", "foo")
                      .orElse(new Document());
              if (snapshot.isEmpty()) {
                return false;
              }
              if (master.isEmpty()) {
                System.out.println("master is empty.");
              } else {
                String masterObjectIdHexString
                        = mongoUtil.getObjectIdHexString(master);
                messageUtil.updateMessage(
                        "compared_master_id", masterObjectIdHexString);
              }
              System.out.println("comparing... " + master + " to " + snapshot);
              Integer snapshotMinuteThree = snapshot.get("minute_three", Integer.class);
              if (master.get("minute_three", Integer.class)
                      != snapshotMinuteThree) {
                System.out.println("updated!" + snapshot);
                Document diff = new Document().append("newValue", snapshotMinuteThree);
                String diffObjectIdHexString
                        = mongoUtil.insertOne("diff", "foo", diff);
                messageUtil.updateMessage("diff_id", diffObjectIdHexString);
                return true;
              } else {
                System.out.println("not updated...");
                return false;
              }
            })
            .to(postQueueUri("test_complete"));
  }

}
