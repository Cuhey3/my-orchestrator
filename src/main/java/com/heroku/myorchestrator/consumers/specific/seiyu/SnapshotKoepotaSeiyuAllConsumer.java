package com.heroku.myorchestrator.consumers.specific.seiyu;

import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu;
import static com.heroku.myorchestrator.config.enumerate.Kind.koepota_seiyu_all;
import static com.heroku.myorchestrator.config.enumerate.Kind.seiyu_category_members_include_template;
import com.heroku.myorchestrator.consumers.SnapshotQueueConsumer;
import com.heroku.myorchestrator.util.actions.MasterUtil;
import com.heroku.myorchestrator.util.consumers.IronmqUtil;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import static com.heroku.myorchestrator.util.content.DocumentUtil.getData;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class SnapshotKoepotaSeiyuAllConsumer extends SnapshotQueueConsumer {

    @Override
    protected Optional<Document> doSnapshot(Exchange exchange) {
        try {
            MasterUtil masterUtil = new MasterUtil(exchange);
            DocumentUtil util = new DocumentUtil();
            List<Map<String, Object>> allList = util.addNewByKey(
                    masterUtil.getLatest(koepota_seiyu_all),
                    masterUtil.getLatest(koepota_seiyu),
                    "title").getData();
            Set scmitSet = getData(masterUtil.getLatest(
                    seiyu_category_members_include_template))
                    .stream().map((map) -> map.get(("title")))
                    .collect(Collectors.toSet());
            allList.stream().forEach((map) -> {
                if (scmitSet.contains(map.get("title"))) {
                    map.remove("inactive");
                } else {
                    map.put("inactive", true);
                }
            });
            return util.setData(allList).nullable();
        } catch (Exception ex) {
            IronmqUtil.sendError(this, "doSnapshot", ex);
            return Optional.empty();
        }
    }
}
