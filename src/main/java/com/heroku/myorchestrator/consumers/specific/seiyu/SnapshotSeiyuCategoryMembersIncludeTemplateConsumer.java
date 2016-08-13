package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.definitions.config.enumerate.Kind.seiyu_category_members;
import static com.heroku.definitions.config.enumerate.Kind.seiyu_template_include_pages;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.definitions.util.actions.MasterUtil;
import com.heroku.definitions.util.consumers.IronmqUtil;
import com.heroku.definitions.util.content.DocumentUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersIncludeTemplateConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil util = new MasterUtil(exchange);
            Document scm = util.findOrElseThrow(seiyu_category_members);
            Document stip = util.findOrElseThrow(seiyu_template_include_pages);
            return new DocumentUtil().productByTitle(scm, stip).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
