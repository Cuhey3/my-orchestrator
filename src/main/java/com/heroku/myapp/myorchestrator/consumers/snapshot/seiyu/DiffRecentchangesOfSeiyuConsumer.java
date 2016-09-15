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
        Map<String, Map<String, Object>> oldMap = new LinkedHashMap<>();
        oldList.stream().forEach((map) -> oldMap.put((String) map.get("title"), map));
        List<Map<String, Object>> collect = newList.stream()
                .filter((map) -> map.containsKey("revid"))
                .filter((map) -> {
                    String title = (String) map.get("title");
                    if (oldMap.containsKey(title)) {
                        Map<String, Object> old = oldMap.get(title);
                        if (old.containsKey("revid")) {
                            String old_new_revid = (String) old.get("revid");
                            String new_old_revid = (String) map.get("old_revid");
                            if (old_new_revid.equals(new_old_revid)) {
                                return true;
                            } else {
                                String old_old_revid = (String) old.get("old_revid");
                                String new_new_revid = (String) map.get("revid");
                                if (old_old_revid.equals(new_old_revid) && old_new_revid.equals(new_new_revid)) {
                                    return false;
                                } else {
                                    throw new RuntimeException();
                                }
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
