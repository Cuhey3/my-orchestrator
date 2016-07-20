package com.heroku.myorchestrator;

import com.heroku.myorchestrator.util.JsonResourceUtil.Paths;
import com.heroku.myorchestrator.util.SettingUtil;
import io.hawt.embedded.Main;
import org.springframework.stereotype.Component;

@Component
public class HatwioMain {

    public HatwioMain() throws Exception {
        Main main = new Main();
        System.setProperty("hawtio.authenticationEnabled", "false");
        String port = new SettingUtil(Paths.SETTINGS).get("PORT");
        main.setPort(Integer.parseInt(port));
        main.setContextPath("/foo");
        main.setWarLocation("./");
        main.run();
    }
}
