package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiOriginalTitlesConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            List<Map<String, Object>> originalTitlesList, amiamiItemList;
            try {
                originalTitlesList = util.findLatest().get()
                        .get("data", List.class);
            } catch (Exception ex0) {
                originalTitlesList = new ArrayList<>();
            }
            amiamiItemList = util.kind(Kind.amiami_item).findLatest().get()
                    .get("data", List.class);
            Set originalTitlesSet = originalTitlesList
                    .stream().map((map) -> map.get("amiami_title"))
                    .collect(Collectors.toSet());
            amiamiItemList.stream()
                    .map((map) -> (String) map.get("orig"))
                    .filter((title) -> title.length() > 0)
                    .collect(Collectors.toSet())
                    .stream()
                    .filter((title) -> !originalTitlesSet.contains(title))
                    .map((title) -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("amiami_title", title);
                        return map;
                    })
                    .forEach(originalTitlesList::add);
            document.append("data", originalTitlesList);
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
