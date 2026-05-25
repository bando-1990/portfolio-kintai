package com.example.kintai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(
    @Email String email,
    String currentPassword,
    @Size(min = 8, message = "パスワードは8文字以上で入力してください") String newPassword
) {}
