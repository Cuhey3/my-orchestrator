package com.heroku.myorchestrator.util.consumers;

import com.heroku.myorchestrator.config.enumerate.Kind;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class KindUtil {
    
    private Kind kind;
    
    public KindUtil kind(Kind kind) {
        this.kind = kind;
        return this;
    }
    
    public String preMessage() {
        InputStream resourceAsStream = ClassLoader.class
                .getResourceAsStream("/message/" + kind.expression() + ".json");
        try (BufferedReader buffer
                = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            System.out.println("premessage initialization failed..."
                    + "\nSystem is shutting down.");
            System.exit(1);
            return null;
        }
    }
}
