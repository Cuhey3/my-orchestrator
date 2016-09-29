package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaRankingConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil util = new MasterUtil(exchange);
        List<String> eventNames = util.mapList(Kind.koepota_events)
                .stream().map((map) -> (String) map.get("c1"))
                .collect(Collectors.toList());
        MapList countedKoepotaSeiyu = new MapList(
                util.mapList(Kind.koepota_seiyu).stream().map((map) -> {
            String title = ((String) map.get("title"))
                    .replaceFirst(" \\(.+\\)", "");
            map.put("koepota_count", eventNames.stream()
                    .filter((str) -> str.contains(title)).count());
            return map;
        }).collect(Collectors.toList()));
        LinkedHashMap<Long, Integer> totalFemaleCounts = new LinkedHashMap<>();
        LinkedHashMap<Long, Integer> totalMaleCounts = new LinkedHashMap<>();
        LinkedHashMap<Long, Integer> totalAllCounts = new LinkedHashMap<>();
        makeTotalCounts(countedKoepotaSeiyu, totalFemaleCounts,
                totalMaleCounts, totalAllCounts);
        List result = countedKoepotaSeiyu.stream().map((map) -> {
            long count = (Long) map.get("koepota_count");
            if (map.get("gender").equals("f")) {
                map.put("koepota_female_ranking", totalFemaleCounts.get(count));
            } else if (map.get("gender").equals("m")) {
                map.put("koepota_male_ranking", totalMaleCounts.get(count));
            }
            map.put("koepota_ranking", totalAllCounts.get(count));
            return map;
        }).collect(Collectors.toList());
        return new DocumentUtil(result).nullable();
    }

    public Consumer<Entry<Long, Integer>> convertToTotal(LinkedHashMap<Long, Integer> counts, LinkedHashMap<Long, Integer> totalCounts) {
        return (entry) -> {
            Long key = entry.getKey();
            totalCounts.put(key, counts.entrySet().stream()
                    .filter((ent) -> ent.getKey() > key)
                    .map((ent) -> ent.getValue())
                    .mapToInt(Integer::intValue).sum() + 1);
        };
    }

    public void makeTotalCounts(MapList countedKoepotaSeiyu, LinkedHashMap<Long, Integer> totalFemaleCounts, LinkedHashMap<Long, Integer> totalMaleCounts, LinkedHashMap<Long, Integer> totalAllCounts) {
        LinkedHashMap<Long, Integer> femaleCounts, maleCounts, allCounts;
        femaleCounts = new LinkedHashMap<>();
        maleCounts = new LinkedHashMap<>();
        allCounts = new LinkedHashMap<>();
        countedKoepotaSeiyu.stream().forEach((map) -> {
            long count = (Long) map.get("koepota_count");
            if (map.get("gender").equals("f")) {
                femaleCounts.put(count, femaleCounts.getOrDefault(count, 0) + 1);
            } else if (map.get("gender").equals("m")) {
                maleCounts.put(count, maleCounts.getOrDefault(count, 0) + 1);
            }
            allCounts.put(count, allCounts.getOrDefault(count, 0) + 1);
        });
        femaleCounts.entrySet().stream()
                .forEach(convertToTotal(femaleCounts, totalFemaleCounts));
        maleCounts.entrySet().stream()
                .forEach(convertToTotal(maleCounts, totalMaleCounts));
        allCounts.entrySet().stream()
                .forEach(convertToTotal(allCounts, totalAllCounts));
    }
}
