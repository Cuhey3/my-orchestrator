package com.heroku.myorchestrator.consumers.common;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
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
                .when(MessageUtil.messageKindIs(Kind.female_seiyu_category_members)).to("log:changing_" + Kind.female_seiyu_category_members.expression())
                .when(MessageUtil.messageKindIs(Kind.male_seiyu_category_members)).to("log:changing_" + Kind.male_seiyu_category_members.expression())
                .when(MessageUtil.messageKindIs(Kind.seiyu_category_members)).to("log:changing_" + Kind.seiyu_category_members.expression())
                .when(MessageUtil.messageKindIs(Kind.seiyu_category_members_include_template)).to("log:changing_" + Kind.seiyu_category_members_include_template.expression())
                .when(MessageUtil.messageKindIs(Kind.seiyu_has_recentchanges)).to("log:changing_" + Kind.seiyu_has_recentchanges.expression())
                .when(MessageUtil.messageKindIs(Kind.seiyu_template_include_pages)).to("log:changing_" + Kind.seiyu_template_include_pages.expression())
                .when(MessageUtil.messageKindIs(Kind.koepota_seiyu)).to("log:changing_" + Kind.koepota_seiyu.expression())
                .when(MessageUtil.messageKindIs(Kind.koepota_seiyu_all)).to("log:changing_" + Kind.koepota_seiyu_all.expression())
                .when(MessageUtil.messageKindIs(Kind.koepota_events)).to("log:changing_" + Kind.koepota_events.expression())
                .when(MessageUtil.messageKindIs(Kind.amiami_item)).to("log:changing_" + Kind.amiami_item.expression())
                .when(MessageUtil.messageKindIs(Kind.amiami_original_titles)).to("log:changing_" + Kind.amiami_original_titles.expression())
                .otherwise().to("log:changing_none")
                .end()
                .filter(MessageUtil.loadAffect())
                .split().body()
                .routingSlip(IronmqUtil.affectQueueUri());
    }
}
