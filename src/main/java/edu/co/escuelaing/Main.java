package edu.co.escuelaing;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        WebFramework webFramework = new WebFramework();

        webFramework.get("/hello", (req, resp) -> {
            String name = req.getValue("name");

            if (name == null || name.isBlank()) {
                name = "World";
            }

            resp.setStatus(200);
            return "Hello" + " " + name;
        });

        String environment = System.getenv().getOrDefault("APP_ENV", "production");

        webFramework.get("/shutdown", (req, resp) -> {
            if (environment.equals("development")) {
                resp.setStatus(200);
                webFramework.stop();
                return "The server will shutdown after this response";
            }

            resp.setStatus(405);
            return "Not Allowed Operation";
        });

        try {
            webFramework.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}