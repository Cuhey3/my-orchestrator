package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.heroku.myorchestrator.config.enumerate.ActionType;
import com.heroku.myorchestrator.config.enumerate.Kind;
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

    public MongoUtil(Exchange exchange, Kind kind) {
        this.registry = exchange.getContext().getRegistry();
        this.kind = kind.expression();
    }

    public MongoUtil type(String type) {
        this.type = type;
        return this;
    }

    public MongoUtil type(ActionType type) {
        this.type = type.expression();
        return this;
    }

    public MongoUtil snapshot() {
        this.type = ActionType.SNAPSHOT.expression();
        return this;
    }

    public MongoUtil diff() {
        this.type = ActionType.DIFF.expression();
        return this;
    }

    public MongoUtil master() {
        this.type = ActionType.MASTER.expression();
        return this;
    }

    public MongoCollection<Document> getCollection() throws Exception {
        if (this.type == null) {
            throw new MongoUtilTypeNotSetException();
        }
        return registry.lookupByNameAndType(type, MongoClient.class)
                .getDatabase(MongoConfig.getMongoClientURI(type).getDatabase())
                .getCollection(getCollectionName());
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
        Document query = new Document()
                .append("_id", new ObjectId(objectIdHexString));
        return getNextDocument(this.getCollection().find(query));
    }

    public Optional<Document> findById(Map map) throws Exception {
        return this.findById((String) map.get(type + "_id"));
    }

    public String insertOne(Document document) throws Exception {
        document.append("creationDate", new Date());
        this.getCollection().insertOne(document);
        return document.get("_id", ObjectId.class).toHexString();
    }

    public String replaceOne(Document document) throws Exception {
        document.append("creationDate", new Date());
        this.getCollection().replaceOne(new Document()
                .append("_id", document.get("_id")), document);
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

    public void disableDocument() {
    }
}
