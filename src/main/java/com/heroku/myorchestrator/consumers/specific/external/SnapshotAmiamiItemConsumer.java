package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiItemConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            String amiamiUrl = "http://www.amiami.jp/top/page/cal/goods.html";
            org.jsoup.nodes.Document doc = Jsoup.connect(amiamiUrl)
                    .maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE)
                    .get();
            doc.select(".listitem:has(.originaltitle:matches(^$))").remove();
            List<Map<String, Object>> collect = doc.select(".listitem")
                    .stream().map((e) -> {
                        Map<String, Object> map = new HashMap<>();
                        String title = e.select(".originaltitle").text();
                        map.put("img", e.select("img").attr("src")
                                .replace("thumbnail", "main"));
                        map.put("url", e.select(".name a").attr("href"));
                        map.put("name", e.select("ul li").text());
                        map.put("release", e.select(".releasedatetext").text());
                        map.put("price", e.select(".price").text());
                        map.put("orig", title);
                        return map;
                    }).collect(Collectors.toList());
            document.append("data", collect);
            DocumentUtil.createPrefix(document, "img", "url");
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
