package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoUtil {
    public static String getObjectIdHexString(Document document) {
        return document.get("_id", ObjectId.class).toHexString();
    }

    private final Registry registry;
    private String kind;
    private String collectionKind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
    }
    
    public MongoUtil kind(String kind){
      this.kind = kind;
      return this;
    }

    public MongoUtil collection(String collectionKind){
      this.collectionKind = collectionKind;
      return this;
    }

    public MongoCollection<Document> getCollection() {
        String collectionName = getCollectionName();
        MongoClient client
                = registry.lookupByNameAndType(kind, MongoClient.class);
        String databaseName = MongoConfig.getMongoClientURI(kind).getDatabase();
        return client.getDatabase(databaseName).getCollection(collectionName);
    }

    public Optional<Document> findFirst() {
        FindIterable<Document> find
                = this.getCollection().find().limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findLatest() {
        FindIterable<Document> find = this.getCollection()
                .find().sort(new Document("creationDate", -1)).limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findById(String objectIdHexString) {
        ObjectId objectId = new ObjectId(objectIdHexString);
        MongoCollection<Document> collection = this.getCollection();
        Document query = new Document().append("_id", objectId);
        return getNextDocument(collection.find(query));
    }

    public Optional<Document> findById(Map map) {
        String objectIdHexString = (String) map.get(kind + "_id");
        return this.findById(objectIdHexString);
    }

    public String insertOne(Document document) {
        document.append("creationDate", new Date());
        this.getCollection().insertOne(document);
        return document.get("_id", ObjectId.class).toHexString();
    }

    private String getCollectionName() {
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
}
