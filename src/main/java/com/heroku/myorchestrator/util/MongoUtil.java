package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MongoUtil {

  @Autowired
  ApplicationContext applicationContext;

  public MongoCollection<Document> getCollection(
          String kind, String collectionKind) {
    String collectionName = getCollectionName(kind, collectionKind);
    MongoClient client = applicationContext.getBean(kind, MongoClient.class);
    String databaseName = MongoConfig.getMongoClientURI(kind).getDatabase();
    return client.getDatabase(databaseName).getCollection(collectionName);
  }

  public Document findFirst(String kind, String collectionKind) {

    String collectionName = getCollectionName(kind, collectionKind);
    return this.getCollection(kind, collectionName).find()
            .limit(1).iterator().next();
  }

  public Document findLatest(String kind, String collectionKind) {
    String collectionName = getCollectionName(kind, collectionKind);
    return this.getCollection(kind, collectionName).find()
            .sort(new Document("timestamp", -1)).limit(1).iterator().next();
  }

  public String getCollectionName(String kind, String collectionKind) {
    return kind + "_" + collectionKind;
  }
}
