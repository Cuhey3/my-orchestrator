package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.koepota_events;
import static com.heroku.myapp.commons.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaSeiyuConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            StringBuilder sb = new StringBuilder(32768);
            DocumentUtil.getData(util.findOrElseThrow(koepota_events)).stream()
                    .map((map) -> (String) map.get("c1")).forEach(sb::append);
            String koepotaStr = new String(sb);
            List<Map<String, Object>> collect = DocumentUtil.getData(util
                    .findOrElseThrow(seiyu_category_members_include_template))
                    .stream().filter((map)
                            -> koepotaStr.contains(((String) map.get("title"))
                                    .replaceFirst(" \\(.+", "")))
                    .collect(Collectors.toList());
            return new DocumentUtil(collect).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
