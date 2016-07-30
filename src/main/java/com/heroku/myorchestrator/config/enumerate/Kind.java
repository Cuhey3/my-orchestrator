package com.heroku.myorchestrator.config.enumerate;

public enum Kind {

    foo,
    female_seiyu_category_members("common_diff", "period=5m&delay=1m"),
    male_seiyu_category_members("common_diff", "period=5m&delay=2m"),
    seiyu_category_members("common_diff"),
    seiyu_template_include_pages("common_diff", "period=5m&delay=3m"),
    seiyu_category_members_include_template("common_diff"),
    koepota_events("period=30m&delay=10m"),
    koepota_seiyu("common_diff"),
    seiyu_has_recentchanges("common_diff"),
    koepota_seiyu_all("common_diff"),
    amiami_item("period=60m&delay=0m"),
    test;

    private Kind(String... token) {
        for (String t : token) {
            if (t.equals("common_diff")) {
                this.useCommonDiff = true;
            } else if (t.contains("period") && t.contains("delay")) {
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
