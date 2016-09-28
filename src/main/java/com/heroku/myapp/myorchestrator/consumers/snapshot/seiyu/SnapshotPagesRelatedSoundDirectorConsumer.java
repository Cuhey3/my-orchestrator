package com.heroku.myapp.myorchestrator.consumers.snapshot.seiyu;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.myorchestrator.consumers.common.SnapshotCategoryPagesAggregationConsumer;
import org.springframework.stereotype.Component;

@Component
public class SnapshotPagesRelatedSoundDirectorConsumer extends SnapshotCategoryPagesAggregationConsumer {

    public SnapshotPagesRelatedSoundDirectorConsumer() {
        super();
        targetKind = Kind.categories_related_sound_director;
    }
}
