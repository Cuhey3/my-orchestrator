package com.heroku.myorchestrator.util.content;

import com.heroku.myorchestrator.util.MessageUtil;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;

public final class DocumentUtil {

    public static Document restorePrefix(Document document) {
        Map<String, String> prefixs = document.get("prefix", Map.class);
        if (prefixs != null) {
            prefixs.entrySet().stream().forEach((entry) -> {
                restorePrefixSpecific(document, entry.getKey(), entry.getValue());
            });
        }
        return document;
    }

    private static void restorePrefixSpecific(Document document, String key, String prefix) {
        DocumentUtil util = new DocumentUtil().setDocument(document);
        List<Map<String, Object>> list = util.getData();
        list.stream().forEach((map) -> {
            map.put(key, prefix + map.get(key));
        });
        util.setData(list);
    }

    public static boolean checkNotFilled(Exchange exchange, Document document) throws Exception {
        MessageUtil messageUtil = new MessageUtil(exchange);
        String fillField = messageUtil.get("fill");
        if (fillField == null) {
            return false;
        } else {
            if (document == null) {
                document = new MasterUtil(exchange).findLatest().get();
            }
            List<Map<String, Object>> list = DocumentUtil.getData(document);
            return list.stream().anyMatch((map) -> !map.containsKey(fillField));
        }
    }

    public static List<Map<String, Object>> getData(Document document) {
        return document.get("data", List.class);
    }

    private Document document;

    public DocumentUtil() {
        this.document = new Document();
    }

    public DocumentUtil(List list) {
        this.document = new Document();
        setData(list);
    }

    public DocumentUtil productByKey(Document sieved, Document filter, final String key) {
        Set<Object> filterSet = getData(filter).stream()
                .map((map) -> map.get(key))
                .collect(Collectors.toSet());
        List<Map<String, Object>> collect = getData(sieved).stream()
                .filter((map) -> filterSet.contains(map.get(key)))
                .collect(Collectors.toList());
        setData(collect);
        return this;
    }

    public DocumentUtil productByTitle(Document sieved, Document filter) {
        return productByKey(sieved, filter, "title");
    }

    public DocumentUtil addNewByKey(Document oldDoc, Document newDoc, final String key) {
        List<Map<String, Object>> oldList;
        if (oldDoc == null || getData(oldDoc) == null) {
            oldList = new ArrayList<>();
        } else {
            oldList = getData(oldDoc);
        }
        Set oldSet = oldList.stream().map((map) -> map.get(key))
                .collect(Collectors.toSet());
        getData(newDoc).stream().filter((map) -> !oldSet.contains(map.get(key)))
                .forEach(oldList::add);
        setData(oldList);
        return this;
    }

    public DocumentUtil createPrefix(String... keys) {
        document.append("prefix", new LinkedHashMap());
        for (String key : keys) {
            createPrefixSpecific(key);
        }
        return this;
    }

    private void createPrefixSpecific(String key) {
        List<Map<String, Object>> list = getData(document);
        String firstValue = (String) list.get(0).get(key);
        int len = firstValue.length();
        String prefix = null;
        for (int i = len; i > 10; i--) {
            String prefixSuggest = firstValue.substring(0, i);
            if (list.stream().allMatch((map)
                    -> ((String) map.get(key)).startsWith(prefixSuggest))) {
                prefix = prefixSuggest;
                int length = prefix.length();
                list.stream().forEach((map) -> {
                    map.put(key, ((String) map.get(key))
                            .substring(length));
                });
                break;
            }
        }
        if (prefix != null) {
            Map prefixs = document.get("prefix", Map.class);
            prefixs.put(key, prefix);
            document.put("prefix", prefixs);
        }
    }

    public List<Map<String, Object>> getData() {
        return document.get("data", List.class);
    }

    public DocumentUtil setData(List list) {
        document.append("data", list);
        return this;
    }

    public DocumentUtil setDiff(List list) {
        document.append("diff", list);
        return this;
    }

    public Document getDocument() {
        return this.document;
    }

    public DocumentUtil setDocument(Document document) {
        this.document = document;
        return this;
    }

    public Optional<Document> nullable() {
        return Optional.ofNullable(document);
    }
}
