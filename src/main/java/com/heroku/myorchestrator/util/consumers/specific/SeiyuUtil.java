package com.heroku.myorchestrator.util.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.MediawikiApiRequest;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;

public class SeiyuUtil {

    private final Exchange exchange;

    public SeiyuUtil(Exchange exchange) {
        this.exchange = exchange;
    }

    public Optional<Document> defaultSnapshot(SeiyuKind seiyuKind, Document document) {
        try {
            List<Map<String, Object>> mapList
                    = new MediawikiApiRequest()
                    .setApiParam("action=query&list=categorymembers"
                            + "&cmtitle=Category:"
                            + URLEncoder.encode(seiyuKind.expression(), "UTF-8")
                            + "&cmlimit=500"
                            + "&cmnamespace=0"
                            + "&format=xml"
                            + "&continue="
                            + "&cmprop=title|ids|sortkeyprefix")
                    .setListName("categorymembers").setMapName("cm")
                    .setContinueElementName("cmcontinue")
                    .setIgnoreFields("ns")
                    .getResultByMapList();
            mapList.forEach((m) -> m.put("gender", seiyuKind.sign()));
            document.append("data", mapList);
            return Optional.ofNullable(document);
        } catch (Exception e) {
            IronmqUtil.sendError(this.getClass(), "defaultSnapshot", exchange, e);
            return Optional.empty();
        }
    }

    public enum SeiyuKind {
        female("日本の女性声優", Kind.female_seiyu_category_members),
        male("日本の男性声優", Kind.male_seiyu_category_members),
        seiyu("none", Kind.seiyu_category_members),
        seiyu_template_include_pages("none", Kind.seiyu_template_include_pages),
        seiyu_category_members_include_template(
                "none", Kind.seiyu_category_members_include_template),
        koepota_seiyu("none", Kind.koepota_seiyu),
        seiyu_has_recentchanges("none", Kind.seiyu_has_recentchanges);

        private final String expression, sign;

        private final Kind kind;

        private SeiyuKind(String expression, Kind kind) {
            this.expression = expression;
            this.sign = String.valueOf(this.name().charAt(0));
            this.kind = kind;
        }

        public String expression() {
            return this.expression;
        }

        public String sign() {
            return this.sign;
        }

        public Kind kind() {
            return this.kind;
        }
    }
}
