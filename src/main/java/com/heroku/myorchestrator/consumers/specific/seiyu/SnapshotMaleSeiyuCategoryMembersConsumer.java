package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.content.MediawikiApiRequest;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotMaleSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    public SnapshotMaleSeiyuCategoryMembersConsumer() {
        setKind(Kind.male_seiyu_category_members);
    }

    @Override
    protected Document doSnapshot(Exchange exchange, Document document) throws Exception {
        List<Map<String, Object>> mapList
                = new MediawikiApiRequest()
                .setApiParam("action=query&list=categorymembers"
                        + "&cmtitle=Category:%E6%97%A5%E6%9C%AC%E3%81%AE%E5%A5%B3%E6%80%A7%E5%A3%B0%E5%84%AA"
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
        document.append("data", mapList);
        return document;
    }
}
