package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.exceptions.MongoUtilTypeNotSetException;
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
    private String type;
    private final String kind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
        this.kind = MessageUtil.getKind(exchange);
    }

    public MongoUtil type(String type) {
        this.type = type;
        return this;
    }

    public MongoUtil type(ActionType type) {
        this.type = type.expression();
        return this;
    }

    public MongoCollection<Document> getCollection() throws Exception {
        if (this.type == null) {
            throw new MongoUtilTypeNotSetException();
        }
        String collectionName = getCollectionName();
        MongoClient client
                = registry.lookupByNameAndType(type, MongoClient.class);
        String databaseName = MongoConfig.getMongoClientURI(type).getDatabase();
        return client.getDatabase(databaseName).getCollection(collectionName);
    }

    public Optional<Document> findFirst() throws Exception {
        FindIterable<Document> find = this.getCollection().find().limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findLatest() throws Exception {
        FindIterable<Document> find = this.getCollection()
                .find().sort(new Document("creationDate", -1)).limit(1);
        return getNextDocument(find);
    }

    public Optional<Document> findById(String objectIdHexString) throws Exception {
        ObjectId objectId = new ObjectId(objectIdHexString);
        MongoCollection<Document> collection = this.getCollection();
        Document query = new Document().append("_id", objectId);
        return getNextDocument(collection.find(query));
    }

    public Optional<Document> findById(Map map) throws Exception {
        String objectIdHexString = (String) map.get(type + "_id");
        return this.findById(objectIdHexString);
    }

    public String insertOne(Document document) throws Exception {
        document.append("creationDate", new Date());
        this.getCollection().insertOne(document);
        return document.get("_id", ObjectId.class).toHexString();
    }

    private String getCollectionName() {
        return type + "_" + kind;
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
