package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.ironmq.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.Objects;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class TestDiffConsumer extends ConsumerRouteBuilder {

    public TestDiffConsumer() {
        kind = "foo";
        ironmqUtil.kind(kind);
        consumerUtil.diff().kind(kind);
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.diff().consumeUri())
                .routeId(consumerUtil.id())
                .filter(consumerUtil.camelBatchComplete())
                .filter((Exchange exchange) -> {
                    try {
                        Optional<Document> snapshotOptional
                                = new SnapshotUtil(exchange).loadDocument();
                        Optional<Document> masterOptional
                                = new MasterUtil(exchange).loadLatestDocument();
                        if (!snapshotOptional.isPresent()) {
                            return false;
                        }
                        if (!masterOptional.isPresent()) {
                            masterIsEmptyLogic();
                            //return false;
                        }
                        Document snapshot = snapshotOptional.orElse(new Document());
                        Document master = masterOptional.orElse(new Document());
                        //messageUtil.writeObjectId("compared_master_id", master);
                        Document diff = compareLogic(master, snapshot).get();
                        if (diff == null) {
                            System.out.println("not updated...");
                            return false;
                        } else {
                            new DiffUtil(exchange)
                                    .saveDocument(diff)
                                    .updateMessage(diff);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .to(ironmqUtil.completion().postUri());
    }

    public void masterIsEmptyLogic() {
        System.out.println("master is empty.");
    }

    public Optional<Document> compareLogic(Document master, Document snapshot) {
        System.out.println("comparing... " + master + " to " + snapshot);
        Integer snapshotMinuteThree
                = snapshot.get("minute_three", Integer.class);
        if (!Objects.equals(master.get("minute_three", Integer.class),
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
