package com.heroku.myorchestrator.util.actions;

import com.heroku.myorchestrator.App;
import com.heroku.myorchestrator.config.enumerate.Kind;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@Component
public class MasterUtilTest extends RouteBuilder {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "direct:master_util_test_0")
    private ProducerTemplate producer0;

    @EndpointInject(uri = "mock:master_util_test_1")
    private MockEndpoint consumer1;

    @Override
    public void configure() throws Exception {
        from("direct:master_util_test_0")
                .setBody().constant(Kind.test.preMessage())
                .process((Exchange exchange) -> {
                    SnapshotUtil util = new SnapshotUtil(exchange);
                    util.useDummy();
                    util.writeDocument(new Document("random", Math.random()));
                })
                .to("mock:master_util_test_1");
    }

    @Test
    public void testSnapshotSaveToMaster() throws Exception {
        producer0.sendBody("");
        consumer1.message(0).body().in((Exchange exchange) -> {
            MasterUtil util = new MasterUtil(exchange);
            util.useDummy();
            try {
                return util.snapshotSaveToMaster();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }, (Exchange exchange) -> {
            MasterUtil masterUtil = new MasterUtil(exchange);
            SnapshotUtil snapshotUtil = new SnapshotUtil(exchange);
            masterUtil.useDummy();
            snapshotUtil.useDummy();
            try {
                Document master = masterUtil.findLatest().get();
                Document snapshot = snapshotUtil.loadDocument().get();
                return master.get("random", Double.class)
                        .equals(snapshot.get("random", Double.class))
                        && master.get("creationDate", String.class)
                        .equals(snapshot.get("creationDate", String.class));
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        });
        MockEndpoint.assertIsSatisfied(camelContext);
    }
}
