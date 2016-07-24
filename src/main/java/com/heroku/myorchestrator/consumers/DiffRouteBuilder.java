package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class DiffRouteBuilder extends ConsumerRouteBuilder {

    public DiffRouteBuilder() {
        route().diff();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().diff().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(comparePredicate())
                .to(ironmq().completion().postUri());
    }

    public abstract Optional<Document> calculateDiff(Document master, Document snapshot);

    public void doWhenMasterIsEmpty(Exchange exchange) {
        new DiffUtil(exchange)
                .updateMessageComparedId(SenseType.EMPTY.expression());
    }

    public Predicate comparePredicate() {
        return (Exchange exchange) -> {
            Optional<Document> optSnapshot, optMaster, optDiff;
            try {
                optSnapshot = new SnapshotUtil(exchange).loadDocument();
                if (!optSnapshot.isPresent()) {
                    return false;
                }
                optMaster = new MasterUtil(exchange).findLatest();
                if (!optMaster.isPresent()) {
                    doWhenMasterIsEmpty(exchange);
                    return true;
                }
                Document master = optMaster.get();
                optDiff = calculateDiff(master, optSnapshot.get());
                if (optDiff.isPresent()) {
                    new DiffUtil(exchange).updateMessageComparedId(master)
                            .write(optDiff.get());
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };
    }
}
