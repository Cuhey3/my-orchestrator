package com.heroku.myorchestrator.ironmq;

import java.util.ArrayList;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IronmqRoute extends RouteBuilder {

    String ironmqConsumerEndpoint;
    String ironmqProducerEndpoint;

    @Autowired
    public IronmqRoute(CamelContext context) {
        ironmqConsumerEndpoint = "ironmq:action_end?client=myclient&batchDelete=true";
        ironmqProducerEndpoint = "ironmq:%s_%s_start?client=myclient";
        ClientConverter clientConverter = new ClientConverter(context);
        context.getTypeConverterRegistry().addTypeConverters(clientConverter);
    }

    @Override
    public void configure() throws Exception {
        from(ironmqConsumerEndpoint)
                .unmarshal().json(JsonLibrary.Gson, Map.class)
                .process((Exchange exchange) -> {
                    Map<String, String> message = exchange.getIn().getBody(Map.class);
                    if (message.get("task_id") == null) {
                        message.put("task_id", exchange.getIn().getHeader("CamelIronMQMessageId", String.class));
                    }
                    String collection = message.get("collection");
                    String to = message.get("to");
                    exchange.getIn().setHeader("to", String.format(ironmqProducerEndpoint, collection, to));
                    exchange.getIn().setBody(message);
                })
                .marshal().json(JsonLibrary.Gson).convertBodyTo(String.class)
                .routingSlip(header("to"));
    }

}
