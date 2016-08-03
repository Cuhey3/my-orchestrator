package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonDiffRoutes {

    @Autowired
    public CommonDiffRoutes(CamelContext context) {
        try {
            for (Kind kind : Kind.values()) {
                if (kind.useCommonDiffRoute()) {
                    context.addRoutes(new DiffRouteBuilder(kind, kind.diffKey()) {
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("common diff routes initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }
}
