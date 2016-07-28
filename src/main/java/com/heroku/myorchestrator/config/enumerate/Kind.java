package com.heroku.myorchestrator.config.enumerate;

public enum Kind {

    foo,
    female_seiyu_category_members,
    male_seiyu_category_members,
    seiyu_category_members,
    seiyu_template_include_pages,
    seiyu_category_members_include_template,
    koepota_events,
    koepota_seiyu,
    test;

    private String timerUri;

    public String expression() {
        return this.name();
    }

    public String timerUri() {
        return this.timerUri;
    }

    public void timerParam(String timerParam) {
        this.timerUri
                = String.format("timer:%s?%s", this.expression(), timerParam);
    }
}
