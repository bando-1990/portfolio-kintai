package com.example.kintai.dto.response;

import com.example.kintai.entity.User;

import java.util.List;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String loginId,
    String name,
    String email,
    boolean active,
    List<String> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getLoginId(),
            user.getName(),
            user.getEmail(),
            user.getActive(),
            user.getRoles().stream().map(r -> r.getCode()).toList()
        );
    }
}
