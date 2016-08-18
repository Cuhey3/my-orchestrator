package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.consumers.ConsumerUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.GoogleTrendsParsingUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SnapshotGoogleTrendsConsumer extends SnapshotQueueConsumer {

    @Autowired
    private CamelContext context;

    @Override
    public void configure() {
        super.configure();

        from("direct:google_trends")
                .process((Exchange exchange) -> {
                    List<String> body = exchange.getIn().getBody(List.class);
                    body.add("神谷明");
                    List<String> collect = body.stream().map((str) -> str.replaceFirst(" \\(.+\\)$", ""))
                            .filter((str) -> str.length() > 0)
                            .map((str) -> {
                                try {
                                    return URLEncoder.encode(str, "UTF-8");
                                } catch (UnsupportedEncodingException ex) {
                                    return "";
                                }
                            })
                            .collect(Collectors.toList());
                    String body1 = Jsoup.connect("http://www.google.com/trends/fetchComponent?q=" + String.join(",", collect) + "&cid=TIMESERIES_GRAPH_0&export=3&hl=ja").ignoreContentType(true).execute().body();
                    exchange.getIn().setBody(body1);
                    ConsumerUtil.sendLog(this, "process", body1);
                })
                .choice().when(body().contains("google.visualization.Query.setResponse"))
                .setBody().javaScript("resource:classpath:googleTrendsParsing.js")
                .unmarshal().json(JsonLibrary.Gson)
                .process((Exchange exchange) -> {
                    GoogleTrendsParsingUtil util = new GoogleTrendsParsingUtil(exchange.getIn().getBody(Map.class), "神谷明", "2");
                    if (util.scaleIsValid()) {
                        exchange.getIn().setBody(util.createSuccessResults());
                    } else {
                        exchange.getIn().setBody(util.createFailedResults());
                    }
                })
                .otherwise()
                .setBody().constant(new ArrayList<>());
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil masterUtil = new MasterUtil(exchange);
        Document google_trends, google_trends_seiyu_all;
        try {
            google_trends
                    = masterUtil.kind(Kind.google_trends).findOrElseThrow();
        } catch (Exception ex) {
            google_trends = new Document();
        }
        try {
            google_trends_seiyu_all = masterUtil
                    .kind(Kind.google_trends_seiyu_all).findOrElseThrow();
        } catch (Exception ex) {
            return Optional.empty();
        }
        DocumentUtil util = new DocumentUtil();
        DocumentUtil addNewByKey = util.addNewByKey(
                google_trends, google_trends_seiyu_all, "title");
        List<String> collect = addNewByKey.getData().stream()
                .filter((map) -> !map.containsKey("trends")).limit(4)
                .map((map) -> (String) map.get("title"))
                .collect(Collectors.toList());
        try {
            if (!collect.isEmpty()) {
                ProducerTemplate pt = context.createProducerTemplate();
                DefaultExchange ex = new DefaultExchange(context);
                ex.getIn().setBody(collect);
                Exchange send = pt.send("direct:google_trends", ex);
                List<Map<String, Object>> body
                        = send.getIn().getBody(List.class);
                if (body == null || body.isEmpty()) {
                    throw new Exception();
                }
                for (String title : collect) {
                    Optional<Map<String, Object>> findFirst = body.stream()
                            .filter((map) -> nameIsEqualToTitle(map, title))
                            .map((map) -> {
                                map.put("title", title);
                                return map;
                            })
                            .findFirst();
                    if (findFirst.isPresent()) {
                        List<Map<String, Object>> data = addNewByKey.getData();
                        data.stream().filter((map)
                                -> ((String) map.get("title")).equals(title))
                                .forEach((map)
                                        -> map.put("trends", findFirst.get()));
                        addNewByKey.setData(data);
                    }
                }
                return addNewByKey.nullable();
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            ConsumerUtil.sendError(this, "doSnapshot", e);
            return Optional.empty();
        }
    }

    private boolean nameIsEqualToTitle(Map<String, Object> map, String title) {
        String name = (String) map.get("name");
        if (title.startsWith(name)
                || title.toLowerCase(Locale.US).equals(name)) {
            return true;
        } else {
            return false;
        }
    }
}
