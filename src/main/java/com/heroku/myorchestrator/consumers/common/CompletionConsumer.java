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
                .choice()
                .when((Exchange exchange)
                        -> new MasterUtil(exchange).comparedIsEmpty())
                .to("direct:snapshotSaveToMaster")
                .otherwise()
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).comparedIsValid())
                .to("direct:snapshotSaveToMaster");
        
        from("direct:snapshotSaveToMaster")
                .routeId("snapshot_save_to_master")
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster())
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff())
                .to(ironmq().changed().postUri());
    }
}
