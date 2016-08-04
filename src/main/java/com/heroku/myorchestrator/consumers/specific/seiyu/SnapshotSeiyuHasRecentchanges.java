package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.MongoUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import static com.heroku.myorchestrator.util.content.DocumentUtil.getData;
import com.mongodb.client.MongoCursor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuHasRecentchanges extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            MongoUtil mongoUtil = new MongoUtil(exchange);
            MongoCursor<Document> iterator
                    = mongoUtil.target(MongoTarget.SEIYULAB).database()
                    .getCollection("wikirc").find().iterator();
            Set<String> seiyuNames = new HashSet<>();
            while (iterator.hasNext()) {
                Document next = iterator.next();
                if (next.get("type", String.class).equals("seiyu")) {
                    seiyuNames.add(next.get("title", String.class));
                } else {
                    List<Map<String, Object>> changedList
                            = next.get("changed", List.class);
                    changedList.stream().filter((map)
                            -> ((String) map.get("type")).equals("add"))
                            .map((map) -> (Map<String, Object>) map.get("data"))
                            .map((map) -> (String) map.get("title"))
                            .forEach(seiyuNames::add);
                }
            }
            List<Map<String, Object>> collect = getData(util
                    .getLatest(seiyu_category_members_include_template))
                    .stream().filter((map)
                            -> seiyuNames.contains((String) map.get("title")))
                    .collect(Collectors.toList());
            return new DocumentUtil(collect).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }

}
