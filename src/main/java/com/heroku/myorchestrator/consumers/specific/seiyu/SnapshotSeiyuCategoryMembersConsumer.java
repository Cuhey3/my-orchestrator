package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil masterUtil = new MasterUtil(exchange);
        return masterUtil.latestJoinAll(Kind.female_seiyu_category_members,
                Kind.male_seiyu_category_members);
    }
}
