package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class DiffRouteBuilder extends ConsumerRouteBuilder {

    public DiffRouteBuilder() {
        routeUtil.diff();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.diff().consumeUri())
                .routeId(routeUtil.id())
                .filter(routeUtil.camelBatchComplete())
                .filter(comparePredicate())
                .to(ironmqUtil.completion().postUri());
    }

    public abstract Optional<Document> calculateDiff(Document master, Document snapshot);

    public void doWhenMasterIsEmpty(Exchange exchange) {
        MessageUtil.updateMessage(exchange,
                "compared_master_id", SenseType.EMPTY.expression());
    }

    public Predicate comparePredicate() {
        return (Exchange exchange) -> {
            Optional<Document> optSnapshot, optMaster, optDiff;
            Document master, diff;
            try {
                optSnapshot = new SnapshotUtil(exchange).loadDocument();
                if (!optSnapshot.isPresent()) {
                    return false;
                }
                optMaster = new MasterUtil(exchange).loadLatestDocument();
                if (!optMaster.isPresent()) {
                    doWhenMasterIsEmpty(exchange);
                    return true;
                }
                master = optMaster.get();
                optDiff = calculateDiff(master, optSnapshot.get());
                if (optDiff.isPresent()) {
                    diff = optDiff.get();
                    MessageUtil.writeObjectId(exchange,
                            "compared_master_id", master);
                    new DiffUtil(exchange).saveDocument(diff)
                            .updateMessage(diff);
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
