package com.heroku.myapp.myorchestrator.consumers.snapshot.external;

import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.KoepotaEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaEventsConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            org.jsoup.nodes.Document doc
                    = Jsoup.connect("http://www.koepota.jp/eventschedule/")
                    .maxBodySize(Integer.MAX_VALUE).timeout(Integer.MAX_VALUE)
                    .get();
            doc.select("#eventschedule tr:eq(0)").remove();
            List<org.bson.Document> collect = doc.select("#eventschedule tr")
                    .stream().map((el) -> new KoepotaEvent(el).getDocument())
                    .collect(Collectors.toList());
            return new DocumentUtil(collect).nullable();
        } catch (Exception e) {
            util().sendError("doSnapshot", e);
            return Optional.empty();
        }
    }
}
