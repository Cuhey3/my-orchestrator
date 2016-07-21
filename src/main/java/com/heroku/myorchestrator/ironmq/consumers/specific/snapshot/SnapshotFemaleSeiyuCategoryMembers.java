package com.heroku.myorchestrator.ironmq.consumers.specific.snapshot;

import com.heroku.myorchestrator.util.IronmqUtil;
import com.heroku.myorchestrator.util.MediawikiApiRequest;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFemaleSeiyuCategoryMembers extends RouteBuilder {

    private final String kind = "female_seiyu_category_members";
    private final IronmqUtil ironmqUtil = new IronmqUtil().kind(kind);

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.snapshot().consumeUri())
                .routeId("snapshot_" + kind)
                .filter(simple("${exchangeProperty.CamelBatchComplete}"))
                .process((Exchange exchange) -> {
                    Document document = new Document();
                    doSnapshot(document);
                    new SnapshotUtil(exchange)
                            .saveDocument(document)
                            .updateMessage(document);
                })
                .to(ironmqUtil.diff().postUri());
    }

    private void doSnapshot(Document document) throws Exception {
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
        mapList.forEach((m) -> m.put("gender", "f"));
        document.append("data", mapList);
    }
}
