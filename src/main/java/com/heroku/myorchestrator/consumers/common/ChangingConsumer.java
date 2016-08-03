package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.config.enumerate.Kind;
import static com.heroku.myorchestrator.config.enumerate.Kind.*;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import static com.heroku.myorchestrator.util.MessageUtil.messageKindIs;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.springframework.stereotype.Component;

@Component
public class ChangingConsumer extends ConsumerRouteBuilder {

    public ChangingConsumer() {
        ironmq().changed();
        route().changing();
    }

    @Override
    public void configure() throws Exception {
        from(ironmq().consumeUri())
                .routeId(route().id())
                .choice()
                .when(messageKindIs(female_seiyu_category_members))
                .to("log:" + Kind.female_seiyu_category_members.expression())
                .when(messageKindIs(male_seiyu_category_members))
                .to("log:" + Kind.male_seiyu_category_members.expression())
                .when(messageKindIs(seiyu_category_members))
                .to("log:" + Kind.seiyu_category_members.expression())
                .when(messageKindIs(seiyu_category_members_include_template))
                .to("log:" + Kind.seiyu_category_members_include_template.expression())
                .when(messageKindIs(seiyu_has_recentchanges))
                .to("log:" + Kind.seiyu_has_recentchanges.expression())
                .when(messageKindIs(seiyu_template_include_pages))
                .to("log:" + Kind.seiyu_template_include_pages.expression())
                .when(messageKindIs(koepota_seiyu))
                .to("log:" + Kind.koepota_seiyu.expression())
                .when(messageKindIs(koepota_seiyu_all))
                .to("log:" + Kind.koepota_seiyu_all.expression())
                .when(messageKindIs(koepota_events))
                .to("log:" + Kind.koepota_events.expression())
                .when(messageKindIs(amiami_item))
                .to("log:" + Kind.amiami_item.expression())
                .when(messageKindIs(amiami_original_titles))
                .to("log:" + Kind.amiami_original_titles.expression())
                .when(messageKindIs(amiami_original_titles_all))
                .to("log:" + Kind.amiami_original_titles_all.expression())
                .when(messageKindIs(google_trends_seiyu_all))
                .to("log:" + Kind.google_trends_seiyu_all.expression())
                .otherwise().to("log:none")
                .end()
                .choice()
                .when(new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        try {
                            return DocumentUtil.checkNotFilled(exchange, null);
                        } catch (Exception ex) {
                            IronmqUtil.sendError(this.getClass(), "matches", exchange, ex);
                            return false;
                        }
                    }
                })
                .process((Exchange exchange) -> {
                    Kind k = Kind.valueOf(new MessageUtil(exchange).get("kind"));
                    new IronmqUtil().snapshot().postMessage(exchange, k);
                })
                .otherwise()
                .filter(MessageUtil.loadAffect())
                .split().body()
                .routingSlip(IronmqUtil.affectQueueUri());
    }
}
