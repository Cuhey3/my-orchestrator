package com.heroku.myapp.myorchestrator.consumers.snapshot.amiami;

import static com.heroku.myapp.commons.config.enumerate.Kind.amiami_item;
import static com.heroku.myapp.commons.config.enumerate.Kind.amiami_original_titles;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.GoogleWikiTitle;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiOriginalTitlesConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            return new DocumentUtil(updateWikiTitles(
                    mergeNewTitles(exchange))).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }

    public MapList mergeNewTitles(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        MapList originalTitles = util.mapList(amiami_original_titles);
        Set originalTitlesSet = originalTitles.attrSet("amiami_title");
        util.mapList(amiami_item)
                .attrStream("orig", String.class)
                .filter((title) -> title.length() > 0)
                .collect(Collectors.toSet())
                .stream()
                .filter((title) -> !originalTitlesSet.contains(title))
                .map((title) -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("amiami_title", title);
                    return map;
                })
                .forEach(originalTitles::add);
        return originalTitles;
    }

    public MapList updateWikiTitles(MapList mergedTitles) {
        final GoogleWikiTitle gwt = new GoogleWikiTitle();
        mergedTitles.stream().filter((map) -> !map.containsKey("wiki_titles"))
                .limit(10).forEach((map) -> {
            String amiamiTitle = (String) map.get("amiami_title");
            map.put("wiki_titles", new ArrayList<>(
                    gwt.google(amiamiTitle).get()));
        });
        return mergedTitles;
    }
}
