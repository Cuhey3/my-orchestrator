package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_category_members_include_template;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_multi_lang;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            DocumentUtil util = new DocumentUtil();
            Document latest = masterUtil.optionalLatest()
                    .orElse(new Document("data", new ArrayList<>()));
            Set keySet = util.setDocument(masterUtil
                    .findOrElseThrow(koepota_seiyu_all)).keySet("title");
            util.setDocument(masterUtil
                    .findOrElseThrow(seiyu_multi_lang)).getData()
                    .stream().filter((map) -> {
                        List langlinks = (List) map.get("langlinks");
                        return langlinks != null && langlinks.size() > 1;
                    })
                    .map((map) -> map.get("title"))
                    .forEach(keySet::add);
            List<Map<String, Object>> product
                    = util.setDocument(masterUtil.findOrElseThrow(
                            seiyu_category_members_include_template)).getData()
                    .stream().filter((map) -> keySet.contains(map.get("title")))
                    .collect(Collectors.toList());
            /*
            Document product = util.productByTitle(
                    masterUtil.findOrElseThrow(koepota_seiyu_all),
                    masterUtil.findOrElseThrow(seiyu_has_recentchanges))
                    .getDocument();*/
            return new DocumentUtil()
                    .addNewByKey(latest, product, "title").nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
