package com.heroku.myorchestrator.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.consumers.KindUtil;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class Requester extends ConsumerRouteBuilder {

    public Requester() {
        ironmq().snapshot();
        route().timer();
    }

    @Override
    public void configure() throws Exception {

        Stream.of(Kind.values())
                .filter((Kind k) -> k.timerUri() != null)
                .forEach((Kind k) -> {
                    kind(k);
                    from(k.timerUri())
                            .routeId(route().id())
                            .setBody().constant(new KindUtil(k).preMessage())
                            .to(ironmq().postUri());
                });
    }
}
