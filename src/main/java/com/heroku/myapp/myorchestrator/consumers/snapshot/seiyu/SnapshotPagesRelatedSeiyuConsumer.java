package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import static com.heroku.myapp.commons.config.enumerate.Kind.categories_related_seiyu;
import com.heroku.myapp.commons.consumers.SnapshotCategoryPagesAggregationConsumer;
import org.springframework.stereotype.Component;

@Component
public class SnapshotPagesRelatedSeiyuConsumer extends SnapshotCategoryPagesAggregationConsumer {

    public SnapshotPagesRelatedSeiyuConsumer() {
        super();
        targetKind = categories_related_seiyu;
    }
}
