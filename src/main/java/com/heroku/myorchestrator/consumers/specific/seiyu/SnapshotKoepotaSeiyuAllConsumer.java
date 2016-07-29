package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
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
public class SnapshotKoepotaSeiyuAllConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            util.kind(Kind.koepota_seiyu_all);
            List<Map<String, Object>> allList, koepotaSeiyuList, scmitList;
            try {
                allList = util.findLatest().get().get("data", List.class);
            } catch (Exception ex0) {
                allList = new ArrayList<>();
            }
            util.kind(Kind.koepota_seiyu);
            koepotaSeiyuList
                    = util.findLatest().get().get("data", List.class);
            Set<Object> allSet = allList.stream().map((map) -> map.get("title"))
                    .collect(Collectors.toSet());
            koepotaSeiyuList.stream()
                    .filter((map) -> !allSet.contains(map.get("title")))
                    .forEach(allList::add);
            util.kind(Kind.seiyu_category_members_include_template);
            scmitList = util.findLatest().get().get("data", List.class);
            Set<Object> scmitSet = scmitList.stream().map((map) -> map.get(("title")))
                    .collect(Collectors.toSet());
            allList.stream()
                    .forEach((map) -> {
                        if (scmitSet.contains(map.get("title"))) {
                            map.remove("inactive");
                        } else {
                            map.put("inactive", true);
                        }
                    });
            return Optional.ofNullable(document.append("data", allList));
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

}
