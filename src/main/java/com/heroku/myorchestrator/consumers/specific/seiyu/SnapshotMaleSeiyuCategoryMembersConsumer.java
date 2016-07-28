package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.specific.SeiyuUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotMaleSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    private final SeiyuUtil.SeiyuKind seiyuKind;

    public SnapshotMaleSeiyuCategoryMembersConsumer() {
        seiyuKind = SeiyuUtil.SeiyuKind.male;
        kind(seiyuKind.kind());
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        return new SeiyuUtil(exchange).defaultSnapshot(seiyuKind, document);
    }
}
