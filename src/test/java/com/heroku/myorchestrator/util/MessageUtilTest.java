package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.App;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@Component
public class MessageUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:message_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:message_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() {
        from("direct:message_util_test_0")
                .filter(MessageUtil.loadAffect())
                .split().body()
                .to("mock:message_util_test_1");
    }

    @Test
    public void testLoadAffect() throws InterruptedException {
        producer0.sendBody("{\"affect\":[\"foo\",\"bar\"]}");
        consumer1.message(0).body().isEqualTo("foo");
        consumer1.message(1).body().isEqualTo("bar");
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
