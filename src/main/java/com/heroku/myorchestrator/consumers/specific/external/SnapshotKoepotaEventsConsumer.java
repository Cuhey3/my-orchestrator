package com.heroku.myorchestrator.consumers.specific.external;

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

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            org.jsoup.nodes.Document doc
                    = Jsoup.connect("http://www.koepota.jp/eventschedule/")
                    .maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE)
                    .get();
            doc.select("#eventschedule tr:eq(0)").remove();
            Elements select = doc.select("#eventschedule tr");
            List<org.bson.Document> collect = select.stream()
                    .map((el) -> new KoepotaEvent(el).getDocument())
                    .collect(Collectors.toList());
            document.append("data", collect);
            return Optional.ofNullable(document);
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, e);
            return Optional.empty();
        }
    }
}
