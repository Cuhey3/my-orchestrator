package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import com.heroku.myorchestrator.util.content.MediawikiApiRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuTemplateIncludePagesConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            List<Map<String, Object>> result = new MediawikiApiRequest()
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
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
