package com.example.kintai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserCreateRequest(
    @NotBlank @Size(max = 100) String loginId,
    @NotBlank @Size(max = 100) String name,
    @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8) String password,
    List<String> roles
) {}
