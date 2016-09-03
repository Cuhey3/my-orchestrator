package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaRankingConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MasterUtil masterUtil = new MasterUtil(exchange);
        DocumentUtil util = new DocumentUtil();
        List<String> collect = util.
                setDocument(masterUtil.findOrElseThrow(Kind.koepota_events))
                .getData().stream().map((map) -> (String) map.get("c1"))
                .collect(Collectors.toList());
        List<Map<String, Object>> countedKoepotaSeiyu = util.setDocument(
                masterUtil.findOrElseThrow(Kind.koepota_seiyu)).getData()
                .stream().map((map) -> {
                    String title = ((String) map.get("title"))
                            .replaceFirst(" \\(.+\\)", "");
                    map.put("koepota_count", collect.stream()
                            .filter((str) -> str.contains(title)).count());
                    return map;
                })
                .collect(Collectors.toList());

        LinkedHashMap<Long, Integer> femaleMap, maleMap, allMap,
                convertedFemaleMap, convertedMaleMap, convertedAllMap;
        femaleMap = new LinkedHashMap<>();
        maleMap = new LinkedHashMap<>();
        allMap = new LinkedHashMap<>();
        convertedFemaleMap = new LinkedHashMap<>();
        convertedMaleMap = new LinkedHashMap<>();
        convertedAllMap = new LinkedHashMap<>();
        countedKoepotaSeiyu.stream()
                .filter((map) -> map.get("gender").equals("f"))
                .forEach((map) -> {
                    long count = (Long) map.get("koepota_count");
                    femaleMap.put(count, femaleMap.getOrDefault(count, 0) + 1);
                    allMap.put(count, allMap.getOrDefault(count, 0) + 1);
                });
        countedKoepotaSeiyu.stream()
                .filter((map) -> map.get("gender").equals("m"))
                .forEach((map) -> {
                    long count = (Long) map.get("koepota_count");
                    maleMap.put(count, maleMap.getOrDefault(count, 0) + 1);
                    allMap.put(count, allMap.getOrDefault(count, 0) + 1);
                });
        femaleMap.entrySet().stream().forEach((ent) -> {
            Long key = ent.getKey();
            convertedFemaleMap.put(key, femaleMap.entrySet().stream()
                    .filter((entry) -> entry.getKey() > key)
                    .map((entry) -> entry.getValue())
                    .mapToInt(Integer::intValue).sum() + 1);
        });
        maleMap.entrySet().stream().forEach((ent) -> {
            Long key = ent.getKey();
            convertedMaleMap.put(key, maleMap.entrySet().stream()
                    .filter((entry) -> entry.getKey() > key)
                    .map((entry) -> entry.getValue())
                    .mapToInt(Integer::intValue).sum() + 1);
        });
        allMap.entrySet().stream().forEach((ent) -> {
            Long key = ent.getKey();
            convertedAllMap.put(key, allMap.entrySet().stream()
                    .filter((entry) -> entry.getKey() > key)
                    .map((entry) -> entry.getValue())
                    .mapToInt(Integer::intValue).sum() + 1);
        });
        List<Map<String, Object>> result = countedKoepotaSeiyu.stream().map((map) -> {
            long count = (Long) map.get("koepota_count");
            if (map.get("gender").equals("f")) {
                map.put("koepota_female_ranking", convertedFemaleMap.get(count));

            } else {
                map.put("koepota_male_ranking", convertedMaleMap.get(count));
            }
            map.put("koepota_ranking", convertedAllMap.get(count));
            return map;
        }).collect(Collectors.toList());

        return new DocumentUtil().setData(result).nullable();
    }
}
