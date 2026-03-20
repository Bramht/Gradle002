package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = resolvePort();
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> respond(exchange, 200,
                "Railway deployment is running. Try /health for a health check.\n"));
        server.createContext("/health", exchange -> respond(exchange, 200, "OK\n"));

        server.setExecutor(Executors.newFixedThreadPool(4));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();

        System.out.println("Server started on port " + port);
    }

    private static int resolvePort() {
        String value = System.getenv("PORT");
        if (value == null || value.isBlank()) {
            return 8080;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            System.err.println("Invalid PORT value '" + value + "', falling back to 8080");
            return 8080;
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        } finally {
            exchange.close();
        }
    }
}
