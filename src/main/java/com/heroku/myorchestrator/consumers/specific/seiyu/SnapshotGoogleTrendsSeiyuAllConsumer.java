package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_has_recentchanges;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.ArrayList;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsSeiyuAllConsumer extends SnapshotQueueConsumer {

    /*@Override
    public void configure() throws Exception {
        super.configure();
        from("timer:foo?repeatCount=1")
                .process((Exchange exchange) -> {
                    //exchange.getIn().setBody(Jsoup.connect("http://www.google.com/trends/fetchComponent?q=xxx&cid=TIMESERIES_GRAPH_0&export=3").ignoreContentType(true).execute().body());
                    exchange.getIn().setBody("");
                })
                .setBody()
                .javaScript("resource:classpath:googleTrendsParsing.js")
                .unmarshal().json(JsonLibrary.Gson)
                .process((Exchange exchange) -> {
                    GoogleTrendsParsingUtil util
                        = new GoogleTrendsParsingUtil(
                            exchange.getIn().getBody(Map.class, "foo", "1");
                    if (util.scaleIsValid()) {
                        System.out.println(util.createSuccessResults());
                    } else {
                        System.out.println(util.createFailedResults());
                    }
                })
                .to("log:foo");
    }*/

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            Document findLatest = masterUtil.findLatest()
                    .orElse(new Document("data", new ArrayList<>()));
            DocumentUtil util = new DocumentUtil();
            Document product = util.productByTitle(
                    masterUtil.getLatest(koepota_seiyu_all),
                    masterUtil.getLatest(seiyu_has_recentchanges))
                    .getDocument();
            return util.addNewByKey(findLatest, product, "title").nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
