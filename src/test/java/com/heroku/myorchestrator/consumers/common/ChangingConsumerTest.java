package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.App;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
public class ChangingConsumerTest extends Assert {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:changing_consumer_test_0")
    private ProducerTemplate producer;

    @EndpointInject(uri = "mock:changing_consumer_test_1")
    private MockEndpoint test1;

    @Test
    public void testMocksAreValid() throws Exception {
        producer.sendBody("{\"affect\":[\"foo\",\"bar\"]}");
        test1.message(0).body().isEqualTo("foo");
        test1.message(1).body().isEqualTo("bar");
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
