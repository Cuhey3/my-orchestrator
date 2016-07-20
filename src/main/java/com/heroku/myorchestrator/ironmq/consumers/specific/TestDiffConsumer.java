package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;
import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MessageUtil;
import java.util.Objects;
import java.util.Optional;

@Component
public class TestDiffConsumer extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("test_diff", 60))
                .filter(simple("${exchangeProperty.CamelBatchComplete}"))
                .filter((Exchange exchange) -> {
                    MessageUtil messageUtil = new MessageUtil(exchange);
                    Map body = messageUtil.getMessage();
                    MongoUtil mongoUtil = new MongoUtil(exchange);
                    Document snapshot
                            = mongoUtil.findById("snapshot", "foo", body)
                            .orElse(new Document());
                    Document master = mongoUtil.findLatest("master", "foo")
                            .orElse(new Document());
                    if (snapshot.isEmpty()) {
                        return false;
                    }
                    if (master.isEmpty()) {
                        masterIsEmptyLogic();
                        //return false;
                    }
                    //messageUtil.writeObjectId("compared_master_id", master);
                    Document diff = compareLogic(master, snapshot).get();
                    if (diff == null) {
                        System.out.println("not updated...");
                        return false;
                    } else {
                        mongoUtil.insertOne("diff", "foo", diff);
                        messageUtil.writeObjectId("diff_id", diff);
                        return true;
                    }
                })
                .to(postQueueUri("test_complete"));
    }

    public void masterIsEmptyLogic() {
        System.out.println("master is empty.");
    }

    public Optional<Document> compareLogic(Document master, Document snapshot) {
        System.out.println("comparing... " + master + " to " + snapshot);
        Integer snapshotMinuteThree
                = snapshot.get("minute_three", Integer.class);
        if (!Objects.equals(
                master.get("minute_three", Integer.class),
                snapshotMinuteThree)) {
            System.out.println("updated!" + snapshot);
            Document diff = new Document()
                    .append("newValue", snapshotMinuteThree);
            return Optional.ofNullable(diff);
        } else {
            return Optional.empty();
        }
    }
}
