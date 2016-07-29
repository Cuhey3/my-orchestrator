package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
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
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            Document scmit, koepota;
            List<Map<String, Object>> scmitList, koepotaList, collect;
            scmit = util.kind(Kind.seiyu_category_members_include_template)
                    .findLatest().get();
            koepota = util.kind(Kind.koepota_events).findLatest().get();
            koepotaList = koepota.get("data", List.class);
            StringBuilder sb = new StringBuilder(32768);
            koepotaList.stream().map((map) -> (String) map.get("c1"))
                    .forEach(sb::append);
            String koepotaString = new String(sb);
            scmitList = scmit.get("data", List.class);
            collect = scmitList.stream().filter(
                    (map) -> koepotaString.contains(((String) map.get("title"))
                            .replaceFirst(" \\(.+", "")))
                    .collect(Collectors.toList());
            document.append("data", collect);
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
