package com.heroku.myorchestrator.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.heroku.myorchestrator.exceptions.SettingNotFoundException;
import java.io.InputStreamReader;
import java.util.Map;

public enum Settings {
    ENV, IRON;
    private final String path;
    private Map<String, Object> map = null;

    private Settings() {
        this.path = String.format("/config/%s.json", this.name().toLowerCase());
    }

    public String get(String key) throws Exception {
        return this.get(key, key);
    }

    public String get(String key1, String key2) throws Exception {
        String value = System.getenv(key1);
        if (value != null) {
            return value;
        } else {
            if (map == null) {
                JsonReader reader = new JsonReader(new InputStreamReader(
                        ClassLoader.class.getResourceAsStream(path), "UTF-8"));
                map = new Gson().fromJson(reader, Map.class);
            }
            value = (String) map.get(key2);
            if (value != null) {
                return value;
            } else {
                throw new SettingNotFoundException();
            }
        }
    }
}
