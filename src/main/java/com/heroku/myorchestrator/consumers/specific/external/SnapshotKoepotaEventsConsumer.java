package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.KoepotaEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaEventsConsumer extends SnapshotRouteBuilder {

    public SnapshotKoepotaEventsConsumer() {
        kind(Kind.koepota_events);
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            org.jsoup.nodes.Document doc
                    = Jsoup.connect("http://www.koepota.jp/eventschedule/")
                    .maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE)
                    .get();

            final String host = "http://www.koepota.jp/eventschedule/";
            doc.select("#eventschedule tr:eq(0)").remove();
            Elements select = doc.select("#eventschedule tr");
            List<org.bson.Document> collect = select.stream()
                    .map((el) -> {
                        int size = el.select("td.day").size();
                        return new KoepotaEvent(
                                el.select("td.title a").attr("href")
                                .replace(host, ""),
                                size > 0 ? el.select("td.day").get(0).text() : "",
                                el.select("td.title").text(),
                                el.select("td.hall").text(),
                                el.select("td.number").text(),
                                size > 1 ? el.select("td.day").get(1).text() : "")
                                .getDocument();

                    })
                    .collect(Collectors.toList());
            document.append("data", collect);
            return Optional.ofNullable(document);
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, e);
            return Optional.empty();
        }
    }
}
