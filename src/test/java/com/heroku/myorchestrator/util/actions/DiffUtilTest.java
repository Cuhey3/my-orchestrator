package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.App;
import com.heroku.myorchestrator.config.enumerate.Kind;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@Component
public class DiffUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:diff_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:diff_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() throws Exception {
        from("direct:diff_util_test_0")
                .setBody().constant(Kind.test.preMessage())
                .process((Exchange exchange) -> {
                    DiffUtil util = new DiffUtil(exchange);
                    util.useDummy();
                    util.writeDocument(new Document());
                })
                .to("mock:diff_util_test_1");
    }

    @Test
    public void testDiffMessageIsValid() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in(
                (Exchange exchange) -> {
                    Map body = exchange.getIn().getBody(Map.class);
                    return body.containsKey("diff_id");
                },
                (Exchange exchange) -> {
                    Map body = exchange.getIn().getBody(Map.class);
                    DiffUtil util = new DiffUtil(exchange);
                    util.useDummy();
                    try {
                        Optional<Document> findById
                        = util.findById((String) body.get("diff_id"));
                        return util.diffIdIsValid() && findById.isPresent();
                    } catch (Exception ex) {
                        return false;
                    }
                }, (Exchange exchange) -> {
                    Map body = exchange.getIn().getBody(Map.class);
                    DiffUtil util = new DiffUtil(exchange);
                    util.useDummy();
                    try {
                        String loadedObjectIdHexString
                        = util.loadDocument().get().get("_id", ObjectId.class)
                        .toHexString();
                        return body.get("diff_id")
                        .equals(loadedObjectIdHexString);
                    } catch (Exception ex) {
                        return false;
                    }
                });
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void testEnableDiff() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            DiffUtil util = new DiffUtil(exchange);
            util.useDummy();
            try {
                if (util.enableDiff(this)) {
                    Document diff = util.loadDocument().get();
                    return diff.get("enable", Boolean.class);
                } else {
                    return false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
