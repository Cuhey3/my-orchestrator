package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.google_trends_seiyu_all;
import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_category_members_include_template;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_multi_lang;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            Set keySet = util.mapList(koepota_seiyu_all).attrSet("title");
            util.mapList(seiyu_multi_lang)
                    .filtered((map) -> {
                        List langlinks = (List) map.get("langlinks");
                        return langlinks != null && langlinks.size() > 1;
                    })
                    .map((map) -> map.get("title"))
                    .forEach(keySet::add);
            MapList product = util.mapList(
                    seiyu_category_members_include_template)
                    .intersectionList("title", keySet);
            return new DocumentUtil(util.mapList(google_trends_seiyu_all)
                    .addNewByKey(product, "title")).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
