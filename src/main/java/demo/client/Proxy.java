package demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class Proxy {

    public static void main(String[] args) throws IOException {
        // Create HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 100);

        // Create a context to handle requests for different APIs
        server.createContext("/grpcunary", new Handler("grpcunary"));
        server.createContext("/grpcstream", new Handler("grpcstream"));
        server.createContext("/rest", new Handler("rest"));

        // Start the server
        server.start();
        //System.out.println("Proxy client is listening on port 8000");
    }

    // Define a handler for the "/api/grpc" endpoint
    static class Handler implements HttpHandler {
        private final String grpcType;

        public Handler(String grpcType) {
            this.grpcType = grpcType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Set response headers
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);

            // Get the response body as an output stream
            OutputStream responseBody = exchange.getResponseBody();

            // Call the gRPC client method based on the type
            // Get the path from the request URL
            String path = exchange.getRequestURI().getPath();
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

            Map<Integer, Integer> response = null;
            try {
                    response = callClientMethod(grpcType, number);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            ObjectMapper objectMapper = new ObjectMapper();

            // Write the response
            objectMapper.writeValue(responseBody, response);

            // Close the output stream
            responseBody.close();
        }

        // Call your gRPC client method here
        private Map<Integer, Integer> callClientMethod(String grpcType , int number) throws InterruptedException, IOException {

            var start = System.currentTimeMillis();
            Map<Integer, Integer> res = null;
            // You can call your gRPC client method from DemoClient class
            if(grpcType.contentEquals("rest")){
                String restApiUrl = "http://localhost:8002/api/square/"; // Assuming the Square API server is running on port 8002
                res = ApiHandler.squareRestClient(restApiUrl , number);
            }
            else if(grpcType.contentEquals("grpcunary")){
                res = ApiHandler.getSquare(number);
            }
            else if(grpcType.contentEquals("grpcstream")){
                res = ApiHandler.getSquareStream(number);
            }
            // Return the response from gRPC client method
            // Assuming your gRPC client returns a String response
            var end = System.currentTimeMillis();
            System.out.println("time taken for " + grpcType + " : " + (end - start) + "ms");
            return res;
        }


    }
}
