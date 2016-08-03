package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_category_members;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_template_include_pages;
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
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            Document scm = util.getLatest(seiyu_category_members);
            Document stip = util.getLatest(seiyu_template_include_pages);
            return DocumentUtil.productSetByTitle(scm, stip);
        } catch (Exception ex) {
            IronmqUtil.sendError(this.getClass(), "doSnapshot", exchange, ex);
            return Optional.empty();
        }
    }

}
