package com.heroku.myorchestrator.consumers.deprecated.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;

//@Component
public class DiffFemaleSeiyuCategoryMembersConsumer extends DiffRouteBuilder {

    public DiffFemaleSeiyuCategoryMembersConsumer() {
        kind(Kind.female_seiyu_category_members);
    }
}
