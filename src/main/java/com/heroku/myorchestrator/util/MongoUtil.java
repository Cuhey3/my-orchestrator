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
    protected ActionType type;
    protected Kind kind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
        this.kind = Kind.valueOf(MessageUtil.getKind(exchange));
    }

    public final MongoUtil type(ActionType type) {
        this.type = type;
        return this;
    }

    public final MongoUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }

    public MongoUtil snapshot() {
        this.type = ActionType.SNAPSHOT;
        return this;
    }

    public MongoUtil diff() {
        this.type = ActionType.DIFF;
        return this;
    }

    public MongoUtil master() {
        this.type = ActionType.MASTER;
        return this;
    }

    public MongoCollection<Document> collection() throws Exception {
        if (this.type == null) {
            throw new MongoUtilTypeNotSetException();
        }
        return registry
                .lookupByNameAndType(type.expression(), MongoClient.class)
                .getDatabase(MongoConfig.getMongoClientURI(type).getDatabase())
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
        return nextDocument(collection().find(new Document()
                .append("_id", new ObjectId(objectIdHexString))));
    }

    public Optional<Document> findById(Map map) throws Exception {
        return findById((String) map.get(typeIdKey()));
    }

    public String insertOne(Document document) throws Exception {
        document.append("creationDate", new Date());
        collection().insertOne(document);
        return getObjectIdHexString(document);
    }

    public String replaceOne(Document document) throws Exception {
        document.append("creationDate", new Date());
        collection().replaceOne(new Document()
                .append("_id", document.get("_id")), document);
        return getObjectIdHexString(document);
    }

    private String collectionName() {
        return type.expression() + "_" + kind;
    }

    private String typeIdKey() {
        return type.expression() + "_id";
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
