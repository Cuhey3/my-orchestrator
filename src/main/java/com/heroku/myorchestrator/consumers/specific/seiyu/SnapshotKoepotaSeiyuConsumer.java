package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_events;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaSeiyuConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            StringBuilder sb = new StringBuilder(32768);
            DocumentUtil.getData(util.getLatest(koepota_events)).stream()
                    .map((map) -> (String) map.get("c1")).forEach(sb::append);
            String koepotaStr = new String(sb);
            List<Map<String, Object>> collect = DocumentUtil.getData(
                    util.getLatest(seiyu_category_members_include_template))
                    .stream().filter((map)
                            -> koepotaStr.contains(((String) map.get("title"))
                                    .replaceFirst(" \\(.+", "")))
                    .collect(Collectors.toList());
            return new DocumentUtil(collect).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
