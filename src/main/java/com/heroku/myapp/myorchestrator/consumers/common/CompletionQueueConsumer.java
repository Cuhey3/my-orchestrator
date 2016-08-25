package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.KindOptions;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.consumers.QueueConsumerUtil;
import static com.heroku.myapp.commons.util.consumers.QueueMessage.messageKindIs;
import org.apache.camel.Exchange;
import org.apache.camel.model.ChoiceDefinition;
import org.springframework.stereotype.Component;

@Component
public class CompletionQueueConsumer extends QueueConsumer {

    @Override
    public void configure() {
        ChoiceDefinition choice = from(util().completion().ironmqConsumeUri())
                .routeId(util().id())
                .filter((Exchange exchange) -> {
                    return new MasterUtil(exchange).toCompleteLogic(this);
                })
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster(this))
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff(this))
                .choice()
                .when(MasterUtil.isNotFilled(this))
                .process(util().requestSnapshotProcess())
                .otherwise();

        for (Kind k : Kind.values()) {
            if (k.optionIsEnable()) {
                choice.when(messageKindIs(k)).to("log:" + k.expression());
                if (k.isEnable(KindOptions.affect)) {
                    k.affects().stream().forEach((affect) -> {
                        choice.setBody().constant(affect.preMessage())
                                .to(new QueueConsumerUtil(affect).snapshot()
                                        .ironmqPostUri());
                    });
                }
            }
        }
    }
}
