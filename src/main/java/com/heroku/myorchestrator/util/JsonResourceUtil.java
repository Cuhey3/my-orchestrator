package com.heroku.myorchestrator.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.heroku.myorchestrator.config.enumerate.Paths;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonResourceUtil {

    private Map<String, Object> map;

    public JsonResourceUtil(String path) throws Exception {
        this.map = new LinkedHashMap<>();
        try {
            InputStream resourceAsStream
                    = ClassLoader.class.getResourceAsStream(path);
            JsonReader reader
                    = new JsonReader(
                            new InputStreamReader(resourceAsStream, "UTF-8"));
            map = new Gson().fromJson(reader, Map.class);
        } catch (NullPointerException e) {
            System.out.println("resource: " + path + " is not find.");
        }
    }

    public JsonResourceUtil(Paths paths) throws Exception {
        this(paths.getPath());
    }

    public <T> T get(String key, Class<T> clazz) {
        return (T) map.get(key);
    }

    public String get(String key) {
        return (String) map.get(key);
    }
}
