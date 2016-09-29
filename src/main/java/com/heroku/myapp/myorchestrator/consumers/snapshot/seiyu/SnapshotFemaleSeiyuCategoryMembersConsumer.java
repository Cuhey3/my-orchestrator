package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.consumers.SnapshotCategoryPagesAggregationConsumer;
import org.springframework.stereotype.Component;

@Component
public class SnapshotFemaleSeiyuCategoryMembersConsumer extends SnapshotCategoryPagesAggregationConsumer {

    public SnapshotFemaleSeiyuCategoryMembersConsumer() {
        super();
        addTargetCategory("Category:日本の女性声優");
        cmpropParam("title|ids|sortkeyprefix");
        includesCategoryFlag(false);
    }

    @Override
    protected void afterProcess(MapList mapList) {
        mapList.forEach((m) -> m.put("gender", "f"));
    }
}
