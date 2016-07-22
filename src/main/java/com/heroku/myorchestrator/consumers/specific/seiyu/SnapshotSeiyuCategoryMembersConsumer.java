package com.heroku.myorchestrator.consumers.specific.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.SnapshotRouteBuilder;
import com.heroku.myorchestrator.util.MongoUtil;
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
        Optional<Document> femaleSeiyuOptional
                = new MongoUtil(exchange, Kind.female_seiyu_category_members).master().findLatest();
        Optional<Document> maleSeiyuOptional
                = new MongoUtil(exchange, Kind.male_seiyu_category_members).master().findLatest();
        if (femaleSeiyuOptional.isPresent() && maleSeiyuOptional.isPresent()) {
            List femaleSeiyuList = femaleSeiyuOptional.get().get("data", List.class);
            List maleSeiyuList = maleSeiyuOptional.get().get("data", List.class);
            femaleSeiyuList.addAll(maleSeiyuList);
            document.append("data", femaleSeiyuList);
        }
        return document;
    }
}
