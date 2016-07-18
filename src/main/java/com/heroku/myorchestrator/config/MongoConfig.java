package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.util.JsonResourceUtil;
import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    String mongodbUri;

    public MongoConfig() {
    }

    @Bean(name = "own")
    MongoClient mongoClient() throws UnknownHostException, UnsupportedEncodingException {
        String settingKey = "MONGODB_URI";
        String mongoUri = System.getenv(settingKey);
        if (mongoUri == null) {
            JsonResourceUtil jru = new JsonResourceUtil(Paths.SETTINGS);
            mongoUri = jru.get(settingKey);
        }
        return new MongoClient(new MongoClientURI(mongoUri));
    }
}
