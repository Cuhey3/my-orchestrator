package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersIncludeTemplateConsumer extends SnapshotRouteBuilder {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange, Document document) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            Document scm = masterUtil.kind(Kind.seiyu_category_members)
                    .findLatest().get();
            Document stip = masterUtil.kind(Kind.seiyu_template_include_pages)
                    .findLatest().get();
            return DocumentUtil.productSetByTitle(scm, stip);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

}
