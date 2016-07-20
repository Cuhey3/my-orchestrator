package com.heroku.myorchestrator.ironmq.consumers.specific.snapshot;

import static com.heroku.myorchestrator.util.IronmqUtil.consumeQueueUri;
import static com.heroku.myorchestrator.util.IronmqUtil.postQueueUri;
import com.heroku.myorchestrator.util.MediawikiApiRequest;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFemaleSeiyuCategoryMembers extends RouteBuilder {

    @Autowired
    ApplicationContext applicationContext;
    String collectionKind = "female_seiyu_category_members";

    @Override
    public void configure() throws Exception {
        from(consumeQueueUri("snapshot", collectionKind, 60))
                .routeId("snapshot_" + collectionKind)
                .filter(simple("${exchangeProperty.CamelBatchComplete}"))
                .process((Exchange exchange) -> {
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
                    Document document = new Document().append("data", mapList);
                    new MongoUtil(applicationContext)
                            .insertOne("snapshot", collectionKind, document);
                    new MessageUtil(exchange).writeObjectId("snapshot_id", document);
                })
                .to(postQueueUri("diff", collectionKind));
    }
}
