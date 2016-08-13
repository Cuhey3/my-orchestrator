package com.heroku.myapp.myorchestrator.consumers.snapshot.amiami;

import com.heroku.myapp.commons.consumers.DiffQueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
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
