package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.consumers.DiffQueueConsumer;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DiffRecentchangesOfSeiyuConsumer extends DiffQueueConsumer {

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        List<Map<String, Object>> newList = new DocumentUtil().setDocument(snapshot).getData();
        List<Map<String, Object>> oldList = new DocumentUtil().setDocument(master).getData();
        Map<Object, Map<String, Object>> oldMap = new LinkedHashMap<>();
        oldList.stream().forEach((map) -> oldMap.put(map.get("title"), map));
        List<Map<String, Object>> collect = newList.stream()
                .filter((map) -> map.containsKey("revid"))
                .filter((map) -> {
                    Object title = map.get("title");
                    if (oldMap.containsKey(title)) {
                        Map<String, Object> old = oldMap.get(title);
                        if (old.containsKey("revid")) {
                            if (old.get("revid").equals(map.get("old_revid"))) {
                                return true;
                            } else {
                                throw new RuntimeException();
                            }
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return Optional.empty();
        } else {
            return new DocumentUtil().setDiff(collect).nullable();
        }
    }
}
