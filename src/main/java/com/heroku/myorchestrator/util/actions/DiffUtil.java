package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.util.MessageUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class DiffUtil extends ActionUtil {

    public DiffUtil(Exchange exchange) {
        super(exchange);
        type(ActionType.DIFF);
    }

    public boolean enableDiff() {
        try {
            if (diffIdIsValid()) {
                Optional<Document> findById = loadDocument();
                if (findById.isPresent()) {
                    Document diff = findById.get();
                    if (!diff.get("enable", Boolean.class)) {
                        Document query = new Document("_id", diff.get("_id"));
                        Document updateDocument = new Document(
                                "$set", new Document("enable", true));
                        this.collection().updateOne(query, updateDocument);
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
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

    public boolean diffIdIsValid() {
        return MessageUtil.get(exchange, "diff_id", String.class) != null;
    }

    @Override
    public void writeDocument(Document document) throws Exception {
        document.append("enable", false);
        this.insertOne(document);
        message().writeObjectId(type.expression() + "_id", document);
    }
}
