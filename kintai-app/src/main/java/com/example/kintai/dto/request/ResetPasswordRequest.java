package com.example.kintai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8, message = "パスワードは8文字以上で入力してください") String newPassword
) {}
