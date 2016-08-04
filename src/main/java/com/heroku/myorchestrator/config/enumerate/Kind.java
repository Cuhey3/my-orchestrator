package com.heroku.myorchestrator.config.enumerate;

import static com.heroku.myorchestrator.util.actions.DiffUtil.commonDiff;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public enum Kind {

    foo,
    female_seiyu_category_members(commonDiff(), "period=5m&delay=1m"),
    male_seiyu_category_members(commonDiff(), "period=5m&delay=2m"),
    seiyu_category_members(commonDiff()),
    seiyu_template_include_pages(commonDiff(), "period=5m&delay=3m"),
    seiyu_category_members_include_template(commonDiff()),
    koepota_events(commonDiff("url"), "period=30m&delay=10m"),
    koepota_seiyu(commonDiff()),
    seiyu_has_recentchanges(commonDiff()),
    koepota_seiyu_all(commonDiff()),
    amiami_item("period=60m&delay=30m"),
    amiami_original_titles(commonDiff("amiami_title")),
    amiami_original_titles_all(commonDiff("amiami_title")),
    google_trends_seiyu_all(commonDiff()),
    test;

    private Kind(String... token) {
        for (String t : token) {
            if (t.contains("common_diff_key=")) {
                this.useCommonDiff = true;
                this.commonDiffKey = t.replace("common_diff_key=", "");
            } else if (t.contains("period=")) {
                this.timerUri = String.format("timer:%s?%s", this.name(), t);
            }
        }
        InputStream resourceAsStream = ClassLoader.class
                .getResourceAsStream("/message/" + this.name() + ".json");
        try (BufferedReader buffer
                = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            preMessage = buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            System.out.println("premessage initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }

    private String timerUri, preMessage;
    private boolean useCommonDiff;
    private String commonDiffKey;

    public String expression() {
        return this.name();
    }

    public String timerUri() {
        return this.timerUri;
    }

    public String commonDiffKey() {
        return this.commonDiffKey;
    }

    public void timerParam(String timerParam) {
        this.timerUri = String.format("timer:%s?%s", expression(), timerParam);
    }

    public boolean useCommonDiffRoute() {
        return this.useCommonDiff;
    }

    public static Kind findKindByClassName(Object object) {
        try {
            String kindCamel = object.getClass().getSimpleName()
                    .replace("Snapshot", "").replace("Diff", "")
                    .replace("Consumer", "");
            String kindSnake
                    = String.join("_", kindCamel.split("(?=[\\p{Upper}])"))
                    .toLowerCase();
            return Kind.valueOf(kindSnake);
        } catch (Exception ex) {
            return null;
        }
    }

    public String preMessage() {
        return this.preMessage;
    }
}
