package com.heroku.myorchestrator.config;

import com.heroku.myorchestrator.exceptions.SettingNotFoundException;
import com.heroku.myorchestrator.util.JsonResourceUtil;

public enum Settings {
    ENV, IRON;
    private final String path;
    private JsonResourceUtil jru;

    private Settings() {
        this.path = String.format("/config/%s.json", this.name().toLowerCase());
        try {
            this.jru = new JsonResourceUtil(this.path);
        } catch (Exception e) {
        }
    }

    public String get(String key) throws Exception {
        return this.get(key, key);
    }

    public String get(String key1, String key2) throws Exception {
        String value = System.getenv(key1);
        if (value != null) {
            return value;
        } else {
            value = jru.get(key2);
            if (value != null) {
                return value;
            } else {
                throw new SettingNotFoundException();
            }
        }
    }
}
