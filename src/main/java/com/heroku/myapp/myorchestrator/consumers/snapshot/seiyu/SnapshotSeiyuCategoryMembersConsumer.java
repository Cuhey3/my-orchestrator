package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.female_seiyu_category_members;
import static com.heroku.myapp.commons.config.enumerate.Kind.male_seiyu_category_members;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        try {
            MapList result = new MapList();
            result.addAll(util.mapList(female_seiyu_category_members));
            result.addAll(util.mapList(male_seiyu_category_members));
            return new DocumentUtil(result).nullable();
        } catch (Exception ex) {
            util().sendError("latestJoinAll", ex);
            return Optional.empty();
        }
    }
}
