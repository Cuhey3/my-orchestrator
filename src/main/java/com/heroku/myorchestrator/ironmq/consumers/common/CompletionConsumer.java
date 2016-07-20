package com.heroku.myorchestrator.ironmq.consumers.common;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CompletionConsumer extends RouteBuilder {

    final String consumeQueue = "completion_develop";
    final String toPostQueue = "changed_develop";

    @Override
    public void configure() throws Exception {
//        from(consumeQueueUri(consumeQueue, 60))
        from(consumeQueueUri("test_complete", 60))
                /*.process((Exchange exchange) -> {
                    Map body = exchange.getIn().getBody(Map.class);
                    String messageType = (String) body.get("message_type");
                    String comparedMasterId = (String) body.get("compared_master_id");
                    String snapshotId = (String) body.get("snapshot_id");
                    String diffId = (String) body.get("diff_id");
                    if (masterIsValid(messageType, comparedMasterId)
                            && snapshotSaveToMaster(messageType, snapshotId)
                            && saveDiff(messageType, diffId)) {
                        pushChanging(messageType);
                    } else {
                        cleanUpDiff(messageType);
                    }
                })*/
                .to(postQueueUri("test_changed"));
    }

    public boolean masterIsValid(String messageType, String comparedMasterId) {
        return true;
    }

    public boolean snapshotSaveToMaster(String messageType, String snapshotId) {
        return true;
    }

    public boolean saveDiff(String messageType, String diffId) {
        return true;
    }

    public void cleanUpDiff(String messageType) {

    }

    public void pushChanging(String messageType) {

    }
}
