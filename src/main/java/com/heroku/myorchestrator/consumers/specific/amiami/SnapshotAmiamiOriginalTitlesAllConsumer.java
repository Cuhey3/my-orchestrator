package com.heroku.myorchestrator.consumers.specific.amiami;

import static com.heroku.myorchestrator.config.enumerate.Kind.amiami_original_titles;
import static com.heroku.myorchestrator.config.enumerate.Kind.amiami_original_titles_all;
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
public class SnapshotAmiamiOriginalTitlesAllConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            List<Map<String, Object>> allList;
            try {
                allList = getData(util.getLatest(amiami_original_titles_all));
            } catch (Exception ex0) {
                allList = new ArrayList<>();
            }
            Set allSet = allList.stream().map((map) -> map.get("amiami_title"))
                    .collect(Collectors.toSet());
            getData(util.getLatest(amiami_original_titles)).stream()
                    .filter((map) -> !allSet.contains(map.get("amiami_title")))
                    .forEach(allList::add);
            return new DocumentUtil(document).setData(allList).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
