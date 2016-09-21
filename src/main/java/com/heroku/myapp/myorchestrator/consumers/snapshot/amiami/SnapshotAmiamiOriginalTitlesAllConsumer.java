package com.heroku.myapp.myorchestrator.consumers.snapshot.amiami;

import static com.heroku.myapp.commons.config.enumerate.Kind.amiami_original_titles;
import static com.heroku.myapp.commons.config.enumerate.Kind.amiami_original_titles_all;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiOriginalTitlesAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            MapList aota, aot;
            aota = util.mapList(amiami_original_titles_all);
            aot = util.mapList(amiami_original_titles);
            return new DocumentUtil(
                    aota.addNewByKey(aot,"amiami_title")).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
