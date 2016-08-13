package com.heroku.myorchestrator.consumers;

import com.heroku.definitions.config.enumerate.Kind;
import com.heroku.definitions.config.enumerate.SenseType;
import com.heroku.definitions.util.MessageUtil;
import com.heroku.definitions.util.actions.DiffUtil;
import com.heroku.definitions.util.actions.MasterUtil;
import com.heroku.definitions.util.actions.SnapshotUtil;
import com.heroku.definitions.util.consumers.IronmqUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class DiffQueueConsumer extends QueueConsumer {

    protected String commonDiffKey;

    public DiffQueueConsumer() {
        route().diff();
        kind(Kind.findKindByClassName(this));
    }

    public DiffQueueConsumer(Kind kind) {
        route().diff();
        kind(kind);
        this.commonDiffKey = kind.commonDiffKey();
    }

    @Override
    public void configure() {
        from(ironmq().diff().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(comparePredicate())
                .to(ironmq().completionPostUri());
    }

    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(master, snapshot, commonDiffKey);
    }

    public void doWhenMasterIsEmpty(Exchange exchange) {
        new DiffUtil(exchange)
                .updateMessageComparedId(SenseType.EMPTY.expression());
    }

    public void doWhenSkipDiff(Exchange exchange) {
        // none
    }

    public Predicate comparePredicate() {
        return (Exchange exchange) -> {
            if (new MessageUtil(exchange).getBool("skip_diff")) {
                doWhenSkipDiff(exchange);
                return true;
            }
            Optional<Document> optSnapshot, optMaster, optDiff;
            try {
                optSnapshot = new SnapshotUtil(exchange).loadDocument();
                if (!optSnapshot.isPresent()) {
                    return false;
                }
                MasterUtil masterUtil = new MasterUtil(exchange);
                optMaster = masterUtil.optionalFind();
                if (!optMaster.isPresent()) {
                    doWhenMasterIsEmpty(exchange);
                    return true;
                }
                Document master = optMaster.get();
                optDiff = calculateDiff(master, optSnapshot.get());
                if (optDiff.isPresent()) {
                    new DiffUtil(exchange).updateMessageComparedId(master)
                            .writeDocument(optDiff.get());
                    return true;
                } else if (masterUtil.checkNotFilled(master)) {
                    new DiffUtil(exchange).updateMessageComparedId(master);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                IronmqUtil.sendError(this, "comparePredicate", e);
                return false;
            }
        };
    }
}
