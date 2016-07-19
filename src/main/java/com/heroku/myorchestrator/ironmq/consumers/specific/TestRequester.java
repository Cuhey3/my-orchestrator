package com.heroku.myorchestrator.ironmq.consumers.specific;

import static com.heroku.myorchestrator.util.IronmqUtil.*;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TestRequester extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("timer:foo?period=60s")
            .setBody(constant("{}"))
            .to(postQueueUri("test_snapshot"));
    from("timer:female_seiyu_category_members?period=20m&delay=10m")
            .routeId("request_female_seiyu_category_members")
            .setBody(constant("{}"))
            .to(postQueueUri("snapshot", "female_seiyu_category_members"));
  }
}
