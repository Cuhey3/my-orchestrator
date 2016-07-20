package com.heroku.myorchestrator.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonResourceUtil {

    Map<String, Object> map = new LinkedHashMap<>();

    public JsonResourceUtil(String path) throws UnsupportedEncodingException {
      try {
        InputStream resourceAsStream = ClassLoader.class.getResourceAsStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(resourceAsStream, "UTF-8"));
        Gson gson = new Gson();
        map = gson.fromJson(reader, Map.class);
      } catch (NullPointerException e) {
        System.out.println("resource: " + path + " is not find.");
      }
    }

    public JsonResourceUtil(Paths paths) throws UnsupportedEncodingException {
        this(paths.path);
    }

    public <T> T get(String key, Class<T> clazz) {
        return (T) map.get(key);
    }

    public String get(String key) {
        return (String) map.get(key);
    }

    public enum Paths {
        SETTINGS("/config/settings.json"),
        IRON("/config/iron.json");
        public String path;

        private Paths(String path) {
            this.path = path;
        }
    }
}
