package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.consumers.SnapshotQueueConsumer;
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
            final int groupingSize = 50;
            List<Map<String, Object>> result
                    = groupedStream(getSoundDirectors(), groupingSize)
                    .flatMap((group) -> pageStream(group))
                    .collect(Collectors.groupingBy((page)
                            -> page.attr("title")))
                    .values().stream()
                    .flatMap((pages) -> mutualArrayStream(pages))
                    .collect(Collectors.groupingBy((mutualArray)
                            -> mutualArray[0]))
                    .values().stream()
                    .map((arrays) -> {
                        Map<String, Object> resultMap = new LinkedHashMap<>();
                        resultMap.put("title", arrays.get(0)[0]);
                        resultMap.put("director", arrays.stream()
                                .map((array) -> array[1])
                                .collect(Collectors.toSet()));
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
}
