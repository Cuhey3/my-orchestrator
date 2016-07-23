package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotSeiyuCategoryMembersConsumer extends SnapshotRouteBuilder {

    public SnapshotSeiyuCategoryMembersConsumer() {
        setKind(Kind.seiyu_category_members);
    }

    @Override
    protected Document doSnapshot(Exchange exchange, Document document) throws Exception {
        Optional<Document> optFemaleSeiyu, optMaleSeiyu;
        optFemaleSeiyu
                = new MongoUtil(exchange, Kind.female_seiyu_category_members)
                .master().findLatest();
        optMaleSeiyu
                = new MongoUtil(exchange, Kind.male_seiyu_category_members)
                .master().findLatest();
        if (optFemaleSeiyu.isPresent() && optMaleSeiyu.isPresent()) {
            List result = new ArrayList<>();
            result.addAll(optFemaleSeiyu.get().get("data", List.class));
            result.addAll(optMaleSeiyu.get().get("data", List.class));
            document.append("data", result);
        }
        return document;
    }
}
