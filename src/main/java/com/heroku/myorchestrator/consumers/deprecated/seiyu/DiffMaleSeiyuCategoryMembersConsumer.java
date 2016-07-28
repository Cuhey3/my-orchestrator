package com.heroku.myorchestrator.consumers.deprecated.seiyu;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.DiffRouteBuilder;

//@Component
public class DiffMaleSeiyuCategoryMembersConsumer extends DiffRouteBuilder {

    public DiffMaleSeiyuCategoryMembersConsumer() {
        kind(Kind.male_seiyu_category_members);
    }
}
