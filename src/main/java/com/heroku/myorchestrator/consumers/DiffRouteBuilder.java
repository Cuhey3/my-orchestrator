package com.heroku.myorchestrator.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.consumers.KindUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.bson.Document;

public abstract class DiffRouteBuilder extends ConsumerRouteBuilder {

    protected String diffKey;

    public DiffRouteBuilder() {
        route().diff();
        kind(KindUtil.findKindByClassName(this));
    }

    public DiffRouteBuilder(Kind kind) {
        route().diff();
        kind(kind);
        this.diffKey = kind.diffKey();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().diff().consumeUri())
                .routeId(route().id())
                .filter(route().camelBatchComplete())
                .filter(comparePredicate())
                .to(ironmq().completion().postUri());
    }

    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(master, snapshot, diffKey);
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
                optMaster = new MasterUtil(exchange).findLatest();
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
                } else if (DocumentUtil.checkNotFilled(exchange, master)) {
                    new DiffUtil(exchange).updateMessageComparedId(master);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                IronmqUtil.sendError(this.getClass(), "comparePredicate", exchange, e);
                return false;
            }
        };
    }
}
