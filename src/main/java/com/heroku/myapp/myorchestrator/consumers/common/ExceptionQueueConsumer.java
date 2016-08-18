package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import org.springframework.stereotype.Component;

@Component
public class ExceptionQueueConsumer extends QueueConsumer {

    public ExceptionQueueConsumer() {
        route().exception();
    }
    
    @Override
    public void configure() {
        from(route().kind(Kind.in).consumeUri())
                .routeId(route().id())
                .to(route().kind(Kind.out).postUri());
    }
}
