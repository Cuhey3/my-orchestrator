package com.heroku.myorchestrator.consumers.common;

import static com.heroku.definitions.config.enumerate.Kind.*;
import com.heroku.myorchestrator.consumers.QueueConsumer;
import com.heroku.definitions.util.MessageUtil;
import static com.heroku.definitions.util.MessageUtil.messageKindIs;
import com.heroku.definitions.util.actions.MasterUtil;
import com.heroku.definitions.util.consumers.IronmqUtil;
import org.springframework.stereotype.Component;

@Component
public class ChangingQueueConsumer extends QueueConsumer {

    public ChangingQueueConsumer() {
        ironmq().changed();
        route().changing();
    }

    @Override
    public void configure() {
        from(ironmq().consumeUri())
                .routeId(route().id())
                .choice()
                .when(messageKindIs(female_seiyu_category_members))
                .to("log:" + female_seiyu_category_members.expression())
                .when(messageKindIs(male_seiyu_category_members))
                .to("log:" + male_seiyu_category_members.expression())
                .when(messageKindIs(seiyu_category_members))
                .to("log:" + seiyu_category_members.expression())
                .when(messageKindIs(seiyu_category_members_include_template))
                .to("log:" + seiyu_category_members_include_template.expression())
                .when(messageKindIs(seiyu_has_recentchanges))
                .to("log:" + seiyu_has_recentchanges.expression())
                .when(messageKindIs(seiyu_template_include_pages))
                .to("log:" + seiyu_template_include_pages.expression())
                .when(messageKindIs(koepota_seiyu))
                .to("log:" + koepota_seiyu.expression())
                .when(messageKindIs(koepota_seiyu_all))
                .to("log:" + koepota_seiyu_all.expression())
                .when(messageKindIs(koepota_events))
                .to("log:" + koepota_events.expression())
                .when(messageKindIs(amiami_item))
                .to("log:" + amiami_item.expression())
                .when(messageKindIs(amiami_original_titles))
                .to("log:" + amiami_original_titles.expression())
                .when(messageKindIs(amiami_original_titles_all))
                .to("log:" + amiami_original_titles_all.expression())
                .when(messageKindIs(google_trends_seiyu_all))
                .to("log:" + google_trends_seiyu_all.expression())
                .otherwise().to("log:none")
                .end()
                .choice()
                .when(MasterUtil.isNotFilled(this))
                .process(IronmqUtil.requestSnapshotProcess())
                .otherwise()
                .filter(MessageUtil.loadAffect())
                .split().body()
                .routingSlip(IronmqUtil.affectQueueUri());
    }
}
