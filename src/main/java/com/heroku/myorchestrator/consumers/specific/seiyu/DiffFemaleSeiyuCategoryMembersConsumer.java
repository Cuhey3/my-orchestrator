package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.SenseType;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.DiffUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.actions.SnapshotUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffFemaleSeiyuCategoryMembersConsumer extends ConsumerRouteBuilder {

    public DiffFemaleSeiyuCategoryMembersConsumer() {
        setKind(Kind.female_seiyu_category_members);
        routeUtil.diff();
    }

    @Override
    public void configure() throws Exception {
        from(ironmqUtil.diff().consumeUri())
                .routeId(routeUtil.id())
                .filter(routeUtil.camelBatchComplete())
                .filter((Exchange exchange) -> {
                    try {
                        Optional<Document> snapshotOptional
                                = new SnapshotUtil(exchange).loadDocument();
                        Optional<Document> masterOptional
                                = new MasterUtil(exchange).loadLatestDocument();
                        if (!snapshotOptional.isPresent()) {
                            return false;
                        }
                        if (!masterOptional.isPresent()) {
                            masterIsEmptyLogic(exchange);
                            return true;
                        }
                        Document snapshot
                                = snapshotOptional.orElse(new Document());
                        Document master = masterOptional.orElse(new Document());
                        Optional<Document> diffOptional
                                = compareLogic(master, snapshot);
                        if (!diffOptional.isPresent()) {
                            System.out.println("not updated...");
                            return false;
                        } else {
                            Document diff = diffOptional.get();
                            MessageUtil.writeObjectId(exchange,
                                    "compared_master_id", master);
                            new DiffUtil(exchange)
                                    .saveDocument(diff)
                                    .updateMessage(diff);
                            return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .to(ironmqUtil.completion().postUri());
    }

    public void masterIsEmptyLogic(Exchange exchange) {
        MessageUtil.updateMessage(exchange, "compared_master_id",
                SenseType.EMPTY.expression());
        System.out.println(this.kind.expression() + " master is empty.");
    }

    public Optional<Document> compareLogic(Document master, Document snapshot) {
        System.out.println("comparing... " + master + " to " + snapshot);
        Optional<Document> foo = foo(master, snapshot);
        if (foo.isPresent()) {
            System.out.println("updated!" + snapshot);
            return foo;
        } else {
            return Optional.empty();
        }
    }

    public Optional<Document> foo(Document master, Document snapshot) {
        List<Map<String, String>> prev = master.get("data", List.class);
        List<Map<String, String>> next = snapshot.get("data", List.class);
        prev.forEach((map) -> map.put("type", "remove"));
        next.forEach((map) -> map.put("type", "add"));
        List<Map<String, String>> collect = prev.stream().filter((map1) -> {
            final String map1Title = map1.get("title");
            return !next.stream().anyMatch((map2) -> map2.get("title").equals(map1Title));
        }).collect(Collectors.toList());
        next.stream().filter((map2) -> {
            final String map2Title = map2.get("title");
            return !prev.stream().anyMatch((map1) -> map1.get("title").equals(map2Title));
        }).forEach(collect::add);
        if (collect.size() > 0) {
            Document document = new Document();
            document.append("diff", collect);
            return Optional.ofNullable(document);
        } else {
            return Optional.empty();
        }
    }
}
