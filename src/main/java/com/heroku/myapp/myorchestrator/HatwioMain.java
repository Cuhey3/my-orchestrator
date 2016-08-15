package com.heroku.myapp.myorchestrator;

import com.heroku.myapp.commons.config.Environments;
import io.hawt.embedded.Main;
import org.springframework.stereotype.Component;

@Component
public class HatwioMain {

    public HatwioMain() throws Exception {
        Main main = new Main();
        System.setProperty("hawtio.authenticationEnabled", "false");
        String port = Environments.ENV.get("PORT");
        main.setPort(Integer.parseInt(port));
        main.setContextPath("/foo");
        main.setWarLocation("./");
        main.run();
    }
}
