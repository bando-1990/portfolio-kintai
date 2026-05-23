package com.example.kintai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserUpdateRequest(
    @Size(max = 100) String name,
    @Email @Size(max = 255) String email,
    Boolean active,
    List<String> roles
) {}
