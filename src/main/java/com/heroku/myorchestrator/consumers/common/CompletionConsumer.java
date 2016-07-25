package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                .when((Exchange exchange) -> new MasterUtil(exchange).comparedIsEmpty())
                .to("direct:snapshotSaveToMaster")
                .otherwise()
                .filter((Exchange exchange) -> {
                    try {
                        return new MasterUtil(exchange).comparedIsValid();
                    } catch (Exception ex) {
                        Logger.getLogger(CompletionConsumer.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                })
                .to("direct:snapshotSaveToMaster");

        from("direct:snapshotSaveToMaster")
                .filter((Exchange exchange) -> {
                    try {
                        return new MasterUtil(exchange).snapshotSaveToMaster();
                    } catch (Exception ex) {
                        Logger.getLogger(CompletionConsumer.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                })
                .filter((Exchange exchange) -> {
                    try {
                        return new DiffUtil(exchange).enableDiff();
                    } catch (Exception ex) {
                        Logger.getLogger(CompletionConsumer.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    }
                })
                .to(ironmq().changed().postUri());
    }
}
