package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.specific.SeiyuUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFemaleSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    private final SeiyuUtil.SeiyuKind seiyuKind;

    public SnapshotFemaleSeiyuCategoryMembersConsumer() {
        seiyuKind = SeiyuUtil.SeiyuKind.female;
        kind(seiyuKind.kind());
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        return SeiyuUtil.defaultSnapshot(seiyuKind, document);
    }
}
