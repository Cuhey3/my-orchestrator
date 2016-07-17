package com.heroku.myorchestrator.hawtio;

import com.heroku.myorchestrator.JsonResourceUtil;
import io.hawt.embedded.Main;
import org.springframework.stereotype.Component;

@Component
public class HawtioMain {

    public HawtioMain() throws Exception {
        Main main = new Main();
        System.setProperty("hawtio.authenticationEnabled", "false");
        String port = System.getenv("PORT");
        if (port == null) {
            JsonResourceUtil jru = new JsonResourceUtil("/config/settings.json");
            port = jru.get("PORT");
        }
        main.setPort(Integer.parseInt(port));
        main.setContextPath("/foo");
        main.setWarLocation("./");
        main.run();
    }
}
