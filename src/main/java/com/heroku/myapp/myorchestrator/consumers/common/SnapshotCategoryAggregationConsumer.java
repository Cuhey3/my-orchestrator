package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.JsonUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.bson.Document;

public abstract class SnapshotCategoryAggregationConsumer extends SnapshotQueueConsumer {

    protected String prefix;
    protected String resourcePath;
    protected JsonUtil jsonRoot;

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        Map<String, Boolean> targetCategories = new LinkedHashMap<>();
        Map<String, String> parentMap = new LinkedHashMap<>();
        init(targetCategories, parentMap);
        whileMain(targetCategories, parentMap);
        List result = result(targetCategories, parentMap);
        return new DocumentUtil(result).nullable();
    }

    private void init(Map<String, Boolean> targetCategories, Map<String, String> parentMap) {
        jsonRoot.map().get().keySet().forEach((key)
                -> targetCategories.put(prefix + key, Boolean.FALSE));
        jsonRoot.map().get().keySet().forEach((key)
                -> parentMap.put(prefix + key, (String) key));
    }

    private void whileMain(Map<String, Boolean> targetCategories, Map<String, String> parentMap) {
        int i = 0;
        while (targetCategories.values().contains(false)) {
            if (++i > 7) {
                break;
            }
            targetCategories.keySet().parallelStream()
                    .filter((key) -> !targetCategories.get(key))
                    .flatMap(mainFunc(targetCategories, parentMap))
                    .reduce(targetCategories, (foo, bar) -> {
                        String title = (String) bar.get("title");
                        if (!targetCategories.containsKey(title)) {
                            targetCategories.put(title, Boolean.FALSE);
                        }
                        return targetCategories;
                    }, (foo, bar) -> {
                        return foo;
                    });
        }
    }

    private Function<String, Stream<Map<String, Object>>> mainFunc(Map<String, Boolean> targetCategories, Map<String, String> parentMap) {
        return (key) -> {
            targetCategories.put(key, Boolean.TRUE);
            if (isNoContinue(parentMap, key)) {
                return new MapList().stream();
            } else {
                return apiRequestMain(key, parentMap);
            }
        };
    }

    private boolean isNoContinue(Map<String, String> parentMap, String key) {
        return jsonRoot.get(parentMap.get(key)).get("filter").get("no_continue")
                .list().get().stream()
                .anyMatch((str) -> key.equals(prefix + str));
    }

    private Stream<Map<String, Object>> apiRequestMain(String key, Map<String, String> parentMap) {
        String parent = parentMap.get(key);
        final List<Pattern> includePattern, excludePattern;
        includePattern = patterns(parent, "include");
        excludePattern = patterns(parent, "exclude");
        return apiRequest(key).stream()
                .filter(patternMatch(includePattern, true))
                .map((map) -> {
                    map.put("from", key);
                    parentMap.put((String) map.get("title"), parent);
                    return map;
                })
                .filter(patternMatch(excludePattern, false));
    }

    private List<Pattern> patterns(String parent, String type) {
        return jsonRoot.get(parent).get("filter").get(type).mapList().get()
                .stream().map(mapToPattern()).collect(Collectors.toList());
    }

    private Function<Map<String, Object>, Pattern> mapToPattern() {
        return (map) -> {
            Object[] args = Optional.ofNullable((List<String>) map.get("args"))
                    .orElse(new ArrayList<>()).stream().map((str) -> {
                switch (str) {
                    case "recent_years":
                        return "(2015|2016|2017)年";
                    case "years":
                        return "[\\d]{4}年";
                }
                return "";
            }).toArray();
            String pattern = (String) map.get("pattern");
            if (args.length > 0) {
                return Pattern.compile("^Category:.*" + String.format(pattern, args));
            } else {
                return Pattern.compile("^Category:.*" + pattern);
            }
        };
    }

    private List<Map<String, Object>> apiRequest(String key) {
        try {
            return new MediawikiApiRequest().setApiParam(
                    "action=query&list=categorymembers"
                    + "&cmtitle=" + URLEncoder.encode(key, "UTF-8")
                    + "&cmlimit=500&cmnamespace=14"
                    + "&cmprop=title|ids|sortkeyprefix&format=xml")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .getResultByMapList();
        } catch (Throwable t) {
            System.out.println("failed!!!");
            throw new RuntimeException();
        }
    }

    private Predicate<Map<String, Object>> patternMatch(List<Pattern> patterns, boolean flag) {
        return (map) -> {
            if (patterns.isEmpty()) {
                return true;
            } else {
                String title = (String) map.get("title");
                return patterns.stream()
                        .allMatch((p) -> flag == p.matcher(title).find());
            }
        };
    }

    private List result(Map<String, Boolean> targetCategories, Map<String, String> parentMap) {
        return targetCategories.keySet().stream().map((key) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", key);
            map.put("from", prefix + parentMap.get(key));
            return map;
        }).collect(Collectors.toList());
    }
}
