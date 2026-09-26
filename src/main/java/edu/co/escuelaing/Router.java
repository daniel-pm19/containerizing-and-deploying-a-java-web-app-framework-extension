package edu.co.escuelaing;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, WebService> routes = new HashMap<>();

    public void add(String route, WebService ws) {
        routes.put(route, ws);
    }

    public WebService resolve(String path) {
        return routes.get(path);
    }
}
