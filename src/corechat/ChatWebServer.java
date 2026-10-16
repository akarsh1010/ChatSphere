package corechat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class ChatWebServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";

    public static void main(String[] args) throws IOException {
        int port = PORT;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // REST API Contexts
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/messages", new MessagesHandler());
        server.createContext("/api/send", new SendMessageHandler());

        // Static Frontend Files Context (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler());

        server.start();

        System.out.println("==================================================");
        System.out.println("   🌐 CoreChat Web Server Started Successfully!   ");
        System.out.println("==================================================");
        System.out.println("👉 Open your browser and go to: http://localhost:" + port);
        System.out.println("👉 Press Ctrl+C in terminal to stop the server.");
        System.out.println("==================================================");
    }

    // ==============================================================
    // 1. Static File Handler (Serves index.html, style.css, app.js)
    // ==============================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File(WEB_DIR + path);
            if (!file.exists() || file.isDirectory()) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                contentType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript; charset=UTF-8";
            } else if (path.endsWith(".png")) {
                contentType = "image/png";
            } else if (path.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    // ==============================================================
    // 2. Authentication: Register Handler
    // ==============================================================
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJsonField(body, "username").trim().toLowerCase();
            String password = extractJsonField(body, "password");

            if (username.isEmpty() || password.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Username and password are required.\"}");
                return;
            }

            ArrayList<User> users = FileManager.loadUsers();
            for (User u : users) {
                if (u.getUserName().equalsIgnoreCase(username)) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Username already exists.\"}");
                    return;
                }
            }

            User newUser = new User(username, password);
            FileManager.saveUser(newUser);

            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Registration successful!\"}");
        }
    }

    // ==============================================================
    // 3. Authentication: Login Handler
    // ==============================================================
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String username = extractJsonField(body, "username").trim().toLowerCase();
            String password = extractJsonField(body, "password");

            ArrayList<User> users = FileManager.loadUsers();
            for (User u : users) {
                if (u.getUserName().equalsIgnoreCase(username)) {
                    if (u.getPassword().equals(password)) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Login successful!\"}");
                        return;
                    } else {
                        sendJsonResponse(exchange, 401, "{\"success\":false,\"error\":\"Incorrect password.\"}");
                        return;
                    }
                }
            }

            sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"User not found. Please register first.\"}");
        }
    }

    // ==============================================================
    // 4. Users List Handler
    // ==============================================================
    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String currentUser = params.getOrDefault("current", "").toLowerCase();

            ArrayList<User> users = FileManager.loadUsers();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (User u : users) {
                if (u.getUserName().equalsIgnoreCase(currentUser)) {
                    continue; // exclude logged in user
                }
                if (!first) {
                    json.append(",");
                }
                json.append("{\"username\":\"").append(escapeJson(u.getUserName())).append("\"}");
                first = false;
            }
            json.append("]");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    // ==============================================================
    // 5. Messages History Handler
    // ==============================================================
    static class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String user1 = params.getOrDefault("user1", "").toLowerCase();
            String user2 = params.getOrDefault("user2", "").toLowerCase();

            ArrayList<User> users = FileManager.loadUsers();
            ArrayList<Message> allMessages = FileManager.loadMessages(users);

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (Message m : allMessages) {
                String sender = m.getSender().getUserName();
                String receiver = m.getReceiver().getUserName();

                if ((sender.equalsIgnoreCase(user1) && receiver.equalsIgnoreCase(user2))
                        || (sender.equalsIgnoreCase(user2) && receiver.equalsIgnoreCase(user1))) {
                    if (!first) {
                        json.append(",");
                    }
                    json.append("{")
                            .append("\"sender\":\"").append(escapeJson(sender)).append("\",")
                            .append("\"receiver\":\"").append(escapeJson(receiver)).append("\",")
                            .append("\"content\":\"").append(escapeJson(m.getMessage())).append("\",")
                            .append("\"timestamp\":\"").append(escapeJson(m.getTimestamp())).append("\"")
                            .append("}");
                    first = false;
                }
            }
            json.append("]");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    // ==============================================================
    // 6. Send Message Handler
    // ==============================================================
    static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);
            String senderName = extractJsonField(body, "sender").trim().toLowerCase();
            String receiverName = extractJsonField(body, "receiver").trim().toLowerCase();
            String content = extractJsonField(body, "content");

            if (senderName.isEmpty() || receiverName.isEmpty() || content.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing message fields.\"}");
                return;
            }

            ArrayList<User> users = FileManager.loadUsers();
            User sender = null;
            User receiver = null;

            for (User u : users) {
                if (u.getUserName().equalsIgnoreCase(senderName)) sender = u;
                if (u.getUserName().equalsIgnoreCase(receiverName)) receiver = u;
            }

            if (sender == null || receiver == null) {
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Sender or receiver not found.\"}");
                return;
            }

            Message message = new Message(sender, receiver, content);
            FileManager.saveMessage(message);

            sendJsonResponse(exchange, 200, "{\"success\":true}");
        }
    }

    // ==============================================================
    // Helper Utilities (Zero-Dependency JSON and HTTP Helpers)
    // ==============================================================
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> queryMap = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return queryMap;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                queryMap.put(key, value);
            }
        }
        return queryMap;
    }

    private static String extractJsonField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";

        int colon = json.indexOf(":", idx + search.length());
        if (colon == -1) return "";

        int startQuote = json.indexOf("\"", colon);
        if (startQuote == -1) return "";

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                sb.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '\"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
