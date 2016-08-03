package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MongoUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import static com.heroku.myorchestrator.util.content.DocumentUtil.getData;
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

    public boolean isSkipDiff() {
        return message().getBool("skip_diff");
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

    public boolean isSkipValidation() {
        return isSkipDiff() || comparedIsEmpty();
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
            List result = new ArrayList<>();
            result.addAll(getData(getLatest(kind1)));
            result.addAll(getData(getLatest(kind2)));
            return new DocumentUtil(result).nullable();
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "latestJoinAll", exchange, e);
            return Optional.empty();
        } finally {
            this.kind(kind0);
        }
    }
}
