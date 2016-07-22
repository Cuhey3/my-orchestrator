package com.heroku.myorchestrator.consumers.specific;

import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.consumers.ConsumerRouteBuilder;
import com.heroku.myorchestrator.util.MongoUtil;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class CombinedRequester extends ConsumerRouteBuilder {

    public CombinedRequester() {
        ironmqUtil.snapshot().kind(Kind.seiyu_category_members);
    }

    @Override
    public void configure() throws Exception {
        from("timer:initialize_seiyu_category_members?repeatCount=1")
                .filter((Exchange exchange) -> {
                    boolean flag;
                    try {
                        MongoUtil mongoUtil
                                = new MongoUtil(exchange,
                                        Kind.seiyu_category_members).master();
                        Optional<Document> findFirst = mongoUtil.findFirst();
                        flag = !findFirst.isPresent();
                    } catch (Exception e) {
                        flag = true;
                    }
                    if(flag){
                        System.out.println("initialize...");
                    }
                    return flag;
                })
                .setBody().constant("{\"kind\":\"seiyu_category_members\"}")
                .to(ironmqUtil.postUri());
    }
}
