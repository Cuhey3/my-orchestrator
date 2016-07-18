package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.util.JsonResourceUtil;
import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    String mongoUri;
    Map mongoSettings;

    public MongoConfig() throws UnsupportedEncodingException {
        String settingKey = "MONGODB_URI";
        mongoUri = System.getenv(settingKey);
        if (mongoUri == null) {
            JsonResourceUtil jru = new JsonResourceUtil(Paths.SETTINGS);
            mongoUri = jru.get(settingKey);
        }
        MongoClientURI mongoClientURI = new MongoClientURI(mongoUri);
        String database = mongoClientURI.getDatabase();
        try (MongoClient mongoClient = new MongoClient(mongoClientURI)) {
            mongoSettings = mongoClient.getDatabase(database)
                    .getCollection("settings").find().iterator().next()
                    .get("mongodb", Map.class);
        }
    }

    @Bean(name = "own")
    MongoClient mongoClient() throws UnknownHostException, UnsupportedEncodingException {
        return new MongoClient(new MongoClientURI(mongoUri));
    }

    @Bean(name = "master")
    MongoClient mongoClientMaster() {
        String masterMongoUri = (String) mongoSettings.get("master");
        return new MongoClient(new MongoClientURI(masterMongoUri));
    }

    @Bean(name = "snapshot")
    MongoClient mongoClientSnapshot() {
        String snapshotMongoUri = (String) mongoSettings.get("snapshot");
        return new MongoClient(new MongoClientURI(snapshotMongoUri));
    }

    @Bean(name = "diff")
    MongoClient mongoClientDiff() {
        String diffMongoUri = (String) mongoSettings.get("diff");
        return new MongoClient(new MongoClientURI(diffMongoUri));
    }
}
