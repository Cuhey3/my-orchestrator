package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class CompletionConsumer extends ConsumerRouteBuilder {

    public CompletionConsumer() {
        route().completion();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().completion().consumeUri())
                .routeId(route().id())
                .filter((Exchange exchange) -> {
                    MasterUtil masterUtil = new MasterUtil(exchange);
                    try {
                        if (masterUtil.comparedIsEmpty()) {
                            return masterUtil.snapshotSaveToMaster();
                        } else if (masterUtil.comparedIsValid()
                                && masterUtil.snapshotSaveToMaster()
                                && new DiffUtil(exchange).enableDiff()) {
                            return true;
                        } else {
                            //new MongoUtil(exchange).disableDocument();
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //new MongoUtil(exchange).disableDocument();
                        return false;
                    }
                })
                .to(ironmq().changed().postUri());
    }
}
