package com.heroku.myorchestrator.config.enumerate;

public enum Kind {

    foo,
    female_seiyu_category_members("common_diff"),
    male_seiyu_category_members("common_diff"),
    seiyu_category_members("common_diff"),
    seiyu_template_include_pages("common_diff"),
    seiyu_category_members_include_template("common_diff"),
    koepota_events,
    koepota_seiyu("common_diff"),
    seiyu_has_recentchanges("common_diff"),
    test;

    private Kind(String... token) {
        for (String t : token) {
            if (t.equals("common_diff")) {
                this.useCommonDiff = true;
            }
        }
    }

    private Kind() {
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
