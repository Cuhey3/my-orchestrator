package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
public class CompletionConsumer extends ConsumerRouteBuilder {

    public CompletionConsumer() {
        routeUtil.completion();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.completion().consumeUri())
                .routeId(routeUtil.id())
                .filter((Exchange exchange) -> {
                    MongoUtil mongoUtil = new MongoUtil(exchange);
                    MessageUtil messageUtil = new MessageUtil(exchange);
                    try {
                        if (masterIsEmpty(messageUtil)) {
                            return snapshotSaveToMaster(exchange);
                        } else if (masterIsValid(exchange)
                                && snapshotSaveToMaster(exchange)
                                && enableDiff(exchange)) {
                            return true;
                        } else {
                            mongoUtil.disableDocument();
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mongoUtil.disableDocument();
                        return false;
                    }
                })
                .to(ironmqUtil.changed().postUri());
    }

    public boolean masterIsEmpty(MessageUtil messageUtil) {
        String comparedMasterId = messageUtil.get("compared_master_id");
        return comparedMasterId.equals(SenseType.EMPTY.expression());
    }

    public boolean masterIsValid(Exchange exchange) throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        MongoUtil mongoUtil = new MongoUtil(exchange);
        String comparedMasterId = messageUtil.get("compared_master_id");
        Optional<Document> findLatest = mongoUtil.master().findLatest();
        if (findLatest.isPresent()) {
            ObjectId objectId = findLatest.get().get("_id", ObjectId.class);
            boolean equals = objectId.toHexString().equals(comparedMasterId);
            if (equals) {
                System.out.println("master is valid.");
                return true;
            } else {
                System.out.println("master is not valid.");
                return false;
            }
        } else {
            System.out.println("master is not valid.");
            return false;
        }
    }

    public boolean snapshotSaveToMaster(Exchange exchange) throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        MongoUtil mongoUtil = new MongoUtil(exchange);
        String snapshotId = messageUtil.get("snapshot_id");
        Optional<Document> snapshotOptional
                = mongoUtil.snapshot().findById(snapshotId);
        if (snapshotOptional.isPresent()) {
            String masterId
                    = mongoUtil.master().insertOne(snapshotOptional.get());
            messageUtil.updateMessage("master_id", masterId);
            return true;
        } else {
            return false;
        }
    }

    public boolean enableDiff(Exchange exchange) throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        MongoUtil mongoUtil = new MongoUtil(exchange).diff();
        String diffId = messageUtil.get("diff_id");
        Optional<Document> findById = mongoUtil.findById(diffId);
        if (findById.isPresent()) {
            Document diff = findById.get();
            if (diff.containsKey("enable")) {
                System.out.println("diff is already enabled.");
                return false;
            } else {
                diff.append("enable", true);
                mongoUtil.replaceOne(diff);
                return true;
            }
        } else {
            System.out.println("diff is not found.");
            return false;
        }
    }
}
