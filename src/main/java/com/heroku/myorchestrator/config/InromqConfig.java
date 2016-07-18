package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.util.JsonResourceUtil;
import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import io.iron.ironmq.Client;
import io.iron.ironmq.Cloud;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InromqConfig {

    @Bean(name = "myclient")
    Client getIronmqClient() throws UnsupportedEncodingException, IOException {
        String projectId = System.getenv("IRON_MQ_PROJECT_ID");
        String token = System.getenv("IRON_MQ_TOKEN");
        if (projectId == null || token == null) {
            JsonResourceUtil jru = new JsonResourceUtil(Paths.IRON);
            projectId = jru.get("project_id");
            token = jru.get("token");
        }
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        return client;
    }
}
