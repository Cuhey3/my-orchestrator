package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.GoogleTrendsParsingUtil;
import com.heroku.myapp.commons.util.content.MapList;
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
                .routeId("snapshot_google_trends_sub_route")
                .process((Exchange exchange) -> {
                    List<String> body = exchange.getIn().getBody(List.class);
                    body.add("名塚佳織");
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
                    util().sendLog("process", body1);
                })
                .id("process:request_to_google_trends_api")
                .choice().when(body().contains("google.visualization.Query.setResponse"))
                .id("when:responseIsValid")
                .setBody().javaScript("resource:classpath:googleTrendsParsing.js")
                .unmarshal().json(JsonLibrary.Gson)
                .process((Exchange exchange) -> {
                    GoogleTrendsParsingUtil util = new GoogleTrendsParsingUtil(exchange.getIn().getBody(Map.class), "名塚佳織", "2");
                    if (util.scaleIsValid()) {
                        exchange.getIn().setBody(util.createSuccessResults());
                    } else {
                        exchange.getIn().setBody(util.createFailedResults());
                    }
                })
                .id("process:parse_google_trends_response")
                .otherwise()
                .setBody().constant(new ArrayList<>());
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        MapList addNewByKey = util.mapList(Kind.google_trends).addNewByKey(
                util.mapList(Kind.google_trends_seiyu_all), "title");
        List<String> targetNames = addNewByKey.stream()
                .filter((map) -> firstFilter(map))
                .filter((map) -> filterTarget(map, "神谷明")).limit(4)
                .map((map) -> (String) map.get("title"))
                .collect(Collectors.toList());
        try {
            if (!targetNames.isEmpty()) {
                ProducerTemplate pt = context.createProducerTemplate();
                DefaultExchange ex = new DefaultExchange(context);
                ex.getIn().setBody(targetNames);
                Exchange send = pt.send("direct:google_trends", ex);
                MapList body = send.getIn().getBody(MapList.class);
                if (body.isEmpty()) {
                    throw new Exception();
                }
                for (String title : targetNames) {
                    Optional<Map<String, Object>> findFirst = body.stream()
                            .filter((map) -> nameIsEqualToTitle(map, title))
                            .map((map) -> {
                                map.put("title", title);
                                return map;
                            })
                            .findFirst();
                    if (findFirst.isPresent()) {
                        Map<String, Object> get = findFirst.get();
                        addNewByKey.stream().filter((map)
                                -> ((String) map.get("title")).equals(title))
                                .forEach((map) -> map.put("trends", get));
                    }
                }
                return new DocumentUtil(addNewByKey).nullable();
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            util().sendError("doSnapshot", e);
            return Optional.empty();
        }
    }

    private boolean nameIsEqualToTitle(Map<String, Object> map, String title) {
        String name = ((String) map.get("name")).toLowerCase(Locale.US);
        title = title.toLowerCase(Locale.US);
        return title.equals(name) || title.startsWith(name);
    }

    private boolean filterTarget(Map<String, Object> map, String newScale) {
        if (map.get("title").equals(newScale)) {
            return false;
        }
        if (!map.containsKey("trends")) {
            return true;
        }
        Map<String, Object> trends = (Map<String, Object>) map.get("trends");
        String scale = (String) trends.get("scale");
        return !scale.equals(newScale);
    }

    private boolean firstFilter(Map<String, Object> map) {
        if (!map.containsKey("trends")) {
            return true;
        } else {
            Map<String, Object> trends = (Map<String, Object>) map.get("trends");
            return trends.get("status").equals("failed");
        }
    }
}
