package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.GoogleWikiTitle;
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
            mergeNewTitles(exchange, document);
            updateWikiTitles(document);
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

    public void mergeNewTitles(Exchange exchange, Document document) throws Exception {
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
    }

    public void updateWikiTitles(Document document) {
        List<Map<String, Object>> titles = document.get("data", List.class);
        final GoogleWikiTitle gwt = new GoogleWikiTitle();
        titles.stream().filter((map) -> !map.containsKey("wiki_titles"))
                .limit(10).forEach((map) -> {
            String amiamiTitle = (String) map.get("amiami_title");
            List<String> wikiTitles
                    = new ArrayList<>(gwt.google(amiamiTitle).get());
            map.put("wiki_titles", wikiTitles);
        });
        document.put("data", titles);
    }
}
