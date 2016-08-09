package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.MongoConfig;
import com.heroku.myorchestrator.config.enumerate.Kind;
import com.heroku.myorchestrator.config.enumerate.MongoTarget;
import com.heroku.myorchestrator.exceptions.DocumentNotFoundException;
import com.heroku.myorchestrator.exceptions.MongoUtilTypeNotSetException;
import com.heroku.myorchestrator.util.content.DocumentUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Registry;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoUtil {

    private final Registry registry;
    protected MongoTarget target;
    protected MongoTarget customTarget;
    protected Kind kind;

    public MongoUtil(Exchange exchange) {
        this.registry = exchange.getContext().getRegistry();
        Optional<String> kindString = MessageUtil.getKind(exchange);
        if (kindString.isPresent()) {
            this.kind = Kind.valueOf(kindString.get());
        }
    }

    public final MongoUtil target(MongoTarget target) {
        this.target = target;
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

    public MongoDatabase database(MongoTarget t) {
        return registry.lookupByNameAndType(t.expression(), MongoClient.class)
                .getDatabase(MongoConfig.getMongoClientURI(t).getDatabase());
    }

    public MongoDatabase database() {
        return database(target);
    }

    public MongoCollection<Document> collection() {
        MongoTarget t = ofNullable(
                ofNullable(this.customTarget).orElse(this.target))
                .orElseThrow(() -> new MongoUtilTypeNotSetException());
        return database(t).getCollection(collectionName());
    }

    public Optional<Document> optionalFind() {
        return nextDocument(latest(), false);
    }

    private FindIterable<Document> latest() {
        return collection().find()
                .sort(new Document("creationDate", -1)).limit(1);
    }

    public Optional<Document> findById(String objectIdHexString) {
        return nextDocument(collection().find(
                new Document("_id", new ObjectId(objectIdHexString))), false);
    }

    public Optional<Document> findByMessage(Map message) {
        return findById((String) message.get(targetIdKey()));
    }

    public String insertOne(Document document) {
        if (!document.containsKey("creationDate")) {
            document.append("creationDate", new Date());
        }
        collection().insertOne(document);
        return DocumentUtil.objectIdHexString(document);
    }

    private String collectionName() {
        return target.expression() + "_" + kind;
    }

    private String targetIdKey() {
        return target.expression() + "_id";
    }

    private Optional<Document> nextDocument(FindIterable<Document> iterable, boolean bool) {
        MongoCursor<Document> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return Optional.ofNullable(iterator.next());
        } else if (bool) {
            throw new DocumentNotFoundException();
        } else {
            return Optional.empty();
        }
    }

    public void disableDocument() {
    }

    public Document findOrElseThrow() {
        return nextDocument(latest(), true).get();
    }

    public Document findOrElseThrow(Kind kind) {
        Kind kind0 = kind;
        kind(kind);
        try {
            return findOrElseThrow();
        } finally {
            kind(kind0);
        }
    }
}
