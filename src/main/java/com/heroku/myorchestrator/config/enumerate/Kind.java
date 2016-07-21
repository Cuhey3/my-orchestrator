package com.heroku.myorchestrator.config.enumerate;

public enum Kind {
    foo, female_seiyu_category_members;

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
