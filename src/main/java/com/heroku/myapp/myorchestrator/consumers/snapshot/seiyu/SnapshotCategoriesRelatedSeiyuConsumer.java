package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.google.gson.Gson;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.JsonUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotCategoriesRelatedSeiyuConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        String resourcePath = "../../../../../../../wikipedia_category_filter.json";
        InputStream resourceAsStream = this.getClass()
                .getResourceAsStream(resourcePath);
        Map fromJson;
        try (BufferedReader buffer = new BufferedReader(
                new InputStreamReader(resourceAsStream, "UTF-8"))) {
            fromJson = new Gson().fromJson(buffer.lines()
                    .collect(Collectors.joining("\n")), Map.class);
        } catch (IOException ex) {
            throw new RuntimeException();
        }
        Map<String, String> parentMap = new LinkedHashMap<>();
        Map<String, Boolean> categories = new LinkedHashMap<>();
        Set<String> pages = new HashSet<>();
        JsonUtil jsonUtil = new JsonUtil(fromJson).get("categories");
        jsonUtil.map().get().keySet().forEach((key) -> categories.put("Category:" + key, Boolean.FALSE));
        jsonUtil.map().get().keySet().forEach((key) -> parentMap.put("Category:" + key, (String) key));
        int i = 0;
        while (categories.values().stream().filter((b) -> !b).findFirst().isPresent()) {
            if (i > 7) {
                break;
            }
            categories.keySet().parallelStream()
                    .filter((key) -> !categories.get(key))
                    .flatMap((key) -> {
                        String parent = parentMap.get(key);
                        List<Map<String, Object>> includeRaw = jsonUtil.get(parent).get("filter").get("include").list().get();
                        List<Pattern> includePattern = includeRaw.stream().map((map) -> {
                            Object[] args = Optional.ofNullable((List<String>) map.get("args")).orElse(new ArrayList<>())
                                    .stream().map((str) -> {
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
                        }).collect(Collectors.toList());
                        List<Map<String, Object>> excludeRaw = jsonUtil.get(parent).get("filter").get("exclude").list().get();
                        List<Pattern> excludePattern = excludeRaw.stream().map((map) -> {
                            Object[] args = Optional.ofNullable((List<String>) map.get("args")).orElse(new ArrayList<>())
                                    .stream().map((str) -> {
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
                        }).collect(Collectors.toList());
                        Set<String> noContinue = (Set<String>) jsonUtil.get(parent).get("filter").get("no_continue").list().get().stream().map((str) -> "Category:" + str).collect(Collectors.toSet());
                        categories.put(key, Boolean.TRUE);
                        if (noContinue.contains(key)) {
                            return new ArrayList<Map<String, Object>>().stream();
                        }
                        try {
                            return new MediawikiApiRequest()
                                    .setApiParam("action=query&list=categorymembers"
                                            + "&cmtitle="
                                            + URLEncoder.encode(key, "UTF-8")
                                            + "&cmlimit=500"
                                            + "&cmnamespace=14"
                                            + "&format=xml"
                                            + "&continue="
                                            + "&cmprop=title|ids|sortkeyprefix")
                                    .setListName("categorymembers").setMapName("cm")
                                    .setContinueElementName("cmcontinue")
                                    .getResultByMapList().stream()
                                    .filter((map) -> {
                                        map.put("from", key);
                                        if (includePattern.isEmpty()) {
                                            return true;
                                        } else {
                                            String title = (String) map.get("title");
                                            return includePattern.stream()
                                                    .allMatch((p) -> p.matcher(title).find());
                                        }
                                    })
                                    .filter((map) -> {
                                        String title = (String) map.get("title");
                                        parentMap.put(title, parent);
                                        if (excludePattern.isEmpty()) {
                                            return true;
                                        } else {
                                            return excludePattern.stream()
                                                    .allMatch((p) -> !p.matcher(title).find());
                                        }
                                    });
                        } catch (Throwable t) {
                            System.out.println("failed!!!");
                            throw new RuntimeException();
                        }
                    }).reduce(categories, (foo, bar) -> {
                String title = (String) bar.get("title");
                if (!categories.containsKey(title)/* && p.matcher(title).find()*/) {
                    categories.put(title, Boolean.FALSE);
                }
                return categories;
            }, (foo, bar) -> {
                return foo;
            });
        }
        List<Map<String, Object>> collect = categories.keySet().stream().map((key) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", key);
            map.put("from", "Category:"+parentMap.get(key));
            return map;
        }).collect(Collectors.toList());
        return new DocumentUtil().setData(collect).nullable();
    }
}
