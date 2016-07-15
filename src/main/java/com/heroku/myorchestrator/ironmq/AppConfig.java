package com.heroku.myorchestrator.ironmq;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Autowired
  CamelContext context;

  @Bean
  AppConfig converterReady() {
    MyConverters myConverters = new MyConverters(context);
    context.getTypeConverterRegistry().addTypeConverters(myConverters);
    return this;
  }
}
