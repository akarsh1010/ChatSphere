package corechat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * AIService — Native AI Assistant for ChatSphere (@SphereAI).
 * Supports zero-dependency Google Gemini API calls with graceful fallback
 * to a built-in Core Java intelligent conversation engine.
 */
public class AIService {

    public static final String BOT_NAME = "SphereAI";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Dispatches an asynchronous AI response so the HTTP send request completes instantly.
     */
    public static void replyAsync(User recipientUser, String userPrompt) {
        CompletableFuture.runAsync(() -> {
            try {
                // Natural typing delay (600ms)
                Thread.sleep(600);

                String reply = generateResponse(userPrompt, recipientUser.getUserName());
                User botUser = new User(BOT_NAME, "");
                Message botMessage = new Message(botUser, recipientUser, reply);
                FileManager.saveMessage(botMessage);
            } catch (Exception e) {
                System.out.println("-> Error generating SphereAI response: " + e.getMessage());
            }
        });
    }

    /**
     * Resolves response via Gemini Cloud API (if key is set) or built-in Smart Engine.
     */
    public static String generateResponse(String prompt, String username) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Hey @" + username + "! 👋 Ask me anything about programming, Java, or general topics!";
        }

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                String cloudReply = callGeminiApi(prompt, apiKey.trim());
                if (cloudReply != null && !cloudReply.trim().isEmpty()) {
                    return cloudReply;
                }
            } catch (Exception e) {
                System.out.println("-> Gemini API error, using built-in smart engine: " + e.getMessage());
            }
        }

        return generateLocalSmartReply(prompt, username);
    }

    private static String callGeminiApi(String prompt, String apiKey) throws IOException, InterruptedException {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String safePrompt = prompt.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "");

        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\""
                + "You are SphereAI, the intelligent assistant inside ChatSphere (a chat app built by Alok Rana using Core Java). "
                + "Keep answers concise, direct, helpful, and formatted for chat bubbles. User question: "
                + safePrompt + "\"}]}]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(12))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() == 200) {
            String body = response.body();
            int textIdx = body.indexOf("\"text\": \"");
            if (textIdx != -1) {
                int start = textIdx + 9;
                int end = body.indexOf("\"", start);
                while (end != -1 && body.charAt(end - 1) == '\\') {
                    end = body.indexOf("\"", end + 1);
                }
                if (end != -1) {
                    String rawText = body.substring(start, end);
                    return rawText.replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                }
            }
        }
        return null;
    }

    private static String generateLocalSmartReply(String prompt, String username) {
        String lower = prompt.trim().toLowerCase();

        // 1. Greetings
        if (lower.matches(".*\\b(hi|hello|hey|namaste|heya|hola|greetings)\\b.*")) {
            return "Hey @" + username + "! 👋 I'm SphereAI, your native AI assistant. How can I help you today?";
        }

        // 2. Creator & Platform Info
        if (lower.contains("who made") || lower.contains("who created") || lower.contains("kisme banaya")
                || lower.contains("creator") || lower.contains("developer") || lower.contains("author")) {
            return "ChatSphere & CoreChat was engineered by Alok Rana (@alok0770) using pure Core Java (Java SE) with zero external frameworks! 🚀";
        }

        if (lower.contains("who are you") || lower.contains("tum kon ho") || lower.contains("what are you")) {
            return "I am @SphereAI 🤖 — the native AI assistant embedded into ChatSphere. I can answer questions, explain Java concepts, tell jokes, or chat 24/7!";
        }

        // 3. Core Java & Programming Explanations
        if (lower.contains("polymorphism")) {
            return "📘 Polymorphism in Java means 'many forms'. It allows an object to take on different shapes. Key types: 1) Compile-time (Method Overloading) and 2) Runtime (Method Overriding using @Override).";
        }
        if (lower.contains("inheritance")) {
            return "🧬 Inheritance in Java allows a child class to inherit fields and methods from a parent class via 'extends'. It maximizes code reusability!";
        }
        if (lower.contains("encapsulation")) {
            return "🛡️ Encapsulation is wrapping data (fields) and code (methods) together while hiding internal state using private variables and public getters/setters.";
        }
        if (lower.contains("abstraction")) {
            return "🎭 Abstraction hides internal complexity and shows only essential functionality. Achieved in Java via abstract classes and interfaces.";
        }
        if (lower.contains("what is java") || lower.contains("java kya hai")) {
            return "☕ Java is a high-level, class-based, object-oriented language famous for 'Write Once, Run Anywhere' (WORA) via the Java Virtual Machine (JVM)!";
        }
        if (lower.contains("exception")) {
            return "⚠️ Exceptions are runtime anomalies that disrupt program flow. Java handles them cleanly with try, catch, finally, throw, and throws.";
        }
        if (lower.contains("thread") || lower.contains("multithread")) {
            return "⚡ Multithreading allows concurrent task execution. ChatSphere uses an ExecutorService ThreadPool (10 threads) to process concurrent user requests simultaneously!";
        }
        if (lower.contains("http") || lower.contains("server")) {
            return "🌐 ChatSphere runs on Java's built-in HttpServer (com.sun.net.httpserver). It routes REST API requests and serves static web assets without Apache or Tomcat!";
        }

        // 4. Fun & Entertainment
        if (lower.contains("joke") || lower.contains("funny") || lower.contains("hasi")) {
            String[] jokes = {
                    "Why do Java programmers wear glasses? Because they don't C#! 😂",
                    "There are 10 types of people in the world: those who understand binary, and those who don't! 🤖",
                    "A SQL query walks into a bar, approaches two tables and asks: 'May I join you?' 🍺",
                    "Why was the JavaScript developer sad? Because they didn't Node how to Express themselves! 🚀"
            };
            return jokes[(int) (Math.random() * jokes.length)];
        }

        if (lower.contains("how are you") || lower.contains("kese ho") || lower.contains("kaisa hai")) {
            return "I am running at optimal speed! ⚡ Server health is green and I'm ready to assist you. What's on your mind?";
        }

        if (lower.contains("help") || lower.contains("what can you do")) {
            return "💡 Here is what you can ask me:\n• Explain Java/OOP concepts (Polymorphism, Threads, JVM)\n• Ask about ChatSphere's architecture & author\n• Request a programmer joke\n• Or add GEMINI_API_KEY for unrestricted live AI!";
        }

        // Default Intelligent Response
        return "That's a thoughtful question, @" + username + "! 💡 I am currently in Core Intelligent mode. To unlock unlimited real-time web AI answers, set GEMINI_API_KEY in your cloud environment!";
    }
}
