package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersIncludeTemplateConsumer extends SnapshotRouteBuilder {

    public SnapshotSeiyuCategoryMembersIncludeTemplateConsumer() {
        kind(Kind.seiyu_category_members_include_template);
    }

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            Document scm = masterUtil.kind(Kind.seiyu_category_members).findLatest().get();
            Document stip = masterUtil.kind(Kind.seiyu_template_include_pages).findLatest().get();
            List<Map<String, Object>> stipList = stip.get("data", List.class);
            Set stipSet = stipList.stream().map((map) -> map.get("title")).collect(Collectors.toSet());
            List<Map<String, Object>> scmList = scm.get("data", List.class);
            List<Map<String, Object>> collect = scmList.stream().filter((map) -> stipSet.contains(map.get("title")))
                    .collect(Collectors.toList());
            document.append("data", collect);
            return Optional.ofNullable(document);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

}
