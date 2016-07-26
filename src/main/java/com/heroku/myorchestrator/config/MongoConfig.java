package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.Paths;
import com.heroku.myorchestrator.util.SettingUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static Map mongoSettings;

    public static MongoClientURI getMongoClientURI(ActionType type) {
        return new MongoClientURI(
                (String) mongoSettings.get(type.expression()));
    }

    private String ownMongodbUri;

    public MongoConfig() throws UnsupportedEncodingException, Exception {
        try {
            ownMongodbUri = new SettingUtil(Paths.SETTINGS).get("MONGODB_URI");
            MongoClientURI mongoClientURI = new MongoClientURI(ownMongodbUri);
            String database = mongoClientURI.getDatabase();
            try (MongoClient mongoClient = new MongoClient(mongoClientURI)) {
                mongoSettings = mongoClient.getDatabase(database)
                        .getCollection("settings").find().iterator().next()
                        .get("mongodb", Map.class);
                mongoSettings.put("dummy", ownMongodbUri);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("mongodb client initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
        }
    }

    @Bean(name = "master")
    public MongoClient getMongoClientMaster() {
        return new MongoClient(getMongoClientURI(ActionType.MASTER));
    }

    @Bean(name = "snapshot")
    public MongoClient getMongoClientSnapshot() {
        return new MongoClient(getMongoClientURI(ActionType.SNAPSHOT));
    }

    @Bean(name = "diff")
    public MongoClient getMongoClientDiff() {
        return new MongoClient(getMongoClientURI(ActionType.DIFF));
    }

    @Bean(name = "dummy")
    public MongoClient getMongoClientDummy() {
        return new MongoClient(getMongoClientURI(ActionType.DUMMY));
    }
}
