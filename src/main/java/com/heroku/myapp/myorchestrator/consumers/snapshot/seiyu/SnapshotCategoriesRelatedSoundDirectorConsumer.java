package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.myorchestrator.consumers.common.SnapshotCategoryAggregationConsumer;
import com.heroku.myapp.commons.util.JsonUtil;
import com.heroku.myapp.commons.util.ResourceUtil;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SnapshotCategoriesRelatedSoundDirectorConsumer extends SnapshotCategoryAggregationConsumer {

    public SnapshotCategoriesRelatedSoundDirectorConsumer() {
        super();
        prefix = "Category:";
        resourcePath = "../../../../../sound_director_category_filter.json";
        jsonRoot = new JsonUtil(new ResourceUtil(resourcePath)
                .getJson(Map.class)).get("categories");
    }
}
