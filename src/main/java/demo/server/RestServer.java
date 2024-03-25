package demo.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RestServer {

    public static void main(String[] args) throws IOException {
        // Create HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8002), 100);

        // Create a context to handle requests
        server.createContext("/api/square", new SquareHandler());

        // Start the server
        server.start();
        System.out.println("Server is listening on port 8002");
    }

    // Define a handler for the "/api/square" endpoint
    static class SquareHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Check if the request method is GET
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.close();
                return;
            }

            // Get the path from the request URL
            String path = exchange.getRequestURI().getPath();

            // Extract the number from the path
            int number;
            try {
                // Extract the number from the last part of the path
                String[] pathParts = path.split("/");
                number = Integer.parseInt(pathParts[pathParts.length - 1]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                exchange.sendResponseHeaders(400, 0); // Bad Request
                exchange.close();
                return;
            }

            // Calculate square of the number
            int square = number * number;


            // Create JSON response
            String jsonResponse = "{\"number\":" + number +
                    ", \"numberSquare\":" + square +
                    " }";

            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

            // Get the response body as an output stream
            OutputStream responseBody = exchange.getResponseBody();

            // Write the JSON response to the response body
            responseBody.write(jsonResponse.getBytes());

            // Close the output stream
            responseBody.close();
        }
    }
}
