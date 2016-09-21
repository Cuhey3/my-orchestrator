package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.DiffQueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
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
        // master:pages_related_seiyu to pagesMap (title, categories)
        Map<String, List<String>> pagesMap = new LinkedHashMap<>();
        new MasterUtil(new DefaultExchange(context))
                .mapList(Kind.pages_related_seiyu)
                .stream().forEach((map) -> {
                    pagesMap.put((String) map.get("title"),
                            (List<String>) map.get("categories"));
                });

        // pagesMap to pagesSet (keySet)
        Set<String> pagesSet = pagesMap.keySet();

        // master:recentchanges_of_seiyu to oldMap (title, self)
        Map<String, Map<String, Object>> oldMap = new LinkedHashMap<>();
        new MapList(master).stream().forEach((map)
                -> oldMap.put((String) map.get("title"), map));

        // calculateDiff main
        List result = new MapList(snapshot)
                .stream()
                .filter(targetFiltering(oldMap))
                .map(diffPutFunction(pagesMap, pagesSet))
                .filter(resultFiltering())
                .collect(Collectors.toList());
        return new DocumentUtil().setDiff(result).nullable();
    }

    private Set<String> getFilteredLinks(String revid, Set<String> pagesSet) throws IOException {
        String url = "http://ja.wikipedia.org/w/api.php"
                + "?action=parse"
                + "&format=xml"
                + "&prop=links"
                + "&oldid=" + revid;
        return Jsoup.connect(url).ignoreContentType(true)
                .timeout(Integer.MAX_VALUE).get()
                .select("links pl[ns=0][exists]")
                .stream().map((element) -> element.text())
                .filter((text) -> pagesSet.contains(text))
                .collect(Collectors.toSet());
    }

    private Predicate<Map<String, Object>> targetFiltering(
            Map<String, Map<String, Object>> oldMap) {
        return (map) -> {
            if (!map.containsKey("revid")) {
                return false;
            }
            String title = (String) map.get("title");
            if (oldMap.containsKey(title)) {
                Map<String, Object> old = oldMap.get(title);
                if (old.containsKey("revid")) {
                    String old_new_revid, new_old_revid;
                    old_new_revid = (String) old.get("revid");
                    new_old_revid = (String) map.get("old_revid");
                    if (old_new_revid.equals(new_old_revid)) {
                        map.put("type", "modify");
                        return true;
                    } else if (old.get("old_revid").equals(new_old_revid)
                            && old_new_revid.equals(map.get("revid"))) {
                        return false;
                    } else {
                        throw new RuntimeException();
                    }
                } else {
                    map.put("type", "modify");
                    return true;
                }
            } else {
                map.put("type", "add");
                return true;
            }
        };
    }

    private Function<Map<String, Object>, Map<String, Object>> diffPutFunction(
            Map<String, List<String>> pagesMap, Set<String> pagesSet) {
        return (map) -> {
            try {
                Set<String> newLinks = getFilteredLinks(
                        (String) map.get("revid"), pagesSet);
                Set<String> oldLinks = getFilteredLinks(
                        (String) map.get("old_revid"), pagesSet);
                List<Map<String, Object>> addList = newLinks.stream()
                        .filter((link) -> !oldLinks.contains(link))
                        .map((link) -> {
                            Map<String, Object> add = new LinkedHashMap<>();
                            add.put("title", link);
                            add.put("categories", pagesMap.get(link));
                            return add;
                        }).collect(Collectors.toList());
                if (!addList.isEmpty()) {
                    map.put("add", addList);
                }
                List<Map<String, Object>> removeList = oldLinks.stream()
                        .filter((link) -> !newLinks.contains(link))
                        .map((link) -> {
                            Map<String, Object> remove = new LinkedHashMap<>();
                            remove.put("title", link);
                            remove.put("categories", pagesMap.get(link));
                            return remove;
                        }).collect(Collectors.toList());
                if (!removeList.isEmpty()) {
                    map.put("remove", removeList);
                }
            } catch (IOException ex) {
                throw new RuntimeException();
            }
            return map;
        };
    }

    private Predicate<Map<String, Object>> resultFiltering() {
        return (map) -> map.containsKey("add") || map.containsKey("remove");
    }

    @Override
    public void toDoWhenDiffIsPresent(Document diff, Exchange exchange, Document master, Document snapshot) {
        DiffUtil diffUtil = new DiffUtil(exchange);
        diffUtil.updateMessageComparedId(master);
        diffUtil.writeDocuments(snapshot, diff);
        //.writeDocumentWhenDiffIsNotEmpty(diff);
    }
}
