package edu.co.escuelaing;

import java.io.IOException;

public class WebFramework {

    private final Router router = new Router();
    private final HttpServer httpServer = new HttpServer(router);

    public void get(String route, WebService ws) {
        router.add(route, ws);
    }

    public void start() throws IOException {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop();
    }
    
}
