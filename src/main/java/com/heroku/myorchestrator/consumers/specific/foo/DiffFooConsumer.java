package com.heroku.myorchestrator.consumers.specific.foo;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.Objects;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

//@Component
public class DiffFooConsumer extends ConsumerRouteBuilder {

    public DiffFooConsumer() {
        kind(Kind.foo);
        route().diff();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().diff().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter((Exchange exchange) -> {
                    try {
                        Optional<Document> snapshotOptional
                                = new SnapshotUtil(exchange).loadDocument();
                        Optional<Document> masterOptional
                                = new MasterUtil(exchange).findLatest();
                        if (!snapshotOptional.isPresent()) {
                            return false;
                        }
                        if (!masterOptional.isPresent()) {
                            masterIsEmptyLogic(exchange);
                            return true;
                        }
                        Document snapshot
                                = snapshotOptional.orElse(new Document());
                        Document master = masterOptional.orElse(new Document());
                        Optional<Document> diffOptional
                                = compareLogic(master, snapshot);
                        if (!diffOptional.isPresent()) {
                            System.out.println("not updated...");
                            return false;
                        } else {
                            Document diff = diffOptional.get();
                            MessageUtil.writeObjectId(exchange,
                                    "compared_master_id", master);
                            new DiffUtil(exchange)
                                    .insert(diff)
                                    .updateMessage(diff);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .to(ironmq().completion().postUri());
    }

    public void masterIsEmptyLogic(Exchange exchange) {
        MessageUtil.updateMessage(exchange, "compared_master_id",
                SenseType.EMPTY.expression());
        System.out.println(this.kind.expression() + " master is empty.");
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
