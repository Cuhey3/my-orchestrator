package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.content.DocumentUtil;
import com.heroku.myapp.commons.util.content.IterableMediawikiApiRequest;
import com.heroku.myapp.commons.util.content.MediawikiApiRequestBuilder;
import com.heroku.myapp.commons.util.content.MediawikiApiRequestBuilder.Namespace;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SnapshotPagesMutualSoundDirectorConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            Set<String> seiyuNames = util.mapList(Kind.seiyu_category_members)
                    .attrSet("title");
            Set<String> relatedPages = util.mapList(Kind.pages_related_seiyu).attrSet("title");
            final int groupingSize = 10;
            List<Map<String, Object>> result
                    = groupedStream(getSoundDirectors(), groupingSize)
                    .parallel()
                    .flatMap((group) -> pageStream(group))
                    .collect(Collectors.groupingBy((page)
                            -> page.attr("title")))
                    .values().stream()
                    .flatMap((pages) -> mutualArrayStream(pages))
                    .collect(Collectors.groupingBy((mutualArray)
                            -> mutualArray[0]))
                    .values().stream()
                    .filter((arrays) -> {
                        String title = arrays.get(0)[0];

                        return relatedPages.contains(title) && !seiyuNames.contains(title);
                    })
                    .map((arrays) -> {
                        Map<String, Object> resultMap = new LinkedHashMap<>();
                        Set<String> director = arrays.stream().map((array) -> array[1]).collect(Collectors.toSet());
                        resultMap.put("title", arrays.get(0)[0]);
                        String group = getGroup(director, seiyuNames);
                        resultMap.put("group", group);
                        if (group.startsWith("filtered_")) {
                            resultMap.put("director", filtered(director, seiyuNames));
                        } else {
                            resultMap.put("director", director);
                        }
                        return resultMap;
                    })
                    .collect(Collectors.toList());
            return new DocumentUtil(result).nullable();
        } catch (Exception ex) {
            util().sendError("doSnapshot", ex);
            return Optional.empty();
        }
    }

    public Stream<List<String>> groupedStream(List<String> titles, int groupingSize) {
        return IntStream.range(0, titles.size())
                .mapToObj((i) -> {
                    Map<Integer, String> map = new LinkedHashMap<>();
                    map.put(i, titles.get(i));
                    return map;
                })
                .collect(Collectors.groupingBy((map) -> map.keySet()
                        .iterator().next() / groupingSize))
                .values().stream().map((values) -> values.stream().map((m)
                        -> m.values().iterator().next())
                        .collect(Collectors.toList()));
    }

    public List<String> getSoundDirectors() throws IOException {
        return new MediawikiApiRequestBuilder()
                .action(MediawikiApiRequestBuilder.Action.QUERY)
                .list(MediawikiApiRequestBuilder.List.CATEGORYMEMBERS)
                .namespaces(new Namespace[]{Namespace.ARTICLE})
                .title("Category:日本の音響監督")
                .build().getResultByMapList()
                .stream().map((map) -> (String) map.get("title"))
                .collect(Collectors.toList());
    }

    public Stream<Element> pageStream(List<String> group) {
        final String[] names = {"plcontinue", "lhcontinue"};
        Iterator<Elements> iterator = new IterableMediawikiApiRequest()
                .setApiParam("action=query&titles=" + String.join("|", group)
                        + "&format=xml&redirects=true&prop=links|linkshere"
                        + "&plnamespace=0&lhnamespace=0"
                        + "&pllimit=500&lhlimit=500")
                .setContinueElementNames(names).debug().iterator();
        Spliterator<Elements> spliterator = Spliterators.spliteratorUnknownSize(
                iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false)
                .flatMap((pages) -> pages.stream());
    }

    public Stream<String[]> mutualArrayStream(List<Element> pages) {
        Set<String> plset, mutual;
        String soundDirectorName = pages.get(0).attr("title");
        plset = pages.stream().flatMap((page) -> page.select("pl").stream())
                .map((m) -> m.attr("title")).collect(Collectors.toSet());
        mutual = pages.stream().flatMap((page) -> page.select("lh").stream())
                .map((m) -> m.attr("title"))
                .filter((str) -> plset.contains(str))
                .collect(Collectors.toSet());
        return mutual.stream().map((title) -> {
            return new String[]{title, soundDirectorName};
        });
    }

    public String getGroup(Set<String> director, Set<String> seiyuNames) {
        int directorSize, filteredSize;
        directorSize = director.size();
        if (directorSize == 1) {
            if (seiyuNames.contains(director.iterator().next())) {
                return "s_one";
            } else {
                return "d_one";
            }
        } else {
            List<String> filtered = filtered(director, seiyuNames);
            filteredSize = filtered.size();
            if (filteredSize == 0) {
                return "s_many";
            } else if (filteredSize == 1) {
                return "filtered_d_one";
            } else if (filteredSize == directorSize) {
                return "d_many";
            } else {
                return "filtered_d_many";
            }
        }
    }

    public List<String> filtered(Set<String> director, Set<String> seiyuNames) {
        return director.stream()
                .filter((name) -> !seiyuNames.contains(name))
                .collect(Collectors.toList());
    }
}
