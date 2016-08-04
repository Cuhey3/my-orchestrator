package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_has_recentchanges;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    public void configure() throws Exception {
        super.configure();
        from("timer:foo?repeatCount=1")
                .process((Exchange exchange) -> {
                    //exchange.getIn().setBody(Jsoup.connect("http://www.google.com/trends/fetchComponent?q=xxx&cid=TIMESERIES_GRAPH_0&export=3").ignoreContentType(true).execute().body());
                    exchange.getIn().setBody("");
                })
                .setBody().javaScript(""
                        + "var google ={};\n"
                        + "google.visualization = {};\n"
                        + "google.visualization.Query = {};\n"
                        + "google.visualization.Query.setResponse"
                        + " = function(obj){return obj;};"
                        + "result = JSON.stringify(eval(body));")
                .unmarshal().json(JsonLibrary.Gson)
                .process((Exchange exchange) -> {
                    Map<String, Object> map, table;
                    List<Map<String, Object>> rows;
                    map = exchange.getIn().getBody(Map.class);
                    table = (Map) map.get("table");
                    rows = (List) table.get("rows");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy'年'MM'月'");
                    Calendar cal = Calendar.getInstance();

                    rows.stream().map((obj) -> (List) obj.get("c"))
                            .forEach((List list) -> {
                                Object time = ((Map) list.get(0)).get("v");
                                System.out.println(time);
                                try {
                                    Date parse = sdf.parse((String) time);
                                    cal.setTime(parse);
                                    cal.add(Calendar.DATE, 1);
                                    System.out.println(sdf2.format(cal.getTime()));
                                } catch (ParseException ex) {
                                }
                                System.out.println(((Map) list.get(1)).get("f") + "\t" + ((Map) list.get(2)).get("f"));
                            });
                })
                .to("log:foo");
    }

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
