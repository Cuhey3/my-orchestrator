/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.heroku.myorchestrator.consumers.specific.amiami;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotAmiamiOriginalTitlesAllConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            util.kind(Kind.amiami_original_titles_all);
            List<Map<String, Object>> allList, aotList;
            try {
                allList = util.findLatest().get().get("data", List.class);
            } catch (Exception ex0) {
                allList = new ArrayList<>();
            }
            util.kind(Kind.amiami_original_titles);
            aotList = util.findLatest().get().get("data", List.class);
            Set<Object> allSet = allList.stream()
                    .map((map) -> map.get("amiami_title"))
                    .collect(Collectors.toSet());
            aotList.stream()
                    .filter((map) -> !allSet.contains(map.get("amiami_title")))
                    .forEach(allList::add);
            return Optional.ofNullable(document.append("data", allList));
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }
}
