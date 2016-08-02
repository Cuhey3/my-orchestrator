package com.heroku.myorchestrator.util.content;

import com.heroku.myorchestrator.App;
import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.consumers.KindUtil;
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
public class DocumentUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:document_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:document_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() throws Exception {
        from("direct:document_util_test_0")
                .setBody().constant(new KindUtil(Kind.amiami_original_titles)
                        .preMessage())
                .to("mock:document_util_test_1");
    }

    @Test
    public void testCheckNotFilled() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            try {
                return !DocumentUtil.checkNotFilled(exchange, null);
            } catch (Exception ex) {
                return true;
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
