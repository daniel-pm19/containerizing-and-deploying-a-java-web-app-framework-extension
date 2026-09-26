package edu.co.escuelaing;

import java.io.IOException;

public class WebFramework {

    private final Router router = new Router();
    private final HttpServer httpServer = new HttpServer(router);

    public void get(String route, WebService ws) {
        router.add(route, ws);
    }

    public void start(int port) throws IOException {
        httpServer.start(port);
    }

    public void stop() {
        httpServer.stop();
    }
    
}
