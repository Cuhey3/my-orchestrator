package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.Optional;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffAmiamiItemConsumer extends DiffRouteBuilder {

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(DocumentUtil.restorePrefix(master),
                DocumentUtil.restorePrefix(snapshot), "url");
    }
}
