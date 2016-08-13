package com.heroku.myorchestrator.consumers.specific.amiami;

import static com.heroku.definitions.config.enumerate.Kind.amiami_item;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.definitions.util.actions.MasterUtil;
import com.heroku.definitions.util.consumers.IronmqUtil;
import com.heroku.definitions.util.content.DocumentUtil;
import static com.heroku.definitions.util.content.DocumentUtil.getData;
import com.heroku.definitions.util.content.GoogleWikiTitle;
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
public class SnapshotAmiamiOriginalTitlesConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            DocumentUtil util = new DocumentUtil();
            mergeNewTitles(exchange, util);
            updateWikiTitles(util);
            return util.nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }

    public void mergeNewTitles(Exchange exchange, DocumentUtil util) {
        MasterUtil masterUtil = new MasterUtil(exchange);
        List<Map<String, Object>> originalTitlesList;
        try {
            originalTitlesList = getData(masterUtil.findOrElseThrow());
        } catch (Exception ex0) {
            originalTitlesList = new ArrayList<>();
        }
        Set originalTitlesSet = originalTitlesList
                .stream().map((map) -> map.get("amiami_title"))
                .collect(Collectors.toSet());
        getData(masterUtil.findOrElseThrow(amiami_item))
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
        util.setData(originalTitlesList);
    }

    public void updateWikiTitles(DocumentUtil util) {
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
