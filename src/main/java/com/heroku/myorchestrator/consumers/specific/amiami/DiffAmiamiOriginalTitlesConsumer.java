package com.heroku.myorchestrator.consumers.specific.amiami;

import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import java.util.Optional;
import org.bson.Document;
import org.springframework.stereotype.Component;

//@Component
public class DiffAmiamiOriginalTitlesConsumer extends DiffRouteBuilder {

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(master, snapshot, "amiami_title");
    }
}
