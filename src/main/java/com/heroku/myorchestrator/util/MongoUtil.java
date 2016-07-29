package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.exceptions.MongoUtilTypeNotSetException;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
    protected MongoTarget target;
    protected MongoTarget customTarget;
    protected Kind kind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
        String kindString = MessageUtil.getKind(exchange);
        if (kindString != null) {
            this.kind = Kind.valueOf(kindString);
        }
    }

    public final MongoUtil target(MongoTarget type) {
        this.target = type;
        return this;
    }

    public MongoUtil useDummy() {
        this.customTarget = MongoTarget.DUMMY;
        return this;
    }

    public final MongoUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public MongoUtil snapshot() {
        this.target = MongoTarget.SNAPSHOT;
        return this;
    }

    public MongoUtil diff() {
        this.target = MongoTarget.DIFF;
        return this;
    }

    public MongoUtil master() {
        this.target = MongoTarget.MASTER;
        return this;
    }

    public MongoDatabase database() {
        String database
                = MongoConfig.getMongoClientURI(this.target).getDatabase();
        return registry
                .lookupByNameAndType(target.expression(), MongoClient.class)
                .getDatabase(database);
    }

    public MongoCollection<Document> collection() throws Exception {
        if (this.target == null && this.customTarget == null) {
            throw new MongoUtilTypeNotSetException();
        }
        MongoTarget t;
        if (this.customTarget != null) {
            t = this.customTarget;
        } else {
            t = this.target;
        }
        return registry.lookupByNameAndType(t.expression(), MongoClient.class)
                .getDatabase(MongoConfig.getMongoClientURI(t).getDatabase())
                .getCollection(collectionName());
    }

    public Optional<Document> findFirst() throws Exception {
        FindIterable<Document> find = collection().find().limit(1);
        return nextDocument(find);
    }

    public Optional<Document> findLatest() throws Exception {
        return nextDocument(collection().find()
                .sort(new Document("creationDate", -1)).limit(1));
    }

    public Optional<Document> findById(String objectIdHexString) throws Exception {
        return nextDocument(collection().find(
                new Document("_id", new ObjectId(objectIdHexString))));
    }

    public Optional<Document> findById(Map map) throws Exception {
        return findById((String) map.get(typeIdKey()));
    }

    public String insertOne(Document document) throws Exception {
        if (!document.containsKey("creationDate")) {
            document.append("creationDate", new Date());
        }
        collection().insertOne(document);
        return getObjectIdHexString(document);
    }

    public String replaceOne(Document document) throws Exception {
        collection().replaceOne(
                new Document("_id", document.get("_id")), document);
        return getObjectIdHexString(document);
    }

    private String collectionName() {
        return target.expression() + "_" + kind;
    }

    private String typeIdKey() {
        return target.expression() + "_id";
    }

    private Optional<Document> nextDocument(FindIterable<Document> iterable) {
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
