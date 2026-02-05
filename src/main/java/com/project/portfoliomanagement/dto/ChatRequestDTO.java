package com.project.portfoliomanagement.dto;

public class ChatRequestDTO {

    private String message;
    private String context; // Optional: portfolio context for more relevant responses

    public ChatRequestDTO() {}

    public ChatRequestDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
