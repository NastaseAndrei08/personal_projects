package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String senderId; // User ID or "admin"
    private String content;  // The text message

    @JsonProperty("isAdmin")
    private boolean isAdmin; // True if sent by admin
}