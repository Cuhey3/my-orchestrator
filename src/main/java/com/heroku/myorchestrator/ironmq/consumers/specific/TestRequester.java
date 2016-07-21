package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.ironmq.consumers.ConsumerRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestRequester extends ConsumerRouteBuilder {

    private final String fscm = "female_seiyu_category_members";

    public TestRequester() {
        ironmqUtil.snapshot();
        consumerUtil.request();
    }

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=60s")
                .routeId(consumerUtil.kind("foo").id())
                .setBody()
                .constant("{\"kind\":\"foo\"}")
                .to(ironmqUtil.kind("foo").postUri());

        from("timer:female_seiyu_category_members?period=20m&delay=10m")
                .routeId(consumerUtil.kind(fscm).id())
                .setBody()
                .constant("{\"kind\":\"female_seiyu_category_members\"}")
                .to(ironmqUtil.kind(fscm).postUri());
    }
}
