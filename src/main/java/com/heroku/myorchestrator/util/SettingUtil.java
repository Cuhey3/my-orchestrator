package com.heroku.myorchestrator.util;

import com.heroku.myorchestrator.config.enumerate.Paths;
import com.heroku.myorchestrator.exceptions.SettingNotFoundException;

public class SettingUtil {

    private JsonResourceUtil jru;
    private final Paths paths;

    public SettingUtil(Paths paths) {
        this.paths = paths;
    }

    public String get(String key) throws Exception {
        return this.get(key, key);
    }

    public String get(String key1, String key2) throws Exception {
        String value = System.getenv(key1);
        if (value != null) {
            return value;
        } else {
            if (jru == null) {
                jru = new JsonResourceUtil(paths);
            }
            value = jru.get(key2);
            if (value != null) {
                return value;
            } else {
                throw new SettingNotFoundException();
            }
        }
    }
}
