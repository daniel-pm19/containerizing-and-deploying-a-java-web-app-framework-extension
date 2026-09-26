package edu.co.escuelaing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpServer {

    private Router router;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    public HttpServer(Router router) {
        this.router = router;
    }

    public void start(int port) throws IOException {
        String portValue = System.getenv().getOrDefault("PORT", "8080");
        String poolSizeValue = System.getenv().getOrDefault("THREAD_POOL_SIZE", "10");

        int portParsed = Integer.parseInt(portValue);
        int poolSize = Integer.parseInt(poolSizeValue);

        running = true;

        try (ServerSocket server = new ServerSocket(portParsed)) {
            System.out.println("Server listening on port:" + portParsed);
            this.serverSocket = server;
            executorService = Executors.newFixedThreadPool(poolSize);
            while (running) {
                Socket client = server.accept();
                executorService.submit(() -> handleRequest(client));
            }
        } catch(SocketException e){
            if(running) throw e;
        }

        System.out.println("Server stopped gracefully");
    }

    public void stop() {
        running = false;
        try {
            if(serverSocket != null) serverSocket.close();
        } catch (IOException i) {}
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e){
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleRequest(Socket client) {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()))) {
            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            String[] parts = requestLine.split(" ");

            if (parts.length < 2) {
                sendResponse(client, "Bad Request", 400);
                client.close();
                return;
            }

            String method = parts[0].toUpperCase();

            if (!"GET".equals(method)) {
                sendResponse(client, "Method Not Allowed", 405);
                client.close();
                return;
            }

            String fullPath = parts[1];
            String path = fullPath.split("\\?")[0];

            WebService service = router.resolve(path);
            if (service == null) {
                sendResponse(client, "Not Found", 404);
                return;
            }

            String query = fullPath.contains("?")
                    ? fullPath.split("\\?", 2)[1]
                    : "";
            Request req = Request.fromQuery(query);
            Response resp = new Response();

            try {
                String result = service.invoque(req, resp);
                sendResponse(client, result, resp.getStatus());
            } catch (Exception e) {
                sendResponse(client, "Internal Server Error", 500);
            } finally {
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(Socket client, String body, int status) {
        sendBytes(client, body.getBytes(), "text/plain; charset=UTF-8", status);
    }

    private void sendBytes(
            Socket client,
            byte[] body,
            String contentType,
            int status) {
        try {
            OutputStream out = client.getOutputStream();
            out.write(buildResponse(body, contentType, status));
            out.flush();
        } catch (IOException ignored) {
        }
    }

    private byte[] buildResponse(byte[] body, String contentType, int status) {
        String statusLine = switch (status) {
            case 200 -> "HTTP/1.1 200 OK\r\n";
            case 400 -> "HTTP/1.1 400 BAD REQUEST\r\n";
            case 404 -> "HTTP/1.1 404 NOT FOUND\r\n";
            case 405 -> "HTTP/1.1 405 NOT ALLOWED\r\n";
            case 500 -> "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n";
            default -> "HTTP/1.1 500 INTERNAL SERVER ERROR\r\n";
        };

        String headers = "Content-Type: " +
                contentType +
                "\r\n" +
                "Content-Length: " +
                body.length +
                "\r\n" +
                "Connection: close\r\n" +
                "\r\n";

        byte[] headBytes = (statusLine + headers).getBytes();
        byte[] response = new byte[headBytes.length + body.length];
        System.arraycopy(headBytes, 0, response, 0, headBytes.length);
        System.arraycopy(body, 0, response, headBytes.length, body.length);
        return response;
    }

}
