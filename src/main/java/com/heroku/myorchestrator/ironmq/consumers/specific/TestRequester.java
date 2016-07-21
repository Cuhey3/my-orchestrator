package com.heroku.myorchestrator.ironmq.consumers.specific;

import com.heroku.myorchestrator.util.IronmqUtil;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestRequester extends RouteBuilder {

    IronmqUtil ironmqUtil = new IronmqUtil().snapshot();

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=60s")
                .setBody()
                .constant("{\"kind\":\"foo\"}")
                .to(ironmqUtil.kind("foo").postUri());

        from("timer:female_seiyu_category_members?period=20m&delay=10m")
                .routeId("request_female_seiyu_category_members")
                .setBody()
                .constant("{\"kind\":\"female_seiyu_category_members\"}")
                .to(ironmqUtil.kind("female_seiyu_category_members").postUri());
    }
}
