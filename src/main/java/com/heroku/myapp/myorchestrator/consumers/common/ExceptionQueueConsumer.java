package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import org.springframework.stereotype.Component;

@Component
public class ExceptionQueueConsumer extends QueueConsumer {

    public ExceptionQueueConsumer() {
        util().exception();
    }
    
    @Override
    public void configure() {
        from(util().kind(Kind.in).ironmqConsumeUri())
                .routeId(util().id())
                .to(util().kind(Kind.out).ironmqPostUri());
    }
}
