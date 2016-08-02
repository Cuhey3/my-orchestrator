package com.heroku.myorchestrator.config.enumerate;

import static com.heroku.myorchestrator.util.actions.DiffUtil.commonDiff;

public enum Kind {

    foo,
    female_seiyu_category_members(commonDiff(), "period=5m&delay=1m"),
    male_seiyu_category_members(commonDiff(), "period=5m&delay=2m"),
    seiyu_category_members(commonDiff()),
    seiyu_template_include_pages(commonDiff(), "period=5m&delay=3m"),
    seiyu_category_members_include_template(commonDiff()),
    koepota_events("period=30m&delay=10m"),
    koepota_seiyu(commonDiff()),
    seiyu_has_recentchanges(commonDiff()),
    koepota_seiyu_all(commonDiff()),
    amiami_item("period=60m&delay=30m"),
    amiami_original_titles,
    amiami_original_titles_all,
    test;

    private Kind(String... token) {
        for (String t : token) {
            if (t.equals(commonDiff())) {
                this.useCommonDiff = true;
            } else if (t.contains("period=")) {
                this.timerUri = String.format("timer:%s?%s", this.name(), t);
            }
        }
    }

    private String timerUri;
    private boolean useCommonDiff;

    public String expression() {
        return this.name();
    }

    public String timerUri() {
        return this.timerUri;
    }

    public void timerParam(String timerParam) {
        this.timerUri = String.format("timer:%s?%s", expression(), timerParam);
    }

    public boolean useCommonDiffRoute() {
        return this.useCommonDiff;
    }
}
