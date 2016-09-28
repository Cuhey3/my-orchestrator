package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.MapList;
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

public abstract class SnapshotCategoryPagesAggregationConsumer extends SnapshotQueueConsumer {

    protected Kind targetKind;
    protected boolean includesCategoryFlag = true;

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        MapList categories = new MasterUtil(exchange).mapList(targetKind);
        LinkedHashMap<String, List<String>> reduce
                = categories.attrStream("title", String.class)
                .map((category) -> {
                    try {
                        return new Object[]{category, new MediawikiApiRequest()
                            .setApiParam("action=query&list=categorymembers"
                            + "&cmtitle="
                            + URLEncoder.encode(category, "UTF-8")
                            + "&cmlimit=500"
                            + "&cmnamespace=0"
                            + "&format=xml"
                            + "&cmprop=title")
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
                }).reduce(new LinkedHashMap<String, List<String>>(),
                (result, objArray) -> {
                    String category = (String) objArray[0];
                    MapList list = new MapList((List) objArray[1]);
                    list.stream()
                    .forEach((map) -> {
                        String title = (String) map.get("title");
                        List<String> categoryList = result.getOrDefault(
                                title, new ArrayList<>());
                        categoryList.add(
                                category.replaceFirst("^Category:", ""));
                        result.put(title, categoryList);
                    });
                    return result;
                }, (foo, bar) -> foo);

        List collect = reduce.entrySet().stream().map((entry) -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", entry.getKey());
            List<String> value = entry.getValue();
            Collections.sort(value);
            if (includesCategoryFlag) {
                map.put("categories", value);
            }
            return map;
        }).collect(Collectors.toList());
        return new DocumentUtil(collect).nullable();
    }
}
