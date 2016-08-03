package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import com.heroku.myorchestrator.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotMaleSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            List<Map<String, Object>> mapList
                    = new MediawikiApiRequest()
                    .setApiParam("action=query&list=categorymembers"
                            + "&cmtitle=Category:"
                            + URLEncoder.encode("日本の男性声優", "UTF-8")
                            + "&cmlimit=500"
                            + "&cmnamespace=0"
                            + "&format=xml"
                            + "&continue="
                            + "&cmprop=title|ids|sortkeyprefix")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList();
            mapList.forEach((m) -> m.put("gender", "m"));
            return new DocumentUtil(mapList).nullable();
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, e);
            return Optional.empty();
        }
    }
}
