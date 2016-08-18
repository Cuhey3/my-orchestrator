package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_has_recentchanges;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.consumers.ConsumerUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.ArrayList;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            Document latest = masterUtil.optionalLatest()
                    .orElse(new Document("data", new ArrayList<>()));
            DocumentUtil util = new DocumentUtil();
            Document product = util.productByTitle(
                    masterUtil.findOrElseThrow(koepota_seiyu_all),
                    masterUtil.findOrElseThrow(seiyu_has_recentchanges))
                    .getDocument();
            return util.addNewByKey(latest, product, "title").nullable();
        } catch (Exception ex) {
            ConsumerUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
