package com.heroku.myorchestrator.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.heroku.myorchestrator.exceptions.SettingNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

public enum Settings {
    ENV, IRON;
    private final String path;
    private Map<String, Object> map;

    private Settings() {
        this.path = String.format("/config/%s.json", this.name().toLowerCase());
    }

    public String get(String key) throws Exception {
        return this.get(key, key);
    }

    public String get(String key1, String key2) throws Exception {
        return Optional.ofNullable(System.getenv(key1))
                .orElseGet(() -> Optional.ofNullable((String) map().get(key2))
                        .orElseThrow(() -> new SettingNotFoundException()));
    }

    private Map<String, Object> map() {
        return Optional.ofNullable(map)
                .orElseGet(() -> loadResource());
    }

    private Map<String, Object> loadResource() {
        try {
            InputStream is = ClassLoader.class.getResourceAsStream(path);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            map = new Gson().fromJson(new JsonReader(isr), Map.class);
            return map;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException();
        }
    }
}
