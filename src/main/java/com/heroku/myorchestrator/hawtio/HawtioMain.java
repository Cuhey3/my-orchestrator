package com.heroku.myorchestrator.hawtio;

import io.hawt.embedded.Main;
import org.springframework.stereotype.Component;

@Component
public class HawtioMain {

  public HawtioMain() throws Exception {
    Main main = new Main();
    System.setProperty("hawtio.authenticationEnabled", "false");
    String port = System.getenv("PORT");
    if (port == null) {
      port = "4646";
    }
    main.setPort(Integer.parseInt(port));
    main.setContextPath("/foo");
    main.setWarLocation("./");
    main.run();
  }
}
