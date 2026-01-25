package com.example.demo.controller;

import com.example.demo.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class ChatController {

    // Inject the API Key from docker-compose environment variables
    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    /**
     * Endpoint: /app/send
     * Broadcasts to: /topic/messages
     */
    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {

        // REQUIREMENT 1: Admin messages pass through untouched
        if (chatMessage.isAdmin()) {
            return chatMessage;
        }

        String text = chatMessage.getContent().toLowerCase();
        String response = null;

        // REQUIREMENT 2: Automated Rule-Based Response System
        if (text.contains("hello") || text.contains("hi")) {
            response = "Hello! How can I help you with your energy consumption?";
        } else if (text.contains("bill") || text.contains("cost")) {
            response = "You can view your estimated costs in the 'My Devices' dashboard.";
        } else if (text.contains("high consumption") || text.contains("alert")) {
            response = "High consumption alerts are sent automatically if you exceed the hourly limit.";
        } else if (text.contains("limit")) {
            response = "The hourly limit is set by the admin. Contact support to change it.";
        } else if (text.contains("sensor")) {
            response = "Make sure your sensors are connected to Wi-Fi and transmitting data.";
        } else if (text.contains("admin")) {
            response = "An admin will be with you shortly.";
        } else if (text.contains("login") || text.contains("password")) {
            response = "If you cannot login, please contact the IT department to reset your credentials.";
        } else if (text.contains("history") || text.contains("past")) {
            response = "You can view historical energy data on the dashboard charts.";
        } else if (text.contains("add device")) {
            response = "To add a new device, please submit a request form to the administrator.";
        } else if (text.contains("hours") || text.contains("time")) {
            response = "Our support team is available from 9 AM to 5 PM, Mon-Fri.";
        } else if (text.contains("thank")) {
            response = "You're welcome! Have a great day.";
        }

        // REQUIREMENT 3: AI-Driven Customer Support (Fallback)
        // If no rule matched, we call the LLM
        if (response == null) {
            response = callLLM(chatMessage.getContent());
        }

        // Append the assistant's response to the user's message
        if (response != null) {
            chatMessage.setContent(chatMessage.getContent() + "\n\n[Assistant]: " + response);
        }

        return chatMessage;
    }

    @MessageMapping("/typing")
    @SendTo("/topic/typing")
    public String typing(@Payload String username) {
        return username + " is typing...";
    }

    /**
     * Calls Google Gemini API if a key is present.
     * Otherwise, returns a mock response to satisfy the assignment without paying.
     */
    private String callLLM(String query) {
        // 1. Check if we have an API Key
        if (apiKey == null || apiKey.isEmpty() || apiKey.length() < 10) {
            System.out.println(">>> No Valid API Key found. Using Mock AI Response.");
            try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) {}
            return "I am the Energy Support AI. I couldn't find a specific rule for your query ('"
                    + query + "'), and I am currently in 'Offline Mode' (No API Key configured). "
                    + "Please contact an admin.";
        }

        // 2. Call Google Gemini API
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite-preview-09-2025:generateContent?key=" + apiKey;

            // Construct JSON Body: { "contents": [{ "parts": [{ "text": "..." }] }] }
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", "You are a helpful customer support agent for an Energy Management System. " +
                    "Answer this user query concisely (max 2 sentences): " + query);

            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(parts));

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Execute Request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // Parse Response
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) content.get("parts");
                    if (!resParts.isEmpty()) {
                        return (String) resParts.get(0).get("text");
                    }
                }
            }
            return "I'm having trouble thinking right now. Please try again.";

        } catch (Exception e) {
            System.err.println("AI Call Failed: " + e.getMessage());
            return "I am experiencing network issues. Please contact a human admin.";
        }
    }
}