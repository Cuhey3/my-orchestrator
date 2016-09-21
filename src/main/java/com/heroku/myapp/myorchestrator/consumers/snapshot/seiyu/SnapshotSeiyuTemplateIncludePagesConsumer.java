package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.io.IOException;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuTemplateIncludePagesConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MapList result = new MediawikiApiRequest()
                    .setApiParam("action=query&list=backlinks"
                            + "&bltitle=Template:%E5%A3%B0%E5%84%AA"
                            + "&format=xml&bllimit=500&blnamespace=0&continue=")
                    .setListName("backlinks")
                    .setMapName("bl")
                    .setContinueElementName("blcontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList();
            return new DocumentUtil(result).nullable();
        } catch (IOException ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }
}
