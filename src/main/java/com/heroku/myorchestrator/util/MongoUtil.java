package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoUtil {

    MongoClientURI mongoClientURI;
    String databaseName;
    String collectionName;
    MongoClient client;

    public MongoUtil(String kind, String collectionName) {
        mongoClientURI = MongoConfig.getMongoClientURI(kind);
        databaseName = mongoClientURI.getDatabase();
        this.collectionName = kind + "_" + collectionName;
    }

    public MongoCollection<Document> getCollection() {
        if (client == null) {
            client = new MongoClient(mongoClientURI);
        }
        return client.getDatabase(databaseName).getCollection(collectionName);
    }

    public void close() {
        this.client.close();
    }

    public Document findFirst() {
        return this.getCollection().find().limit(1).iterator().next();
    }

    public Document findLatest() {
        return this.getCollection().find().sort(new Document("timestamp", -1)).limit(1).iterator().next();
    }
}
