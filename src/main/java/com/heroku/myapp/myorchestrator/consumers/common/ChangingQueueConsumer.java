package com.heroku.myapp.myorchestrator.consumers.common;

import com.heroku.myapp.commons.config.enumerate.Kind;
import com.heroku.myapp.commons.consumers.QueueConsumer;
import com.heroku.myapp.commons.util.actions.MasterUtil;
import com.heroku.myapp.commons.util.consumers.QueueConsumerUtil;
import com.heroku.myapp.commons.util.consumers.QueueMessage;
import static com.heroku.myapp.commons.util.consumers.QueueMessage.messageKindIs;
import org.apache.camel.model.ChoiceDefinition;
import org.springframework.stereotype.Component;

//@Component
public class ChangingQueueConsumer extends QueueConsumer {

    public ChangingQueueConsumer() {
        util().changing();
    }

    @Override
    public void configure() {
        ChoiceDefinition choice
                = from(util().copy().changed().ironmqConsumeUri())
                .routeId(util().id())
                .choice();
        for (Kind k : Kind.values()) {
            choice.when(messageKindIs(k)).to("log:" + k.expression());
        }
        choice.otherwise().to("log:none")
                .end()
                .choice()
                .when(MasterUtil.isNotFilled(this))
                .process(util().requestSnapshotProcess())
                .otherwise()
                .filter(QueueMessage.loadAffectPredicate())
                .split().body()
                .routingSlip(util().affectQueueUri());
    }
}
