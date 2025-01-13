package com.mindhub.todolist.dtos;

public class AuthResponseDTO {
    private String token;
    private String message;

    public AuthResponseDTO(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
