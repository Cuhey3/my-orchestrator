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
        //Kind.foo.timerParam("period=60s");
        Kind.female_seiyu_category_members.timerParam("period=5m&delay=1m");
        Kind.male_seiyu_category_members.timerParam("period=5m&delay=2m");
        Kind.seiyu_template_include_pages.timerParam("period=5m&delay=3m");
        Kind.koepota_events.timerParam("period=30m&delay=10m");
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
