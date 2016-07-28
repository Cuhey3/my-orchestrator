package com.heroku.myorchestrator.consumers.deprecated.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;

//@Component
public class DiffSeiyuCategoryMembersConsumer extends DiffRouteBuilder {

    public DiffSeiyuCategoryMembersConsumer() {
        kind(Kind.seiyu_category_members);
    }
}
