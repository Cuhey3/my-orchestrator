package com.heroku.myorchestrator.util.content;

import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.Optional;
import org.bson.Document;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentUtil {

    public static Optional<Document> productSetByKey(Document sieved, Document filter, final String key) {
        List<Map<String, Object>> sievedList, filterList, collect;
        sievedList = sieved.get("data", List.class);
        filterList = filter.get("data", List.class);
        Set<Object> filterSet = filterList.stream()
                .map((map) -> map.get(key))
                .collect(Collectors.toSet());
        collect = sievedList.stream()
                .filter((map) -> filterSet.contains(map.get(key)))
                .collect(Collectors.toList());
        return Optional.ofNullable(new Document("data", collect));
    }

    public static Optional<Document> productSetByTitle(Document sieved, Document filter) {
        return productSetByKey(sieved, filter, "title");
    }
}
