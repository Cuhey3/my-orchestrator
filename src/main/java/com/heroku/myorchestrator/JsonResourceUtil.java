package com.heroku.myorchestrator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class JsonResourceUtil {

    Map<String, Object> map;

    public JsonResourceUtil(String path) throws UnsupportedEncodingException {
        Gson gson = new Gson();
        InputStream resourceAsStream = ClassLoader.class.getResourceAsStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(resourceAsStream, "UTF-8"));
        map = gson.fromJson(reader, Map.class);
    }

    public <T> T get(String key, Class<T> clazz) {
        return (T) map.get(key);
    }

    public String get(String key) {
        return (String) map.get(key);
    }
}
