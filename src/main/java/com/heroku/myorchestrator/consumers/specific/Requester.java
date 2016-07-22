package com.heroku.myorchestrator.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.consumers.KindUtil;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class Requester extends ConsumerRouteBuilder {

    private final KindUtil kindUtil = new KindUtil();

    public Requester() {
        ironmqUtil.snapshot();
        routeUtil.request();
        //Kind.foo.timerParam("period=60s");
        Kind.female_seiyu_category_members.timerParam("period=5m");
        Kind.male_seiyu_category_members.timerParam("period=5m");
    }

    @Override
    public void configure() throws Exception {

        Stream.of(Kind.values())
                .filter((Kind k) -> k.timerUri() != null)
                .forEach((Kind k) -> {
                    setKind(k);
                    from(k.timerUri())
                            .routeId(routeUtil.id())
                            .setBody().constant(kindUtil.kind(k).preMessage())
                            .to(ironmqUtil.postUri());
                });
    }
}
