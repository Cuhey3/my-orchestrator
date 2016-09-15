package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.DiffQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiffRecentchangesOfSeiyuConsumer extends DiffQueueConsumer {

    @Autowired
    CamelContext context;

    @Override
    public Optional<Document> calculateDiff(Document master, Document snapshot) {
        Map<String, List<String>> pagesMap = new LinkedHashMap<>();
        new DocumentUtil().setDocument(new MasterUtil(new DefaultExchange(context)).findOrElseThrow(Kind.pages_related_seiyu)).getData()
                .stream().forEach((map) -> {
                    pagesMap.put((String) map.get("title"), (List<String>) map.get("categories"));
                });
        Set<String> pagesSet = pagesMap.keySet();
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
                                map.put("type", "modify");
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
                            map.put("type", "modify");
                            return true;
                        }
                    } else {
                        map.put("type", "add");
                        return true;
                    }
                }).collect(Collectors.toList());
        List<Map<String, Object>> collect1 = collect.stream().map((map) -> {
            String revid = (String) map.get("revid");
            String old_revid = (String) map.get("old_revid");
            try {
                Set<String> newLinks = getFilteredLinks(revid, pagesSet);
                Set<String> oldLinks = getFilteredLinks(old_revid, pagesSet);
                List<Map<String, Object>> addList = newLinks.stream().filter((link) -> !oldLinks.contains(link))
                        .map((link) -> {
                            Map<String, Object> add = new LinkedHashMap<>();
                            add.put(link, pagesMap.get(link));
                            return add;
                        }).collect(Collectors.toList());
                if (!addList.isEmpty()) {
                    map.put("add", addList);
                }
                List<Map<String, Object>> removeList = oldLinks.stream().filter((link) -> !newLinks.contains(link))
                        .map((link) -> {
                            Map<String, Object> remove = new LinkedHashMap<>();
                            remove.put(link, pagesMap.get(link));
                            return remove;
                        }).collect(Collectors.toList());
                if (!removeList.isEmpty()) {
                    map.put("remove", addList);
                }
            } catch (IOException ex) {
                throw new RuntimeException();
            }
            return map;
        }).filter((map) -> map.containsKey("add") || map.containsKey("remove"))
                .collect(Collectors.toList());
        return new DocumentUtil().setDiff(collect1).nullable();
    }

    private Set<String> getFilteredLinks(String revid, Set<String> pagesSet) throws IOException {
        return Jsoup.connect("http://ja.wikipedia.org/w/api.php?action=parse&format=xml&prop=links&oldid=" + revid).ignoreContentType(true).timeout(Integer.MAX_VALUE).get().select("links pl[ns=0][exists]")
                .stream().map((element) -> element.text())
                .filter((text) -> pagesSet.contains(text))
                .collect(Collectors.toSet());
    }
}
