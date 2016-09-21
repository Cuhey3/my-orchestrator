package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_seiyu;
import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            MapList allList = util.mapList(koepota_seiyu_all)
                    .addNewByKey(util.mapList(koepota_seiyu), "title");
            Set scmitSet = util.mapList(seiyu_category_members_include_template)
                    .attrSet("title");
            allList.stream().forEach((map) -> {
                if (scmitSet.contains(map.get("title"))) {
                    map.remove("inactive");
                } else {
                    map.put("inactive", true);
                }
            });
            return new DocumentUtil(allList).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
