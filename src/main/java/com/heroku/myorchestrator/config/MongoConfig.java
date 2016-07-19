package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.util.JsonResourceUtil;
import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

  String ownMongoUri;
  static Map mongoSettings;

  public MongoConfig() throws UnsupportedEncodingException {
    String settingKey = "MONGODB_URI";
    ownMongoUri = System.getenv(settingKey);
    if (ownMongoUri == null) {
      JsonResourceUtil jru = new JsonResourceUtil(Paths.SETTINGS);
      ownMongoUri = jru.get(settingKey);
    }
    MongoClientURI mongoClientURI = new MongoClientURI(ownMongoUri);
    String database = mongoClientURI.getDatabase();
    try (MongoClient mongoClient = new MongoClient(mongoClientURI)) {
      mongoClient.getDatabase(database);
      mongoSettings = mongoClient.getDatabase(database)
              .getCollection("settings").find().iterator().next()
              .get("mongodb", Map.class);
    }
  }

  @Bean(name = "own")
  public MongoClient getMongoClient() {
    return new MongoClient(new MongoClientURI(ownMongoUri));
  }

  @Bean(name = "master")
  public MongoClient getMongoClientMaster() {
    return new MongoClient(getMongoClientURI("master"));
  }

  @Bean(name = "snapshot")
  public MongoClient getMongoClientSnapshot() {
    return new MongoClient(getMongoClientURI("snapshot"));
  }

  @Bean(name = "diff")
  public MongoClient getMongoClientDiff() {
    return new MongoClient(getMongoClientURI("diff"));
  }

  public static MongoClientURI getMongoClientURI(String kind) {
    return new MongoClientURI((String) mongoSettings.get(kind));
  }
}
