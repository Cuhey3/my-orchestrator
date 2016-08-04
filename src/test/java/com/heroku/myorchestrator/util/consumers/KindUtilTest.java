package com.heroku.myorchestrator.util.consumers;

import com.heroku.myorchestrator.App;
import com.heroku.myorchestrator.config.enumerate.Kind;
import java.util.List;
import java.util.Map;
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
public class KindUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:kind_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:kind_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() throws Exception {
        from("direct:kind_util_test_0")
                .setBody().constant(Kind.test.preMessage())
                .to("mock:kind_util_test_1");
    }

    @Test
    public void testPreMessageConvertToString() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            String bodyString = exchange.getIn().getBody(String.class);
            return bodyString.contains("\"kind\"")
                    && bodyString.contains("\"affect\"");
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void testPreMessageConvertToMap() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            Map bodyMap = exchange.getIn().getBody(Map.class);
            return bodyMap.containsKey("kind")
                    && bodyMap.containsKey("affect");
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void testPreMessageAffectIsList() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            Map bodyMap = exchange.getIn().getBody(Map.class);
            List affect = (List) bodyMap.get("affect");
            return affect.isEmpty();
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
