package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
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

    public SnapshotSeiyuTemplateIncludePagesConsumer() {
        kind(Kind.seiyu_template_include_pages);
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            List<Map<String, Object>> resultByMapList = new MediawikiApiRequest()
                    .setApiParam("action=query&list=backlinks&bltitle=Template:%E5%A3%B0%E5%84%AA&format=xml&bllimit=500&blnamespace=0&continue=")
                    .setListName("backlinks")
                    .setMapName("bl")
                    .setContinueElementName("blcontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList();
            document.append("data", resultByMapList);
            return Optional.ofNullable(document);
        } catch (IOException ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}