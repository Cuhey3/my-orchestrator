package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.bson.types.ObjectId;

public class DiffUtil extends ActionUtil {

    public DiffUtil(Exchange exchange) {
        super(exchange);
        type(ActionType.DIFF);
    }

    public boolean enableDiff() throws Exception {
        Optional<Document> findById = loadDocument();
        if (findById.isPresent()) {
            Document diff = findById.get();
            if (!diff.get("enable", boolean.class)) {
                System.out.println("diff is already enabled.");
                return false;
            } else {
                ObjectId objectId
                        = new ObjectId(MongoUtil.getObjectIdHexString(diff));
                this.collection().updateOne(
                        new Document().append("_id", objectId),
                        new Document().append("enable", true));
                return true;
            }
        } else {
            System.out.println("diff is not found.");
            return false;
        }
    }

    public DiffUtil updateMessageComparedId(String id) {
        message().updateMessage("compared_master_id", id);
        return this;
    }

    public DiffUtil updateMessageComparedId(Document document) {
        message().writeObjectId("compared_master_id", document);
        return this;
    }
}
