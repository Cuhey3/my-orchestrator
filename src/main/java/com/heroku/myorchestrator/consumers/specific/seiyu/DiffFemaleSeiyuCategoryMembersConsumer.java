package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffFemaleSeiyuCategoryMembersConsumer extends DiffRouteBuilder {

    public DiffFemaleSeiyuCategoryMembersConsumer() {
        setKind(Kind.female_seiyu_category_members);
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
