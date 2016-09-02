package com.heroku.myapp.myorchestrator.consumers.request;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.KindOptions;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class TimerRequester extends QueueConsumer {

    @Override
    public void configure() {

        Stream.of(Kind.values())
                .filter((Kind k) -> k.isEnable(KindOptions.polling))
                .forEach((Kind k) -> {
                    from(k.timerUri())
                            .routeId(util().timer().kind(k).id())
                            .setBody().constant(k.preMessage())
                            .to(util().snapshot().ironmqPostUri());
                });
    }
}
