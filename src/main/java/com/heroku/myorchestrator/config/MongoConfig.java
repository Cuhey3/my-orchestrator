package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static Map mongoSettings;

    public static MongoClientURI getMongoClientURI(MongoTarget target) {
        return new MongoClientURI(
                (String) mongoSettings.get(target.expression()));
    }

    private String ownMongodbUri;

    public MongoConfig() throws Exception {
        try {
            ownMongodbUri = Settings.ENV.get("MONGODB_URI");
            MongoClientURI mongoClientURI = new MongoClientURI(ownMongodbUri);
            try (MongoClient mongoClient = new MongoClient(mongoClientURI)) {
                mongoSettings = mongoClient
                        .getDatabase(mongoClientURI.getDatabase())
                        .getCollection("settings").find().iterator().next()
                        .get("mongodb", Map.class);
                mongoSettings.put("dummy", ownMongodbUri);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("mongodb client initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }

    @Bean(name = "master")
    public MongoClient getMongoClientMaster() {
        return new MongoClient(getMongoClientURI(MongoTarget.MASTER));
    }

    @Bean(name = "snapshot")
    public MongoClient getMongoClientSnapshot() {
        return new MongoClient(getMongoClientURI(MongoTarget.SNAPSHOT));
    }

    @Bean(name = "diff")
    public MongoClient getMongoClientDiff() {
        return new MongoClient(getMongoClientURI(MongoTarget.DIFF));
    }

    @Bean(name = "seiyulab")
    public MongoClient getMongoClientSeiyulab() {
        return new MongoClient(getMongoClientURI(MongoTarget.SEIYULAB));
    }

    @Bean(name = "dummy")
    public MongoClient getMongoClientDummy() {
        return new MongoClient(getMongoClientURI(MongoTarget.DUMMY));
    }
}
