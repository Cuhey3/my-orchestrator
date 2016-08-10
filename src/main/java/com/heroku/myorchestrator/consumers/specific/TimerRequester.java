package com.heroku.myorchestrator.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.QueueConsumer;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class TimerRequester extends QueueConsumer {

    public TimerRequester() {
        ironmq().snapshot();
        route().timer();
    }

    @Override
    public void configure() {

        Stream.of(Kind.values())
                .filter((Kind k) -> k.timerUri() != null)
                .forEach((Kind k) -> {
                    kind(k);
                    from(k.timerUri())
                            .routeId(route().id())
                            .setBody().constant(k.preMessage())
                            .to(ironmq().postUri());
                });
    }
}
