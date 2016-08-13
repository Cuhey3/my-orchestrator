package com.heroku.myorchestrator.consumers.specific.amiami;

import com.heroku.myorchestrator.consumers.DiffQueueConsumer;
import com.heroku.definitions.util.actions.DiffUtil;
import com.heroku.definitions.util.content.DocumentUtil;
import java.util.Optional;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffAmiamiItemConsumer extends DiffQueueConsumer {

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        return DiffUtil.basicDiff(DocumentUtil.restorePrefix(master),
                DocumentUtil.restorePrefix(snapshot), "url");
    }
}
