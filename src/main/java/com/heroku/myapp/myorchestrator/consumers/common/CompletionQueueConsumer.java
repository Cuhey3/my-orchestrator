package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.config.enumerate.KindOptions;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.actions.DiffUtil;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.consumers.QueueConsumerUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import static com.heroku.myapp.commons.util.consumers.QueueMessage.messageKindIs;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.model.ChoiceDefinition;
import org.springframework.stereotype.Component;

@Component
public class CompletionQueueConsumer extends QueueConsumer {
    
    @Override
    public void configure() {
        ChoiceDefinition topLevelChoice = from(util().completion().ironmqConsumeUri())
                .routeId(util().id()).choice();
        setAlwaysAffects(topLevelChoice);
        ChoiceDefinition secondLevelChoice
                = topLevelChoice.otherwise().filter((Exchange exchange) -> {
                    return new MasterUtil(exchange).toCompleteLogic(this);
                })
                .id("filter:to_complete")
                .filter((Exchange exchange)
                        -> new MasterUtil(exchange).snapshotSaveToMaster(this))
                .id("filter:snapshot_save_to_master")
                .filter((Exchange exchange)
                        -> new DiffUtil(exchange).enableDiff(this))
                .id("filter:enable_diff")
                .choice();
        ChoiceDefinition secondLevelChoiceOtherwise
                = secondLevelChoice.when(MasterUtil.isNotFilled(this))
                .id("when:isNotFilled")
                .process(util().requestSnapshotProcess())
                .otherwise().choice();
        
        for (Kind k : Kind.values()) {
            if (k.optionIsEnable()) {
                secondLevelChoiceOtherwise.when(messageKindIs(k)).to("log:" + k.expression());
                if (k.isEnable(KindOptions.affect)) {
                    k.affects().stream().forEach((affect) -> {
                        secondLevelChoiceOtherwise.setBody()
                                .constant(affect.preMessage())
                                .to(new QueueConsumerUtil(affect).snapshot()
                                        .ironmqPostUri());
                    });
                }
            }
        }
    }
    
    public void setAlwaysAffects(ChoiceDefinition choice) {
        ChoiceDefinition nestedChoice = choice.when((Exchange exchange)
                -> !Optional.ofNullable(
                        (Boolean) new QueueMessage(exchange)
                        .map().get("changed")).orElse(false))
                .id("when:changedIsFalse")
                .choice();
        for (Kind k : Kind.values()) {
            if (k.optionIsEnable()) {
                if (k.isEnable(KindOptions.always_affect)) {
                    nestedChoice.when(messageKindIs(k))
                            .id("when:is_" + k.expression())
                            .to("log:" + k.expression());
                    k.alwaysAffects().stream().forEach((affect) -> {
                        nestedChoice.setBody().constant(affect.preMessage())
                                .to(new QueueConsumerUtil(affect).snapshot()
                                        .ironmqPostUri());
                    });
                }
            }
        }
        choice.endChoice();
    }
}
