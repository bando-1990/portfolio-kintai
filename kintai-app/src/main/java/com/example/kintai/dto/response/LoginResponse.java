package com.example.kintai.dto.response;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
    public LoginResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
