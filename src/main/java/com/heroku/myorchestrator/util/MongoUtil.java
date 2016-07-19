package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.Date;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;

public class MongoUtil {

  ApplicationContext applicationContext;

  public MongoUtil(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

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
            .sort(new Document("creationDate", -1)).limit(1).iterator().next();
  }

  public Document findById(String kind, String collectionKind, String objectIdHexString) {
    ObjectId objectId = new ObjectId(objectIdHexString);
    MongoCollection<Document> collection = this.getCollection(kind, collectionKind);
    FindIterable<Document> find = collection.find(new Document().append("_id", objectId));
    if (find.iterator().hasNext()) {
      return find.iterator().next();
    } else {
      return null;
    }
  }

  public Document findById(String kind, String collectionKind, Map map) {
    String objectIdHexString = (String) map.get(kind + "_id");
    return this.findById(kind, collectionKind, objectIdHexString);
  }

  public String insertOne(String kind, String collectionKind, Document document) {
    document.append("creationDate", new Date());
    this.getCollection(kind, collectionKind).insertOne(document);
    ObjectId objectId = document.get("_id", ObjectId.class);
    return objectId.toHexString();
  }

  public String getCollectionName(String kind, String collectionKind) {
    return kind + "_" + collectionKind;
  }
}
