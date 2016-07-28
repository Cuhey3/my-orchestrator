package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import java.util.Optional;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffKoepotaEventsConsumer extends DiffRouteBuilder {

    public DiffKoepotaEventsConsumer() {
        kind(Kind.koepota_events);
    }

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(master, snapshot, "url");
    }
}
