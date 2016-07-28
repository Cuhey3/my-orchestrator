package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import com.heroku.myorchestrator.util.consumers.specific.SeiyuUtil;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiffSeiyuRoutes {

    @Autowired
    public DiffSeiyuRoutes(CamelContext context) throws Exception {
        for (SeiyuUtil.SeiyuKind seiyuKind : SeiyuUtil.SeiyuKind.values()) {
            context.addRoutes(new DiffRouteBuilder(seiyuKind.kind()) {
            });
        }
    }
}
