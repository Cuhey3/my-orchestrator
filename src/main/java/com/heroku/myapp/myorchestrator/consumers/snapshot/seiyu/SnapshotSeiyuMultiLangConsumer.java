package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.google.gson.Gson;
import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuMultiLangConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MapList data = new MasterUtil(exchange)
                .mapList(Kind.seiyu_has_recentchanges);
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < data.size(); i = i + 50) {
            urls.add(String.join("|", data.stream().map((map)
                    -> (String) map.get("pageid")).skip(i).limit(i + 50)
                    .collect(Collectors.toList())));
        }
        List<Map<String, Object>> result;
        result = urls.stream().parallel().flatMap((url) -> {
            try {
                String body = Jsoup.connect(
                        "https://ja.wikipedia.org/w/api.php"
                        + "?action=query&prop=langlinks"
                        + "&pageids=" + url + "&redirects="
                        + "&lllimit=500&format=json")
                        .ignoreContentType(true)
                        .timeout(Integer.MAX_VALUE).execute().body();
                Map<String, Object> fromJson
                        = new Gson().fromJson(body, Map.class);
                Map<String, Map<String, Object>> pages
                        = (Map<String, Map<String, Object>>) ((Map<String, Object>) fromJson.get("query")).get("pages");
                return pages.values().stream();
            } catch (IOException ex) {
                return new ArrayList<Map<String, Object>>().stream();
            }
        }).collect(Collectors.toList());
        if (result.size() == data.size()) {
            return new DocumentUtil(result).nullable();
        } else {
            util().sendLog("SnapshotSeiyuMultiLangConsumer#doSnapshot", "size not match result: " + result.size() + " orig: " + data.size());
            return Optional.empty();
        }
    }
}
