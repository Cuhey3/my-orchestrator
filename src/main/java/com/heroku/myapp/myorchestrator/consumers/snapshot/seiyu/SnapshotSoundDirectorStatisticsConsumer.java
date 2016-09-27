package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSoundDirectorStatisticsConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        Set<String> ignore = new HashSet<>(Arrays.asList(new String[]{"藤原啓治", "井上和彦 (声優)","千葉繁","宮村優子 (声優)"}));
        MapList list = util.mapList(Kind.pages_mutual_sound_director).filteredList((map) -> {
            List<String> director = (List<String>) map.get("director");
            return !director.stream().allMatch((d) -> ignore.contains(d));
        });
        int size = list.size();
        List<Map<String, Object>> result = list.stream().flatMap((map) -> {
            List<String> director = (List<String>) map.get("director");
            String title = (String) map.get("title");
            return director.stream().map((name) -> {
                Map<String, String> part = new LinkedHashMap<>();
                part.put("key", name);
                part.put("title", title);
                return part;
            });
        }).collect(Collectors.groupingBy((part) -> part.get("key")))
                .entrySet().stream()
                .filter((entry) -> !ignore.contains(entry.getKey()))
                .map((entry) -> {
                    String director = entry.getKey();
                    Set<String> titles = entry.getValue().stream().map((m) -> m.get("title"))
                            .collect(Collectors.toSet());
                    Map<String, Object> resultMap = new LinkedHashMap<>();
                    resultMap.put("director", director);
                    resultMap.put("count", titles.size());
                    resultMap.put("percentage", titles.size() * 100d / size);
                    return resultMap;
                }).sorted((Map<String, Object> o1, Map<String, Object> o2) -> {
            double d1 = (double) o1.get("percentage");
            double d2 = (double) o2.get("percentage");
            if (d1 == d2) {
                return 0;
            } else if (d1 > d2) {
                return -1;
            } else {
                return 1;
            }
        }).collect(Collectors.toList());
        return new DocumentUtil(result).nullable();
    }
}
