package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MongoUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MasterUtil extends ActionUtil {

    private final SnapshotUtil snapshotUtil;

    public MasterUtil(Exchange exchange) {
        super(exchange);
        this.target(MongoTarget.MASTER);
        snapshotUtil = new SnapshotUtil(exchange);
    }

    @Override
    public MasterUtil useDummy() {
        super.useDummy();
        this.snapshotUtil.useDummy();
        return this;
    }

    public boolean comparedIsEmpty() {
        return SenseType.EMPTY.expression()
                .equals(message().get("compared_master_id"));
    }

    public boolean comparedIsValid() {
        try {
            return MongoUtil.getObjectIdHexString(findLatest().get())
                    .equals(message().get("compared_master_id"));
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "comparedIsValid", exchange, e);
            return false;
        }
    }

    public boolean snapshotSaveToMaster() {
        try {
            this.writeDocument(snapshotUtil.loadDocument().get());
            return true;
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "snapshotSaveToMaster", exchange, e);
            return false;
        }
    }

    public Optional<Document> latestJoinAll(Kind kind1, Kind kind2) {
        Kind kind0 = this.kind;
        try {
            Optional<Document> kind1Optional = this.kind(kind1).findLatest();
            Optional<Document> kind2Optional = this.kind(kind2).findLatest();
            if (kind1Optional.isPresent() && kind2Optional.isPresent()) {
                List result = new ArrayList<>();
                result.addAll(kind1Optional.get().get("data", List.class));
                result.addAll(kind2Optional.get().get("data", List.class));
                return Optional.ofNullable(new Document("data", result));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "latestJoinAll", exchange, e);
            return Optional.empty();
        } finally {
            this.kind(kind0);
        }
    }
}
