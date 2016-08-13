package com.heroku.myorchestrator.consumers.specific.external;

import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.definitions.util.consumers.IronmqUtil;
import com.heroku.definitions.util.content.DocumentUtil;
import com.heroku.definitions.util.content.KoepotaEvent;
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
            IronmqUtil.sendError(this, "doSnapshot", e);
            return Optional.empty();
        }
    }
}
