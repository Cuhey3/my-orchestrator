package com.heroku.myorchestrator.consumers.specific.amiami;

import static com.heroku.myorchestrator.config.enumerate.Kind.amiami_item;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import static com.heroku.myorchestrator.util.content.DocumentUtil.getData;
import static com.heroku.myorchestrator.util.content.DocumentUtil.setData;
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
        List<Map<String, Object>> originalTitlesList;
        try {
            originalTitlesList = getData(util.findLatest().get());
        } catch (Exception ex0) {
            originalTitlesList = new ArrayList<>();
        }
        Set originalTitlesSet = originalTitlesList
                .stream().map((map) -> map.get("amiami_title"))
                .collect(Collectors.toSet());
        getData(util.getLatest(amiami_item))
                .stream().map((map) -> (String) map.get("orig"))
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
        setData(document, originalTitlesList);
    }

    public void updateWikiTitles(Document document) {
        DocumentUtil util = new DocumentUtil(document);
        List<Map<String, Object>> titles = util.getData();
        final GoogleWikiTitle gwt = new GoogleWikiTitle();
        titles.stream().filter((map) -> !map.containsKey("wiki_titles"))
                .limit(10).forEach((map) -> {
            String amiamiTitle = (String) map.get("amiami_title");
            List<String> wikiTitles
                    = new ArrayList<>(gwt.google(amiamiTitle).get());
            map.put("wiki_titles", wikiTitles);
        });
        util.setData(titles);
    }
}
