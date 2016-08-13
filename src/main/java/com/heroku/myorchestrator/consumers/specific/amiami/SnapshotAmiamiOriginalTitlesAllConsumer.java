package com.heroku.myorchestrator.consumers.specific.amiami;

import static com.heroku.definitions.config.enumerate.Kind.amiami_original_titles;
import static com.heroku.definitions.config.enumerate.Kind.amiami_original_titles_all;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.definitions.util.actions.MasterUtil;
import com.heroku.definitions.util.consumers.IronmqUtil;
import com.heroku.definitions.util.content.DocumentUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiOriginalTitlesAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            return new DocumentUtil().addNewByKey(
                    masterUtil.findOrElseThrow(amiami_original_titles_all),
                    masterUtil.findOrElseThrow(amiami_original_titles),
                    "amiami_title").nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
