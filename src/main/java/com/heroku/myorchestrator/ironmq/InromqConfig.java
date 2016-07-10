package com.heroku.myorchestrator.ironmq;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.iron.ironmq.Client;
import io.iron.ironmq.Cloud;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InromqConfig {

    @Bean(name = "myclient")
    Client getIronmqClient() throws UnsupportedEncodingException, IOException {
        String projectId = System.getenv("IRON_MQ_PROJECT_ID");
        String token = System.getenv("IRON_MQ_TOKEN");
        if (projectId == null || token == null) {
            Gson gson = new Gson();
            InputStream resourceAsStream = ClassLoader.class.getResourceAsStream("/iron.json");
            JsonReader reader = new JsonReader(new InputStreamReader(resourceAsStream, "UTF-8"));
            Map<String, String> ironJson = gson.fromJson(reader, Map.class);
            System.out.println(ironJson);
            projectId = ironJson.get("project_id");
            token = ironJson.get("token");
        }
        Client client = new Client(projectId, token, Cloud.ironAWSUSEast);
        /*        Queue queue = client.queue("bar");
        QueueModel infoAboutQueue = queue.getInfoAboutQueue();
        System.out.println("bar size..." + infoAboutQueue.getSizeLong());
        Message reserve = queue.reserve();
        System.out.println(reserve.getBody());
        queue.deleteMessage(reserve);*/
        return client;
    }

}
