package com.heroku.myorchestrator.config;

import io.iron.ironmq.Client;
import io.iron.ironmq.Cloud;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InromqConfig {

    @Bean(name = "myironmq")
    Client getIronmqClient() throws Exception {
        try {
            String projectId, token;
            projectId = Settings.IRON.get("IRON_MQ_PROJECT_ID", "project_id");
            token = Settings.IRON.get("IRON_MQ_TOKEN", "token");
            return new Client(projectId, token, Cloud.ironAWSUSEast);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ironmq client initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
            return null;
        }
    }
}
