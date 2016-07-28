package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.specific.SeiyuUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    public SnapshotSeiyuCategoryMembersConsumer() {
        kind(Kind.seiyu_category_members);
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        MasterUtil masterUtil = new MasterUtil(exchange);
        return masterUtil.latestJoinAll(SeiyuUtil.SeiyuKind.female.kind(),
                SeiyuUtil.SeiyuKind.male.kind());
    }
}
