package com.heroku.myapp.myorchestrator;

import com.heroku.myapp.commons.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class Develop {

    public Processor dev() {
        return (Exchange exchange) -> {
            Map<String, Boolean> categories = new LinkedHashMap<>();
            Set<String> pages = new HashSet<>();
            //categories.put("Category:マスコット", Boolean.FALSE);
            //categories.put("Category:各年のコンピュータゲーム", Boolean.FALSE);
            //categories.put("Category:各年のアニメ", Boolean.FALSE);
            categories.put("Category:各年のテレビドラマ", Boolean.FALSE);
            //categories.put("Category:各年の日本のテレビ番組", Boolean.FALSE);
            //categories.put("Category:各年の映画", Boolean.FALSE);
            //categories.put("Category:各年の日本のラジオ番組", Boolean.FALSE);
            int i = 0;
            Pattern p = Pattern.compile("^Category:201\\d年の.+$");

            while (categories.values().stream().filter((b) -> !b).findFirst().isPresent()) {
                System.out.println(++i);
                categories.keySet().parallelStream()
                        .filter((key) -> !categories.get(key))
                        .flatMap((key) -> {
                            categories.put(key, Boolean.TRUE);
                            try {
                                return new MediawikiApiRequest()
                                        .setApiParam("action=query&list=categorymembers"
                                                + "&cmtitle="
                                                + URLEncoder.encode(key, "UTF-8")
                                                + "&cmlimit=500"
                                                + "&cmnamespace=0|14"
                                                + "&format=xml"
                                                + "&continue="
                                                + "&cmprop=title|ids|sortkeyprefix")
                                        .setListName("categorymembers").setMapName("cm")
                                        .setContinueElementName("cmcontinue")
                                        .getResultByMapList().stream();
                            } catch (Throwable t) {
                                System.out.println("failed!!!");
                                throw new RuntimeException();
                            }
                        }).reduce(categories, (foo, bar) -> {
                    String title = (String) bar.get("title");
                    String ns = (String) bar.get("ns");
                    if (ns.equals("0")) {
                        pages.add(title);
                    } else if (!categories.containsKey(title) && p.matcher(title).find()) {
                        categories.put(title, Boolean.FALSE);
                    }
                    return categories;
                }, (foo, bar) -> {
                    return foo;
                });
            }
            System.out.println(categories.keySet().size() + " " + pages.size());
            pages.stream().forEach(System.out::println);
            categories.keySet().stream().forEach(System.out::println);
        };
    }
}
