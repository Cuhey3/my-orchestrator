package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MongoUtil;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MasterUtil extends ActionUtil {

    private final SnapshotUtil snapshotUtil;

    public MasterUtil(Exchange exchange) {
        super(exchange);
        this.type(ActionType.MASTER);
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
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean snapshotSaveToMaster() {
        try {
            this.writeDocument(snapshotUtil.loadDocument().get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
