package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class MasterUtil extends ActionUtil {

    private final SnapshotUtil snapshotUtil;

    public MasterUtil(Exchange exchange) {
        super(exchange);
        this.type(ActionType.MASTER);
        snapshotUtil = new SnapshotUtil(exchange);
    }

    public boolean comparedIsEmpty() {
        return SenseType.EMPTY.expression()
                .equals(message().get("compared_master_id"));
    }

    public boolean comparedIsValid() throws Exception {
        Optional<Document> latest = findLatest();
        if (latest.isPresent()
                && MongoUtil.getObjectIdHexString(latest.get())
                .equals(message().get("compared_master_id"))) {
            System.out.println("master is valid.");
            return true;
        }
        System.out.println("master is not valid.");
        return false;
    }

    public boolean snapshotSaveToMaster() throws Exception {
        Optional<Document> snapshotOptional = snapshotUtil.loadDocument();
        if (snapshotOptional.isPresent()) {
            this.write(snapshotOptional.get());
            return true;
        } else {
            return false;
        }
    }
}
