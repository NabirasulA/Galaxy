package com.project.portfoliomanagement.dto;

public class ChatResponseDTO {

    private String response;
    private boolean success;
    private String error;

    public ChatResponseDTO() {}

    public ChatResponseDTO(String response, boolean success) {
        this.response = response;
        this.success = success;
    }

    public static ChatResponseDTO success(String response) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setResponse(response);
        dto.setSuccess(true);
        return dto;
    }

    public static ChatResponseDTO error(String error) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setError(error);
        dto.setSuccess(false);
        return dto;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
