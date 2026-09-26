package edu.co.escuelaing;

import java.util.HashMap;
import java.util.Map;

public class Request {

    Map<String, String> body = new HashMap<>();

    public static Request fromQuery(String rawQuery) {
        Request request = new Request();

        if (rawQuery == null || rawQuery.isEmpty()) {
            return request;
        }

        for (String param : rawQuery.split("&")) {
            if (param.isEmpty()) {
                continue;
            }
            String[] kv = param.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            request.body.put(key, value);
        }
        return request;
    }

    public String getValue(String key) {
        return body.get(key);
    }

}
