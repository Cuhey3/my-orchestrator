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
public class SnapshotUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:snapshot_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:snapshot_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() throws Exception {
        from("direct:snapshot_util_test_0")
                .setBody().constant(Kind.test.preMessage())
                .process((Exchange exchange) -> {
                    SnapshotUtil util = new SnapshotUtil(exchange);
                    util.useDummy();
                    util.writeDocument(new Document());
                })
                .to("mock:snapshot_util_test_1");
    }

    @Test
    public void testMessageHasSnapshotId() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            Map body = exchange.getIn().getBody(Map.class);
            return body.containsKey("snapshot_id");
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void testMessageSnapshotIdIsValid() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            Map body = exchange.getIn().getBody(Map.class);
            SnapshotUtil util = new SnapshotUtil(exchange);
            util.useDummy();
            try {
                Optional<Document> findById
                        = util.findById((String) body.get("snapshot_id"));
                return findById.isPresent();
            } catch (Exception ex) {
                return false;
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    public void testLoadSnapshotDocument() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            Map body = exchange.getIn().getBody(Map.class);
            SnapshotUtil util = new SnapshotUtil(exchange);
            util.useDummy();
            try {
                String loadedObjectIdHexString
                        = util.loadDocument().get().get("_id", ObjectId.class)
                        .toHexString();
                return body.get("snapshot_id").equals(loadedObjectIdHexString);
            } catch (Exception ex) {
                return false;
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
