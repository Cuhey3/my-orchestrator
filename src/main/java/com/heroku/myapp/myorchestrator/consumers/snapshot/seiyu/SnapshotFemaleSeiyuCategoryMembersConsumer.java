package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.consumers.IronmqUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFemaleSeiyuCategoryMembersConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            List<Map<String, Object>> result
                    = new MediawikiApiRequest()
                    .setApiParam("action=query&list=categorymembers"
                            + "&cmtitle=Category:"
                            + URLEncoder.encode("日本の女性声優", "UTF-8")
                            + "&cmlimit=500"
                            + "&cmnamespace=0"
                            + "&format=xml"
                            + "&continue="
                            + "&cmprop=title|ids|sortkeyprefix")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList();
            result.forEach((m) -> m.put("gender", "f"));
            return new DocumentUtil(result).nullable();
        } catch (Exception e) {
            IronmqUtil.sendError(this, "doSnapshot", e);
            return Optional.empty();
        }
    }
}