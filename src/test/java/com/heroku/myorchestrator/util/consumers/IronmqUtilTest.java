package com.heroku.myorchestrator.util.consumers;

import com.heroku.myorchestrator.App;
import com.heroku.myorchestrator.consumers.common.ChangingQueueConsumer;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
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
public class IronmqUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;
    @Autowired
    protected ChangingQueueConsumer changingQueueConsumer;

    @EndpointInject(uri = "direct:ironmq_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:ironmq_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() {
        from("direct:ironmq_util_test_0")
                .to("mock:ironmq_util_test_1");
    }

    @Test
    public void testSendError() throws InterruptedException {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            try {
                throw new RuntimeException();
            } catch (RuntimeException e) {
                IronmqUtil.sendError(changingQueueConsumer, "testSendError", e);
                return true; // dummy
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
