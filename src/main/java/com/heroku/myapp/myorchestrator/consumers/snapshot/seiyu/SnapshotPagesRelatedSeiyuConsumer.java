package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotPagesRelatedSeiyuConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        List<Map<String, Object>> categories
                = new DocumentUtil().setDocument(
                        new MasterUtil(exchange).findOrElseThrow(
                                Kind.categories_related_seiyu)).getData();

        LinkedHashMap<String, List<String>> reduce
                = categories.parallelStream().map((map) -> (String) map.get("title"))
                .map((category) -> {
                    try {
                        return new Object[]{category, new MediawikiApiRequest()
                            .setApiParam("action=query&list=categorymembers"
                            + "&cmtitle="
                            + URLEncoder.encode(category, "UTF-8")
                            + "&cmlimit=500"
                            + "&cmnamespace=0"
                            + "&format=xml"
                            + "&continue="
                            + "&cmprop=title|ids|sortkeyprefix")
                            .setListName("categorymembers").setMapName("cm")
                            .setContinueElementName("cmcontinue")
                            .setIgnoreFields("ns").getResultByMapList()};
                    } catch (UnsupportedEncodingException ex) {
                        util().sendError("SnapshotPagesRelatedSeiyuConsumer", ex);
                        throw new RuntimeException();
                    } catch (IOException ex) {
                        util().sendError("SnapshotPagesRelatedSeiyuConsumer", ex);
                        throw new RuntimeException();
                    }
                }).reduce(new LinkedHashMap<String, List<String>>(), (result, objArray) -> {
            String category = (String) objArray[0];
            List<Map<String, Object>> list = (List<Map<String, Object>>) objArray[1];
            list.stream()
                    .forEach((map) -> {
                        String title = (String) map.get("title");
                        List<String> categoryList = result.getOrDefault(title, new ArrayList<>());
                        categoryList.add(category.replaceFirst("^Category:", ""));
                        result.put(title, categoryList);
                    });
            return result;
        }, (foo, bar) -> foo);

        List<Map<String, Object>> collect = reduce.entrySet().stream().map((entry) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", entry.getKey());
            List<String> value = entry.getValue();
            Collections.sort(value);
            map.put("categories", value);
            return map;
        }).collect(Collectors.toList());
        return new DocumentUtil().setData(collect).nullable();
    }
}
