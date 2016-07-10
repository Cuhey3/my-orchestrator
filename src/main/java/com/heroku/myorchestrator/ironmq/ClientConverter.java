package com.heroku.myorchestrator.ironmq;

import io.iron.ironmq.Client;
import org.apache.camel.CamelContext;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

public class ClientConverter implements TypeConverters {

    CamelContext context;

    public ClientConverter(CamelContext context) {
        this.context = context;
    }

    @Converter
    public Client toClient(String beanName) {
        return context.getRegistry().lookupByNameAndType(beanName, Client.class);
    }
}
