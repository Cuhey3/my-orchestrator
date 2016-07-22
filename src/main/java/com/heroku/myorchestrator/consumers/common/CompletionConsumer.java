package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
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
                .to(ironmqUtil.changed().postUri());
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
