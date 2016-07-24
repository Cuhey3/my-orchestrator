package com.heroku.myorchestrator.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class CombinedRequester extends ConsumerRouteBuilder {

    public CombinedRequester() {
        ironmq().snapshot().kind(Kind.seiyu_category_members);
    }

    @Override
    public void configure() throws Exception {
        from("timer:initialize_seiyu_category_members?repeatCount=1")
                .filter((Exchange exchange) -> {
                    boolean flag;
                    try {
                        flag = !new MasterUtil(exchange)
                                .findLatest().isPresent();
                    } catch (Exception e) {
                        flag = true;
                    }
                    if (flag) {
                        System.out.println("initialize...");
                    }
                    return flag;
                })
                .setBody().constant("{\"kind\":\"seiyu_category_members\"}")
                .to(ironmq().postUri());
    }
}
