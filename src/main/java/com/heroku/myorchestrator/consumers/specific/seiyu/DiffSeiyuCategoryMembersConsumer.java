package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffSeiyuCategoryMembersConsumer extends DiffRouteBuilder {

    public DiffSeiyuCategoryMembersConsumer() {
        kind(Kind.seiyu_category_members);
    }

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
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
        List<Map<String, String>> prev, next, collect;
        prev = master.get("data", List.class);
        next = snapshot.get("data", List.class);
        prev.forEach((map) -> map.put("type", "remove"));
        next.forEach((map) -> map.put("type", "add"));
        Set<String> prevTitleSet, nextTitleSet;
        prevTitleSet = prev.stream()
                .map((Map<String, String> map) -> map.get("title"))
                .collect(Collectors.toSet());
        nextTitleSet = next.stream()
                .map((Map<String, String> map) -> map.get("title"))
                .collect(Collectors.toSet());
        collect = prev.stream()
                .filter((map) -> !nextTitleSet.contains(map.get("title")))
                .collect(Collectors.toList());
        next.stream()
                .filter((map) -> !prevTitleSet.contains(map.get("title")))
                .forEach(collect::add);
        if (collect.size() > 0) {
            Document document = new Document("diff", collect);
            return Optional.ofNullable(document);
        } else {
            return Optional.empty();
        }
    }
}
