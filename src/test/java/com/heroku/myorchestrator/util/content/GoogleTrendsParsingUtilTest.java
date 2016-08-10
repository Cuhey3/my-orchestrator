package com.heroku.myorchestrator.util.content;

import com.heroku.myorchestrator.App;
import static com.heroku.myorchestrator.config.enumerate.Kind.google_trends;
import com.heroku.myorchestrator.util.actions.MasterUtil;
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
public class GoogleTrendsParsingUtilTest extends RouteBuilder {
    
    @Autowired
    protected CamelContext camelContext;
    
    @EndpointInject(uri = "direct:google_trends_parsing_util_test_0")
    private ProducerTemplate producer0;
    
    @EndpointInject(uri = "mock:google_trends_parsing_util_test_1")
    private MockEndpoint consumer1;
    
    @Override
    public void configure() {
        from("direct:google_trends_parsing_util_test_0")
                .to("mock:google_trends_parsing_util_test_1");
    }
    
    @Test
    public void testGoogleTrendsValues() throws InterruptedException {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            MasterUtil masterUtil = new MasterUtil(exchange);
            try {
                DocumentUtil.getData(masterUtil.findOrElseThrow(google_trends))
                        .stream().filter((map) -> map.containsKey("trends"))
                        .map((map) -> (Map<String, Object>) map.get("trends"))
                        .filter((map) -> map.get("status").equals("success"))
                        .filter((map) -> ((Double) map.get("avg")) > 3.5 && ((Double) map.get("avg")) < 5.5)
                        .filter((map) -> (Integer) map.get("max") < 15)
                        .filter((map) -> (Long) map.get("count") > 36)
                        .filter((map) -> (Long) map.get("count") < 108)
                        .forEach(System.out::println);
            } catch (Exception ex) {
            }
            return true;
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
