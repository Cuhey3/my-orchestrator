package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.util.content.MapList;
import com.heroku.myapp.commons.consumers.SnapshotCategoryPagesAggregationConsumer;
import org.springframework.stereotype.Component;

@Component
public class SnapshotMaleSeiyuCategoryMembersConsumer extends SnapshotCategoryPagesAggregationConsumer {

    public SnapshotMaleSeiyuCategoryMembersConsumer() {
        super();
        addTargetCategory("Category:日本の男性声優");
        cmpropParam("title|ids|sortkeyprefix");
        includesCategoryFlag(false);
    }

    @Override
    protected void afterProcess(MapList mapList) {
        mapList.forEach((m) -> m.put("gender", "m"));
    }
}
