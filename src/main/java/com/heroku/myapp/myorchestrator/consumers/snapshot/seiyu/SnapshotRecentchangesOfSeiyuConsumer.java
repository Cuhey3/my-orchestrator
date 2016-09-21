package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SnapshotRecentchangesOfSeiyuConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        Optional<Document> optionalMaster
                = util.optionalDocumentFromKindString(
                        Kind.recentchanges_of_seiyu.expression());
        MapList rclist, requestList, noChangeList, hitList;
        String rcstart;
        if (optionalMaster.isPresent()) {
            Document master = optionalMaster.get();
            rclist = new MapList(master);
            rcstart = (String) master.getOrDefault("rcstart", getRcstart());
        } else {
            rclist = new MapList();
            rcstart = getRcstart();
        }
        Set listNames = rclist.attrSet("title");
        util.mapList(Kind.seiyu_category_members_include_template)
                .intersection("title", listNames, false)
                .forEach(rclist::add);
        Set updatedNames = rclist.attrSet("title");
        try {
            requestList = new MapList(new MediawikiApiRequest()
                    .setApiParam("action=query&list=recentchanges"
                            + "&rcnamespace=0"
                            + "&rclimit=500&format=xml&rctype=edit"
                            + "&rctoponly&rcdir=newer"
                            + "&rcstart=" + rcstart)
                    .setListName("recentchanges")
                    .setMapName("rc")
                    .setContinueElementName("rccontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList());
        } catch (IOException ex) {
            throw new RuntimeException();
        }
        String nextRcstart = (String) requestList.get(requestList.size() - 1)
                .get("timestamp");
        Set requestTitles = requestList.attrSet("title");
        noChangeList = rclist.intersectionList("title", requestTitles, false);
        hitList = requestList.intersectionList("title", updatedNames);
        rclist.intersection("title", requestTitles)
                .map((map) -> {
                    Object title = map.get("title");
                    Map<String, Object> change = hitList.stream()
                            .filter((m) -> m.get("title").equals(title))
                            .findFirst().get();
                    if (map.containsKey("revid")) {
                        map.put("old_revid", map.get("revid"));
                        map.put("revid", change.get("revid"));
                    } else {
                        map.put("revid", change.get("revid"));
                        map.put("old_revid", change.get("old_revid"));
                    }
                    return map;
                }).forEach(noChangeList::add);
        Document document = new DocumentUtil(noChangeList).getDocument();
        document.put("rcstart", nextRcstart);
        return Optional.ofNullable(document);
    }

    public String getRcstart() {
        Elements recentchanges;
        try {
            recentchanges = Jsoup.connect("https://ja.wikipedia.org/w/api.php"
                    + "?action=query&list=recentchanges&rcnamespace=0"
                    + "&rclimit=100&format=xml&rctype=edit&rctoponly")
                    .ignoreContentType(true).get().select("rc");
        } catch (IOException ex) {
            throw new RuntimeException();
        }
        return recentchanges.get(recentchanges.size() - 1).attr("timestamp");
    }
}
