package com.heroku.myorchestrator;

import com.heroku.myorchestrator.util.JsonResourceUtil;
import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import io.hawt.embedded.Main;
import org.springframework.stereotype.Component;

@Component
public class HatwioMain {

    public HatwioMain() throws Exception {
        Main main = new Main();
        System.setProperty("hawtio.authenticationEnabled", "false");
        String port = System.getenv("PORT");
        if (port == null) {
            JsonResourceUtil jru = new JsonResourceUtil(Paths.SETTINGS);
            port = jru.get("PORT");
        }
        main.setPort(Integer.parseInt(port));
        main.setContextPath("/foo");
        main.setWarLocation("./");
        main.run();
    }
}
