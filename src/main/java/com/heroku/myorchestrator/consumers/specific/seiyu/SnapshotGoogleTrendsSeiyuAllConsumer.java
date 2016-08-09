package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_has_recentchanges;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
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
            Document findLatest = masterUtil.optionalFind()
                    .orElse(new Document("data", new ArrayList<>()));
            DocumentUtil util = new DocumentUtil();
            Document product = util.productByTitle(
                    masterUtil.findOrElseThrow(koepota_seiyu_all),
                    masterUtil.findOrElseThrow(seiyu_has_recentchanges))
                    .getDocument();
            return util.addNewByKey(findLatest, product, "title").nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
