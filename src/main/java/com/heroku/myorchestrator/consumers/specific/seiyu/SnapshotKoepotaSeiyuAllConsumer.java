package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu;
import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import static com.heroku.myorchestrator.util.content.DocumentUtil.getData;
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
            List<Map<String, Object>> allList;
            try {
                allList = getData(util.getLatest(koepota_seiyu_all));
            } catch (Exception ex0) {
                allList = new ArrayList<>();
            }
            Set<Object> allSet = allList.stream().map((map) -> map.get("title"))
                    .collect(Collectors.toSet());
            getData(util.getLatest(koepota_seiyu)).stream()
                    .filter((map) -> !allSet.contains(map.get("title")))
                    .forEach(allList::add);
            Set scmitSet = getData(
                    util.getLatest(seiyu_category_members_include_template))
                    .stream().map((map) -> map.get(("title")))
                    .collect(Collectors.toSet());
            allList.stream()
                    .forEach((map) -> {
                        if (scmitSet.contains(map.get("title"))) {
                            map.remove("inactive");
                        } else {
                            map.put("inactive", true);
                        }
                    });
            return new DocumentUtil(document).setData(allList).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
