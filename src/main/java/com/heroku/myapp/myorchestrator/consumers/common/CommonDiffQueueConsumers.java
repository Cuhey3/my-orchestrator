package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.DiffQueueConsumer;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonDiffQueueConsumers {

    @Autowired
    public CommonDiffQueueConsumers(CamelContext context) {
        try {
            for (Kind kind : Kind.values()) {
                if (kind.isUsedCommonDiffRoute()) {
                    context.addRoutes(new DiffQueueConsumer(kind) {
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("common diff consumers initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }
}
