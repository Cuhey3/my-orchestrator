package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.MongoUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
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
public class SnapshotSeiyuHasRecentchanges extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            Document scmit = masterUtil
                    .kind(Kind.seiyu_category_members_include_template)
                    .findLatest().get();
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
                    changedList.stream()
                            .filter((map)
                                    -> ((String) map.get("type")).equals("add"))
                            .map((map) -> (Map<String, Object>) map.get("data"))
                            .map((map) -> (String) map.get("title"))
                            .forEach(seiyuNames::add);
                }
            }
            List<Map<String, Object>> scmitList = scmit.get("data", List.class);
            List<Map<String, Object>> collect = scmitList.stream()
                    .filter((map)
                            -> seiyuNames.contains((String) map.get("title")))
                    .collect(Collectors.toList());
            document.append("data", collect);
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

}
