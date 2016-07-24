package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    public SnapshotSeiyuCategoryMembersConsumer() {
        kind(Kind.seiyu_category_members);
    }

    @Override
    protected Document doSnapshot(Exchange exchange, Document document) throws Exception {
        Optional<Document> optFemaleSeiyu, optMaleSeiyu;
        MasterUtil masterUtil = new MasterUtil(exchange);
        optFemaleSeiyu = masterUtil
                .kind(Kind.female_seiyu_category_members).findLatest();
        optMaleSeiyu = masterUtil
                .kind(Kind.male_seiyu_category_members).findLatest();
        if (optFemaleSeiyu.isPresent() && optMaleSeiyu.isPresent()) {
            List result = new ArrayList<>();
            result.addAll(optFemaleSeiyu.get().get("data", List.class));
            result.addAll(optMaleSeiyu.get().get("data", List.class));
            document.append("data", result);
        }
        return document;
    }
}
