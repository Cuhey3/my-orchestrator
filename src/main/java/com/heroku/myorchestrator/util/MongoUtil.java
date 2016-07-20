package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationContext;

public class MongoUtil {

    ApplicationContext applicationContext;

    public MongoUtil(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public MongoCollection<Document> getCollection(String kind, String collectionKind) {
        String collectionName = getCollectionName(kind, collectionKind);
        MongoClient client
                = applicationContext.getBean(kind, MongoClient.class);
        String databaseName = MongoConfig.getMongoClientURI(kind).getDatabase();
        return client.getDatabase(databaseName).getCollection(collectionName);
    }

    public Optional<Document> findFirst(String kind, String collectionKind) {
        String collectionName = getCollectionName(kind, collectionKind);
        FindIterable<Document> find
                = this.getCollection(kind, collectionName).find().limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findLatest(String kind, String collectionKind) {
        String collectionName = getCollectionName(kind, collectionKind);
        FindIterable<Document> find = this.getCollection(kind, collectionName)
                .find().sort(new Document("creationDate", -1)).limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findById(String kind, String collectionKind, String objectIdHexString) {
        ObjectId objectId = new ObjectId(objectIdHexString);
        MongoCollection<Document> collection
                = this.getCollection(kind, collectionKind);
        Document query = new Document().append("_id", objectId);
        return getNextDocument(collection.find(query));
    }

    public Optional<Document> findById(
            String kind, String collectionKind, Map map) {
        String objectIdHexString = (String) map.get(kind + "_id");
        return this.findById(kind, collectionKind, objectIdHexString);
    }

    public String insertOne(
            String kind, String collectionKind, Document document) {
        document.append("creationDate", new Date());
        this.getCollection(kind, collectionKind).insertOne(document);
        return document.get("_id", ObjectId.class).toHexString();
    }

    private String getCollectionName(String kind, String collectionKind) {
        return kind + "_" + collectionKind;
    }

    private Optional<Document> getNextDocument(FindIterable<Document> iterable) {
        MongoCursor<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    public static String getObjectIdHexString(Document document) {
        return document.get("_id", ObjectId.class).toHexString();
    }
}
