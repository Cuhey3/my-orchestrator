package com.heroku.myorchestrator.util.content;

import java.util.LinkedHashMap;
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

    public static void createPrefix(Document document, String... keys) {
        document.append("prefix", new LinkedHashMap());
        for (String key : keys) {
            createPrefixSpecific(document, key);
        }
    }

    private static void createPrefixSpecific(Document document, String key) {
        List<Map<String, Object>> list = document.get("data", List.class);
        String firstValue = (String) list.get(0).get(key);
        int len = firstValue.length();
        String prefix = null;
        for (int i = len; i > 10; i--) {
            String prefixSuggest = firstValue.substring(0, i);
            if (list.stream().allMatch((map)
                    -> ((String) map.get(key)).startsWith(prefixSuggest))) {
                prefix = prefixSuggest;
                int length = prefix.length();
                list.stream().forEach((map) -> {
                    map.put(key, ((String) map.get(key))
                            .substring(length));
                });
                break;
            }
        }
        if (prefix != null) {
            Map prefixs = document.get("prefix", Map.class);
            prefixs.put(key, prefix);
            document.put("prefix", prefixs);
        }
    }

    public static Document restorePrefix(Document document) {
        Map<String, String> prefixs = document.get("prefix", Map.class);
        if (prefixs != null) {
            prefixs.entrySet().stream().forEach((entry) -> {
                restorePrefixSpecific(document, entry.getKey(), entry.getValue());
            });
        }
        return document;
    }

    private static void restorePrefixSpecific(Document document, String key, String prefix) {
        List<Map<String, Object>> list = document.get("data", List.class);
        list.stream().forEach((map) -> {
            map.put(key, prefix + map.get(key));
        });
        document.append("data", list);
    }
}
